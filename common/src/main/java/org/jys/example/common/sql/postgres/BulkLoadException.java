package org.jys.example.common.sql.postgres;

/**
 * @author YueSong Jiang
 * @date 2020/2/3
 */
public class BulkLoadException extends RuntimeException {

    private final int errorCode;

    private final String errorMessage;

    private final transient Object errorData;

    public BulkLoadException(int errorCode, String errorMessage, Object errorData) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.errorData = errorData;
    }

    public Object getErrorData() {
        return this.errorData;
    }

    public int getErrorCode() {
        return this.errorCode;
    }

    public String getErrorMessage() {
        return this.errorMessage;
    }
}

