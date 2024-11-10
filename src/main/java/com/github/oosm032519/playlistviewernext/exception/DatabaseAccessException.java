package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class DatabaseAccessException extends PlaylistViewerNextException {
    public DatabaseAccessException(HttpStatus httpStatus, String message, Throwable cause) {
        super(httpStatus, message, "DATABASE_ACCESS_ERROR", cause);
    }

    public DatabaseAccessException(HttpStatus httpStatus, String message) {
        super(httpStatus, message, "DATABASE_ACCESS_ERROR");
    }
}
