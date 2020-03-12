package org.jys.example.common.sql.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * @author YueSong Jiang
 * @date 2020/2/3
 */
public class QueryParam {

    private List<String> fields = new ArrayList();
    private String table;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private List<Condition> conditions = new ArrayList();
    private List<OrderInfo> orderInfo = new ArrayList();
    private boolean queryCount;

    public QueryParam() {
    }

    public boolean isQueryCount() {
        return this.queryCount;
    }

    public void setQueryCount(boolean queryCount) {
        this.queryCount = queryCount;
    }

    public Integer getPageNo() {
        return this.pageNo;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return this.pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public List<Condition> getConditions() {
        return this.conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<OrderInfo> getOrderInfo() {
        return this.orderInfo;
    }

    public void setOrderInfo(List<OrderInfo> orderInfo) {
        this.orderInfo = orderInfo;
    }

    public List<String> getFields() {
        return this.fields;
    }

    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    public String getTable() {
        return this.table;
    }

    public void setTable(String table) {
        this.table = table;
    }
}
