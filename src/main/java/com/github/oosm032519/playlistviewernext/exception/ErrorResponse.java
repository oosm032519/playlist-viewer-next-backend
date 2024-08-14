package com.github.oosm032519.playlistviewernext.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ErrorResponse {
    private final HttpStatus status;
    private final String errorCode;
    private final String message;
    private final String details;

    public ErrorResponse(HttpStatus status, String errorCode, String message) {
        this(status, errorCode, message, null);
    }

    public ErrorResponse(HttpStatus status, String errorCode, String message, String details) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
    }

}
