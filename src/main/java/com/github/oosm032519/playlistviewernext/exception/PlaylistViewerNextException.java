package com.github.oosm032519.playlistviewernext.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class PlaylistViewerNextException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String errorCode;
    private final String details; // 詳細なエラー情報

    public PlaylistViewerNextException(HttpStatus httpStatus, String errorCode, String message) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = null;
    }

    public PlaylistViewerNextException(HttpStatus httpStatus, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = getStackTraceAsString(cause); // スタックトレースを詳細情報として保存
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

    public PlaylistViewerNextException(HttpStatus httpStatus, String errorCode, String message, String details) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = details;
    }
}
