package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends PlaylistViewerNextException {
    public InvalidRequestException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }

    public InvalidRequestException(final HttpStatus httpStatus, final String message, final IllegalArgumentException e) {
        super(httpStatus, message, e);
    }

    public InvalidRequestException(final HttpStatus httpStatus, final String message, final Exception e) {
        super(httpStatus, message, e);
    }
}
