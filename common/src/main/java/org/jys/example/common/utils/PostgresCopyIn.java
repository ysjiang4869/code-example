package org.jys.example.common.utils;

import com.zaxxer.hikari.HikariDataSource;
import org.postgresql.copy.CopyManager;
import org.postgresql.core.BaseConnection;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author YueSong Jiang
 * @date 2019/6/26
 * @description <p> </p>
 */
public class PostgresCopyIn {

    private CopyManager copyManager;
    private Connection connection;
    private int i = 10;
    private boolean x = true;

    public PostgresCopyIn(HikariDataSource dataSource) throws SQLException {
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);
        copyManager = new CopyManager((BaseConnection) connection.getMetaData().getConnection());
    }

    //重复执行多次改方法，用于测试copy in的事务
    public long execute() throws IOException, SQLException {
        String sql1 = "COPY test1 FROM STDIN ";
        String sql2 = "COPY test1 FROM STDIN ";
        // use \t tab to split different column, and use \n to split different record
        String sql3 = i + "\t2";
        String sql4;
        if (x) {
            sql4 = i + "\t570\t";
        } else {
            sql4 = i + "\t570";
        }
        x = !x;
        i++;
        InputStream in = new ByteArrayInputStream(sql3.getBytes(StandardCharsets.UTF_8));
        long success = copyManager.copyIn(sql1, in);
        in = new ByteArrayInputStream(sql4.getBytes(StandardCharsets.UTF_8));
        try {
            success += copyManager.copyIn(sql2, in);
        } catch (Exception e) {
            connection.rollback();
        } finally {
            connection.commit();
        }
        return success;
    }
}
