package org.jys.example.common.sql.query;

import org.jys.example.common.sql.dao.Condition;
import java.util.List;

/**
 * @author YueSong Jiang
 * @date 2020/3/12
 */
public class StructuredCountParam {

    protected Long startTime;
    protected Long endTime;
    protected List<String> deviceIds;
    protected List<String> fileIds;
    protected List<String> algorithms;
    protected List<String> algorithmVendors;
    protected List<Condition> conditions;


    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public List<String> getDeviceIds() {
        return this.deviceIds;
    }

    public void setDeviceIds(List<String> deviceIds) {
        this.deviceIds = deviceIds;
    }

    public List<Condition> getConditions() {
        return this.conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    public List<String> getAlgorithms() {
        return this.algorithms;
    }

    public void setAlgorithms(List<String> algorithms) {
        this.algorithms = algorithms;
    }

    public List<String> getAlgorithmVendors() {
        return this.algorithmVendors;
    }

    public void setAlgorithmVendors(List<String> algorithmVendors) {
        this.algorithmVendors = algorithmVendors;
    }

    public List<String> getFileIds() {
        return this.fileIds;
    }

    public void setFileIds(List<String> fileIds) {
        this.fileIds = fileIds;
    }

    @Override
    public String toString() {
        return "StructuredCountParam{startTime=" + this.startTime + ", endTime=" + this.endTime + ", deviceIds=" + this.deviceIds + ", algorithms=" + this.algorithms + ", algorithmVendors=" + this.algorithmVendors + ", conditions=" + this.conditions + '}';
    }
}
