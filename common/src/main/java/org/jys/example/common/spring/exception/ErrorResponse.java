package org.jys.example.common.spring.exception;

/**
 * @author YueSong Jiang
 * @date 2019/3/16
 * Common error response
 */
public class ErrorResponse {

    private int errorCode;

    private String message;

    public ErrorResponse() {

    }

    public ErrorResponse(int errorCode) {
        this.errorCode = errorCode;
    }

    public ErrorResponse(int errorCode, String message) {
        this.errorCode = errorCode;
        this.message = message;
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
