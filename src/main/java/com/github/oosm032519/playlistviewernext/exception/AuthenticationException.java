package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends PlaylistViewerNextException {
    public AuthenticationException(HttpStatus httpStatus, String errorCode, String message) {
        super(httpStatus, errorCode, message);
    }

    public AuthenticationException(HttpStatus httpStatus, String errorCode, String message, Throwable cause) {
        super(httpStatus, errorCode, message, cause);
    }
}
