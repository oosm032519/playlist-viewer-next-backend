package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class InvalidRequestException extends PlaylistViewerNextException {
    public InvalidRequestException(HttpStatus httpStatus, String errorCode, String message) {
        super(httpStatus, errorCode, message);
    }
}
