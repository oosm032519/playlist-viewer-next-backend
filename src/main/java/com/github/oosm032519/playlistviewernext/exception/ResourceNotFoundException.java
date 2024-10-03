package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends PlaylistViewerNextException {
    public ResourceNotFoundException(HttpStatus httpStatus, String message) {
        super(httpStatus, message);
    }
}
