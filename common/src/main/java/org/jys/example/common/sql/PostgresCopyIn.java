package org.jys.example.common.sql;

import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author YueSong Jiang
 * @date 2019/6/26
 * Postgres Copy-In tools
 * get connection from connection pool
 */
@Component
@ConditionalOnBean(HikariDataSource.class)
public class PostgresCopyIn {

    private final HikariDataSource dataSource;
    private static final Logger logger= LoggerFactory.getLogger(PostgresCopyIn.class);

    @Autowired
    public PostgresCopyIn(HikariDataSource dataSource) throws SQLException {
        this.dataSource=dataSource;
    }

    public long doCopyIn(InputStream inputStream, String sql){
        long result=0;
        Connection connection=null;
        try {
            connection=getConnection();
            CopyManager manager=getCopyManager(connection);
            result=manager.copyIn(sql, inputStream);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            if(e instanceof PSQLException){
                String code=((PSQLException)e).getSQLState();
                if("23305".equals(code)){
                    return result;
                }
            }
            evictConnection(connection);
        }
        return result;
    }

    public CopyManager getCopyManager(Connection connection) throws SQLException {
        connection.setAutoCommit(false);
        return new CopyManager((BaseConnection) connection.getMetaData().getConnection());
    }

    public Connection getConnection(){
        while (true){
            try {
                return dataSource.getConnection();
            } catch (SQLException e) {
                logger.error("Get connection from hikari pool error");
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void ReleaseConnection(Connection connection){
        if(null != connection){
            try {
                connection.close();
            }catch (SQLException e){
                logger.error("Release connection to hikari pool error");
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void evictConnection(Connection connection){
        dataSource.evictConnection(connection);
    }

}
