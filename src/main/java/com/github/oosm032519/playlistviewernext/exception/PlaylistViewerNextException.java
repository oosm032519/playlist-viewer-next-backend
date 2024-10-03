package com.github.oosm032519.playlistviewernext.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PlaylistViewerNextException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String details;

    public PlaylistViewerNextException(HttpStatus httpStatus, String message) {
        super(message);
        this.httpStatus = httpStatus;
        details = null;
    }

    public PlaylistViewerNextException(HttpStatus httpStatus, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        details = getStackTraceAsString(cause);
    }

    // スタックトレースを文字列に変換するヘルパーメソッド
    private String getStackTraceAsString(Throwable throwable) {
        StackTraceElement[] stackTraceElements = throwable.getStackTrace();
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : stackTraceElements) {
            sb.append(element.toString()).append("\n");
        }
        return sb.toString();
    }

    public PlaylistViewerNextException(HttpStatus httpStatus, String message, String details) {
        super(message);
        this.httpStatus = httpStatus;
        this.details = details;
    }

    public PlaylistViewerNextException(HttpStatus httpStatus, String message, String details, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.details = details;
    }
}
