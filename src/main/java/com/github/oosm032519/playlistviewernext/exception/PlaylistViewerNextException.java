package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class PlaylistViewerNextException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;

    public PlaylistViewerNextException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public PlaylistViewerNextException(HttpStatus httpStatus, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
