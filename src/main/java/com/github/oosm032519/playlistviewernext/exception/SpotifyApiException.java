package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class SpotifyApiException extends PlaylistViewerNextException {

    public SpotifyApiException(HttpStatus httpStatus, String errorCode, String message, Throwable cause) {
        super(httpStatus, errorCode, message, cause);
    }

    public SpotifyApiException(HttpStatus httpStatus, String errorCode, String message, String details) {
        super(httpStatus, errorCode, message, details);
    }

    public SpotifyApiException(HttpStatus httpStatus, String errorCode, String message, String details, Throwable cause) {
        super(httpStatus, errorCode, message, details, cause);
    }

    public SpotifyApiException(int status, String errorCode, String message, Throwable cause) {
        super(convertStatusToHttpStatus(status), errorCode, message, cause);
    }

    private static HttpStatus convertStatusToHttpStatus(int status) {
        try {
            return HttpStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            return HttpStatus.INTERNAL_SERVER_ERROR;
        }
    }
}
