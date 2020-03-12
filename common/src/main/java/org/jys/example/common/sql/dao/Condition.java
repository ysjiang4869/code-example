package org.jys.example.common.sql.dao;

/**
 * @author YueSong Jiang
 * @date 2020/2/3
 */
public class Condition {
    private String key;
    private String operator;
    private Object value;

    public Condition() {
    }

    public Condition(String key, String operator, Object value) {
        this.key = key;
        this.operator = operator;
        this.value = value;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOperator() {
        return this.operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return this.value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
