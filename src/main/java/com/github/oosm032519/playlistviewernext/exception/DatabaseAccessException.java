package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class DatabaseAccessException extends PlaylistViewerNextException {
    public DatabaseAccessException(HttpStatus httpStatus, String errorCode, String message) {
        super(httpStatus, errorCode, message);
    }

    public DatabaseAccessException(HttpStatus httpStatus, String errorCode, String message, Throwable cause) {
        super(httpStatus, errorCode, message, cause);
    }
}
