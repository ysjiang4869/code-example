package org.jys.example.common.sql.postgres;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jys.example.common.sql.BaseTable;
import org.jys.example.common.sql.copy.CopyInData;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Supplier;

/**
 * @author YueSong Jiang
 * @date 2020/2/3
 */
public abstract class BasePostgresBulkLoadService<T extends CopyInData> implements BulkLoadService<T> {
    protected BaseTable table;
    protected long algorithm;
    @Autowired
    protected DataSource dataSource;
    @Autowired
    protected JdbcTemplate jdbcTemplate;
    @Value("${data.load.retry-num:3}")
    protected int maxRetryNum;
    @Value("${database.type:'PG'}")
    private String databaseType;
    protected String copyInSql = " COPY %s FROM STDIN DELIMITER E'%s' NULL '%s' ";
    private static final String INSERT_FAULT_RECORD = "INSERT INTO viid_system.faultrecord(tablename, data, recordtime,err_msg) VALUES (?, ?, ?, ?);";
    private static final String GP_CREATE_TABLE_APPEND_SQL = " (LIKE %s ) WITH(APPENDONLY=true,ORIENTATION=column) DISTRIBUTED BY(recordid); ";
    private static final String PG_CREATE_TABLE_APPEND_SQL = " (LIKE %s INCLUDING INDEXES)";
    private static final String CREATE_TABLE_SQL = "CREATE TABLE %s ";
    private static final String GP_DATABASE_TYPE = "GP";
    protected char fieldDelimiter;
    protected String stringFieldDelimiter;
    protected char lineDelimiter;
    protected String stringLineDelimiter;
    protected String nullCharacter;
    private static final Logger logger = LoggerFactory.getLogger(BasePostgresBulkLoadService.class);

