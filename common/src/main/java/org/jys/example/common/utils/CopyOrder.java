package org.jys.example.common.utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author jiangys
 * add to CopyIn data field
 * there musn't be same value for same class
 * it is recommend to use from 0 and use continuous number
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CopyOrder {
    public int value();
}
