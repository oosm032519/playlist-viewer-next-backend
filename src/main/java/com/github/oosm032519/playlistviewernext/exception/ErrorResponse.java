package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class ErrorResponse {
    private final HttpStatus status;
    private final String errorCode;
    private final String message;

    public ErrorResponse(HttpStatus status, String errorCode, String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
    }

    // ゲッター
    public HttpStatus getStatus() {
        return status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