    public BasePostgresBulkLoadService(BaseTable table, long algorithm) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.table = table;
        this.algorithm = algorithm;
        //获得泛型实际类型
        Class<T> clazz = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        T item = clazz.getDeclaredConstructor().newInstance();
        lineDelimiter = '\n';
        stringLineDelimiter = "\\n";
        fieldDelimiter = item.getFieldDelimiter();
        stringFieldDelimiter = "\\" + StringUtils.leftPad(Integer.toOctalString(item.getFieldDelimiter()), 3, '0');
        this.nullCharacter = item.getNullCharacter();
    }

    @Transactional(rollbackFor = {RuntimeException.class})
    @Override
    public long bulkLoad(List<T> data, Supplier<Object> processSupplier) {
        Map<String, List<String>> recordCacheMap = new HashMap<>(16);
        String tableName;
        String copyString;
        for (T item : data) {
            tableName = getTableName(item);
            if (tableName == null) {
                continue;
            }
            copyString = item.generateCopyString();
            recordCacheMap.putIfAbsent(tableName, new ArrayList<>());
            recordCacheMap.get(tableName).add(copyString);
        }
        long successRecords = 0;
        for (Map.Entry<String, List<String>> entry : recordCacheMap.entrySet()) {
            successRecords += doCopyIn(entry.getKey(), entry.getValue());
        }
        return successRecords;
    }

    protected String getTableName(T item) {
        return this.table.getTableName(algorithm, item.getRecordTime());
    }

    protected long doCopyIn(String tableName, List<String> records) {
        StringBuilder copyInBody = null;

        for (String record : records) {
            if (copyInBody == null) {
                copyInBody = new StringBuilder();
            } else {
                copyInBody.append(this.lineDelimiter);
            }
            copyInBody.append(record);
        }

        if (copyInBody == null) {
            return 0L;
        }

        try (InputStream inputStream
                     = new ByteArrayInputStream(copyInBody.toString().getBytes(StandardCharsets.UTF_8))) {
            inputStream.mark(0);
            return copyInAction(inputStream, tableName, maxRetryNum);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new BulkLoadException(0, "io exception when get input string from data", (Object) null);
        }
    }

    protected long copyInAction(InputStream is, String tableName, int retryNum) {
        if (retryNum < 1) {
            logger.warn("bulk reload warning: exceed max retry num {}", this.maxRetryNum);
            return 0L;
        }
        Connection connection = getConnection();
        if (null == connection) {
            logger.error("get connection failed, connection is null");
            return 0;
        }
        String copyString = String.format(copyInSql, tableName, stringFieldDelimiter, nullCharacter);

        try {
            CopyManager copyManager = new CopyManager((BaseConnection) connection.getMetaData().getConnection());
            long result = copyManager.copyIn(copyString, is);
            if (result < 0) {
                throw new BulkLoadException(0, "copy in save data failed, unknown error", null);
            } else {
                return result;
            }
        } catch (PSQLException e) {
            logger.error("bulk load process error: {}", copyString, e);
            try {
                connection.rollback();
            } catch (SQLException ex) {
                throw new BulkLoadException(0, "roll back connection failed", null);
            }

            PsqlErrorState errorState = new PsqlErrorState(e);
            PsqlErrorStateEnum errorStateEnum = errorState.getSqlError();

            if (errorStateEnum == PsqlErrorStateEnum.TABLE_NOT_FOUND) {
                createTable(tableName);
                return copyInAction(is, tableName, maxRetryNum);
            } else if (errorStateEnum == PsqlErrorStateEnum.ERROR_UNIQUE_VIOLATION
                    || errorStateEnum == PsqlErrorStateEnum.OTHER_RESULT) {
                saveToErrorTable(is, tableName, e);
            } else if (errorStateEnum == PsqlErrorStateEnum.CONNECTION_FAILURE) {
                return copyInAction(is, tableName, maxRetryNum);
            } else {
                return copyInAction(is, tableName, --retryNum);
            }

        } catch (SQLException | IOException e) {
            logger.error("catch exception, continue to try [{}] time", retryNum - 1);
            return copyInAction(is, copyString, --retryNum);
        } finally {
            releaseConnection(connection);
        }

        return 0;
    }

    protected void saveToErrorTable(InputStream is, String tableName, Exception e) {
        try {
            is.reset();
            String errorData = IOUtils.toString(is, StandardCharsets.UTF_8);
            FaultRecord faultCache = new FaultRecord(tableName, errorData, e.getMessage());
            this.doError(faultCache);
        } catch (IOException ex) {
            logger.error("get io exception when do bulk load");
            logger.error(ex.getMessage(), ex);
            throw new BulkLoadException(0, "io exception when get input string from input stream", (Object) null);
        }
    }

    protected Connection getConnection() {
        while (true) {
            try {
                return DataSourceUtils.getConnection(this.dataSource);
            } catch (CannotGetJdbcConnectionException e) {
                logger.error("Bulk load getConnection() error : {}", e.getMessage());
            }
        }
    }

    protected void releaseConnection(Connection connection) {
        if (null != connection) {
            try {
                DataSourceUtils.doReleaseConnection(connection, this.dataSource);
            } catch (SQLException e) {
                logger.error("HikariUtils releaseConnection(Connection) error : {}", e.getMessage());
            }
        }

    }

    protected void evictConnection(Connection connection) {
        this.releaseConnection(connection);
        if (this.dataSource instanceof HikariDataSource) {
            ((HikariDataSource) this.dataSource).evictConnection(connection);
        } else {
            try {
                DataSourceUtils.doCloseConnection(connection, this.dataSource);
            } catch (SQLException e) {
                logger.error("close connection failed");
                logger.error(e.getMessage(), e);
            }
        }

    }

    protected void doError(FaultRecord errorData) {
        this.jdbcTemplate.update(INSERT_FAULT_RECORD,
                errorData.getTable(), errorData.getData(), System.currentTimeMillis(), errorData.getErrorMessage());
    }

    protected void createTable(String tableName) {
        String sql = CREATE_TABLE_SQL;
        if (Objects.equals("GP", this.databaseType)) {
            sql = sql + GP_CREATE_TABLE_APPEND_SQL;
        } else {
            sql = sql + PG_CREATE_TABLE_APPEND_SQL;
        }

        sql = String.format(sql, tableName, this.table.getFullTableName());
        this.jdbcTemplate.execute(sql);
    }

    public abstract Class<T> getGenericClass();
}

