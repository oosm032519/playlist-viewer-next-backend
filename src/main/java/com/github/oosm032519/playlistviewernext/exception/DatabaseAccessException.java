package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class DatabaseAccessException extends PlaylistViewerNextException {
    public DatabaseAccessException(HttpStatus httpStatus, String message, Throwable cause) {
        super(httpStatus, message, cause);
    }

    public DatabaseAccessException(final HttpStatus httpStatus, final String errorCode) {
        super(httpStatus, errorCode);
    }
}
