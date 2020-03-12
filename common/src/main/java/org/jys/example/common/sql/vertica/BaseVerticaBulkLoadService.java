package org.jys.example.common.sql.vertica;

import com.vertica.jdbc.VerticaConnection;
import com.vertica.jdbc.VerticaCopyStream;
import org.apache.commons.lang3.StringUtils;
import org.jys.example.common.sql.BaseTable;
import org.jys.example.common.sql.copy.CopyInData;
import org.jys.example.common.sql.postgres.BasePostgresBulkLoadService;
import org.jys.example.common.sql.postgres.BulkLoadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

/**
 * @author YueSong Jiang
 * @date 2020/2/3
 */
public abstract class BaseVerticaBulkLoadService<T extends CopyInData> extends BasePostgresBulkLoadService<T> {
    private static final Logger logger = LoggerFactory.getLogger(BaseVerticaBulkLoadService.class);
    private static final String CREATE_TABLE_SQL = "CREATE TABLE %s Like %s INCLUDING PROJECTIONS ";
    private Boolean isSingleNode = null;


    public BaseVerticaBulkLoadService(BaseTable table, long algorithmEnum) throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
        super(table, algorithmEnum);
        T item = getGenericClass().getDeclaredConstructor().newInstance();
        lineDelimiter = item.getLineDelimiter();
        stringLineDelimiter = "\\" + StringUtils.leftPad(Integer.toOctalString(item.getLineDelimiter()), 3, '0');
        fieldDelimiter = item.getFieldDelimiter();
        stringFieldDelimiter = "\\" + StringUtils.leftPad(Integer.toOctalString(item.getFieldDelimiter()), 3, '0');
        nullCharacter = item.getNullCharacter();
        copyInSql = "COPY %s FROM STDIN DELIMITER E'%s' RECORD TERMINATOR E'%s'  NULL '%s' ABORT ON ERROR ";
    }

    @Override
    protected String getTableName(CopyInData item) {
        long version = item.getAlgorithmId();
        return table.getTableName(version);
    }

    @Override
    protected long copyInAction(InputStream is, String tableName, int retryNum) {
        if (retryNum < 1) {
            logger.warn("bulk reload warning: exceed max retry num {}", maxRetryNum);
            return 0L;
        }

        Connection connection = getConnection();
        if (null == connection) {
            logger.error("get connection failed, connection is null");
            return 0L;
        }

        String copyString = String.format(copyInSql, tableName, stringFieldDelimiter, stringLineDelimiter, nullCharacter);
        if (isSingleNode == null) {
            Integer nodeNum = jdbcTemplate.queryForObject("select count('node_name') from nodes;", Integer.class);
            isSingleNode = Optional.ofNullable(nodeNum).orElse(1) == 1;
        }

        if (isSingleNode) {
            copyString = copyString + " DIRECT ";
        }

        try {
            VerticaCopyStream copyStream =
                    new VerticaCopyStream((VerticaConnection) connection.getMetaData().getConnection(), copyString, is);
            copyStream.start();
            long result = copyStream.finish();
            if (result <= 0) {
                throw new BulkLoadException(0, "bulk load save 0 data", null);
            } else {
                return result;
            }
        } catch (SQLException e) {
            logger.error("bulk load process error: {}", copyString, e);
            VsqlErrorState vsqlErrorState = new VsqlErrorState(e);
            VsqlErrorStateEnum errorStateEnum = vsqlErrorState.getSqlError();

            if (errorStateEnum == VsqlErrorStateEnum.TABLE_NOT_FOUND) {
                createTable(tableName);
                return copyInAction(is, tableName, maxRetryNum);
            } else if (errorStateEnum == VsqlErrorStateEnum.ERROR_UNIQUE_VIOLATION
                    || errorStateEnum == VsqlErrorStateEnum.OTHER_RESULT) {
                saveToErrorTable(is, tableName, e);
            } else if (errorStateEnum == VsqlErrorStateEnum.CONNECTION_FAILURE) {
                return copyInAction(is, tableName, maxRetryNum);
            } else if (errorStateEnum == VsqlErrorStateEnum.INSUFFICIENT_RESOURCE) {
                Integer vendorCode = vsqlErrorState.getVendorCode();
                if (2245 == vendorCode || 5065 == vendorCode) {
                    logger.warn("too many ros containers, wait 10 seconds and retry");
                    try {
                        Thread.sleep(10000);
                        return copyInAction(is, tableName, maxRetryNum);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            } else {
                return copyInAction(is, tableName, --retryNum);
            }
        } finally {
            releaseConnection(connection);
        }
        return 0L;
    }

    @Override
    protected void createTable(String tableName) {
        String sql = String.format("CREATE TABLE %s Like %s INCLUDING PROJECTIONS ", tableName, table.getFullTableName());
        jdbcTemplate.execute(sql);
    }
}
