package com.github.oosm032519.playlistviewernext.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PlaylistViewerNextException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String details;

    public PlaylistViewerNextException(HttpStatus httpStatus, String message, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        details = null;
    }

    public PlaylistViewerNextException(HttpStatus httpStatus, String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        details = cause.getMessage();
    }

    // 詳細情報なし、エラーコードのみ
    public PlaylistViewerNextException(HttpStatus httpStatus, String message, String errorCode, String details) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = details;
    }
}
