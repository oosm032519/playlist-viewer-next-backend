package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class SpotifyApiException extends PlaylistViewerNextException {
    public SpotifyApiException(HttpStatus httpStatus, String errorCode, String message, Throwable cause) {
        super(httpStatus, errorCode, message, cause);
    }

    public SpotifyApiException(HttpStatus httpStatus, String errorCode, String message, String details) {
        super(httpStatus, errorCode, message, details);
    }
}
