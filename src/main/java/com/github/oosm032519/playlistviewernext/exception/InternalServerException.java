package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends PlaylistViewerNextException {
    public InternalServerException(HttpStatus httpStatus, String message, Throwable cause) {
        super(httpStatus, message, "INTERNAL_SERVER_ERROR", cause);
    }

    public InternalServerException(HttpStatus httpStatus, String message) {
        super(httpStatus, message, "INTERNAL_SERVER_ERROR");
    }
}
