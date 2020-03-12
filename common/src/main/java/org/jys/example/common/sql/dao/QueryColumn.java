package org.jys.example.common.sql.dao;

import java.lang.annotation.*;

/**
 * @author YueSong Jiang
 * @date 2020/2/3
 */
@Documented
@Inherited
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface QueryColumn {
    String field();
}
