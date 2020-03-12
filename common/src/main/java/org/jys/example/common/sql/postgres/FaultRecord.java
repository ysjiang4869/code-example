package org.jys.example.common.sql.postgres;

/**
 * @author YueSong Jiang
 * @date 2020/3/7
 */
public class FaultRecord {

    private String id;

    private String table;

    private String data;

    private String errorMessage;

    private long time;

    private int isReload;

    public FaultRecord(String table, String data, String errorMessage) {
        this.table = table;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getIsReload() {
        return isReload;
    }

    public void setIsReload(int isReload) {
        this.isReload = isReload;
    }
}
