package org.jys.example.common.sql.dao;

/**
 * @author YueSong Jiang
 * @date 2020/2/3
 */
public class OrderInfo {
    private String key;
    private String order;

    public OrderInfo(String key, String order) {
        this.key = key;
        this.order = order;
    }

    public OrderInfo() {
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getOrder() {
        return this.order;
    }

    public void setOrder(String order) {
        this.order = order;
    }
}
