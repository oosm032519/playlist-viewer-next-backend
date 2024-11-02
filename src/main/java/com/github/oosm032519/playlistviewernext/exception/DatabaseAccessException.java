package com.github.oosm032519.playlistviewernext.exception;

import org.springframework.http.HttpStatus;

public class DatabaseAccessException extends PlaylistViewerNextException {
    public DatabaseAccessException(HttpStatus httpStatus, String message, Throwable cause) {
        super(httpStatus, message, "FAVORITE_PLAYLISTS_RETRIEVAL_ERROR", cause); // errorCode を設定
    }

    public DatabaseAccessException(HttpStatus httpStatus, String message) {
        super(httpStatus, message, "FAVORITE_PLAYLISTS_RETRIEVAL_ERROR", null); // errorCode を設定、cause は null
    }

    public String getErrorCode() {
        return super.getErrorCode();
    }
}
