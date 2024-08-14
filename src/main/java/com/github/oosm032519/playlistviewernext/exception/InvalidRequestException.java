package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends PlaylistViewerNextException {
    public InvalidRequestException(HttpStatus httpStatus, String errorCode, String message) {
        super(httpStatus, errorCode, message);
    }

    public InvalidRequestException(final HttpStatus httpStatus, final String tokenValidationError, final String message, final IllegalArgumentException e) {
        super(httpStatus, tokenValidationError, message, e);
    }

    public InvalidRequestException(final HttpStatus httpStatus, final String tokenValidationError, final String message, final Exception e) {
        super(httpStatus, tokenValidationError, message, e);
    }
}
