package org.jys.common;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author YueSong Jiang
 * @date 2019/11/1
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class CommonTools {

    public static void main(String[] args){
        SpringApplication.run(CommonTools.class,args);
    }
}
