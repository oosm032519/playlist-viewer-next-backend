package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends PlaylistViewerNextException {
    public AuthenticationException(HttpStatus httpStatus, String message) {
        super(httpStatus, message, "AUTHENTICATION_ERROR");
    }

    public AuthenticationException(HttpStatus httpStatus, String message, Throwable cause) {
        super(httpStatus, message, "AUTHENTICATION_ERROR", cause);
    }
}
