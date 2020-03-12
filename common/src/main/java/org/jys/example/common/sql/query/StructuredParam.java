package org.jys.example.common.sql.query;


import org.jys.example.common.sql.dao.OrderInfo;

/**
 * @author YueSong Jiang
 * @date 2020/3/12
 */
public class StructuredParam extends StructuredCountParam {


    private Integer pageNo;
    private Integer pageSize;
    private OrderInfo orderInfo;


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


    public OrderInfo getOrderInfo() {
        return this.orderInfo;
    }

    public void setOrderInfo(OrderInfo orderInfo) {
        this.orderInfo = orderInfo;
    }


    @Override
    public String toString() {
        return "StructuredParam{startTime=" + this.startTime + ", endTime=" + this.endTime + ", deviceIds=" + this.deviceIds + ", algorithms=" + this.algorithms + ", algorithmVendors=" + this.algorithmVendors + ", pageNo=" + this.pageNo + ", pageSize=" + this.pageSize + ", conditions=" + this.conditions + ", orderInfo=" + this.orderInfo + '}';
    }
}
