package org.jys.example.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author YueSong Jiang
 * @date 2019/11/1
 *
 * exclude spring datasource auto config
 * also can use [spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration]
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class CommonTools {

    public static void main(String[] args){
        SpringApplication.run(CommonTools.class,args);
    }
}
