package org.jys.example.common;

import javassist.CannotCompileException;
import javassist.NotFoundException;
import org.jys.example.common.sql.copy.CopyMethodGenerator;
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

    public static void main(String[] args) throws NotFoundException, CannotCompileException {
        //can not use CopyInDataObject.class.getName() get the name
        //this will cause class load first!!
        CopyMethodGenerator.modifyCopyMethod("org.jys.example.common.sql.copy.CopyInDataObject");
        CopyMethodGenerator.modifyCopyMethod("org.jys.example.common.sql.copy.CopyInDataObject2");
        CopyMethodGenerator.modifyCopyMethod("org.jys.example.common.sql.copy.CopyInDataObject3");
        SpringApplication.run(CommonTools.class,args);
    }
}
