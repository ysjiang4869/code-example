package org.jys.example.common.sql;

import java.lang.annotation.*;

/**
 * @author jiangys
 * add to CopyIn data field
 * used for judge copy in data field order
 */
@Documented
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CopyOrder {

    /**
     * copy order
     * must be the field name in same class or super no-private field
     * "NULL" means the first value
     * the value must be not same for different fields
     * @return field name which is before this field
     */
    String beforeField() default "NULL";

    /**
     * field name in database
     * @return field name
     */
    String field() default "";
}
