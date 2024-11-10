package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends PlaylistViewerNextException {
    public InvalidRequestException(HttpStatus httpStatus, String message) {
        super(httpStatus, message, "INVALID_REQUEST");
    }

    public InvalidRequestException(HttpStatus httpStatus, String message, Throwable cause) {
        super(httpStatus, message, "INVALID_REQUEST", cause);
    }
}
