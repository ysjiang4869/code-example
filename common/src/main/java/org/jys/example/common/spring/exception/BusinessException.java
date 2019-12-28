package org.jys.example.common.spring.exception;

/**
 * @author YueSong Jiang
 * @date 2019/3/16
 * Common exception for self-defined
 */
public class BusinessException extends RuntimeException {

    private final int errorCode;

    private final int responseStatus;

    private final String errorMessage;


    public BusinessException() {
        this(500);
    }

    public BusinessException(int errorCode) {
        this(errorCode, "unknown error");
    }

    public BusinessException(int errorCode, String errorMessage) {
        this(errorCode, errorMessage, 500);
    }

    public BusinessException(int errorCode, String errorMessage, int responseStatus) {
        this.errorCode = errorCode;
        this.responseStatus = responseStatus;
        this.errorMessage = errorMessage;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
