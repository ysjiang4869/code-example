package org.jys.common.spring.exception;

/**
 * @author YueSong Jiang
 * @date 2019/3/16
 * @description <p> </p>
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
