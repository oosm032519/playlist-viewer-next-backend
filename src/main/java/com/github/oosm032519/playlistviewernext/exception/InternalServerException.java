package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class InternalServerException extends PlaylistViewerNextException {

    public InternalServerException(String message) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }

    public InternalServerException(HttpStatus httpStatus, String details, Throwable cause) {
        super(httpStatus, details, cause);
    }

    public InternalServerException(final HttpStatus httpStatus, final String details, final Exception e) {
        super(httpStatus, details, e);
    }

    public InternalServerException(final HttpStatus httpStatus, final String details, final String s) {
        super(httpStatus, details, s);
    }
}
