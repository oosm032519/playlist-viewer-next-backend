package com.github.oosm032519.playlistviewernext.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PlaylistViewerNextException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String details;

    private String getStackTraceAsString(Throwable throwable) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTraceElements) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

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
        details = getStackTraceAsString(cause);
    }

    // 詳細情報なし、エラーコードのみ
    public PlaylistViewerNextException(HttpStatus httpStatus, String message, String errorCode, String details) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = details;
    }
}
