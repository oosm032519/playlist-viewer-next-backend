package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class AuthenticationException extends PlaylistViewerNextException {
    public AuthenticationException(HttpStatus httpStatus, String message, Throwable cause) {
        super(httpStatus, message, cause);
    }

    public AuthenticationException(final HttpStatus httpStatus, final String authenticationError, final String s) {
        super(httpStatus, authenticationError, s);
    }
}
