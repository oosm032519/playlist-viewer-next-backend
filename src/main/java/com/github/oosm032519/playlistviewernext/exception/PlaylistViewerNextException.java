package com.github.oosm032519.playlistviewernext.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * PlaylistViewerNextアプリケーションの基本例外クラス。
 * HTTPステータス、エラーコード、詳細情報を含むカスタム例外を提供する。
 */
@Getter
public class PlaylistViewerNextException extends RuntimeException {

    /**
     * HTTPレスポンスステータス
     */
    private final HttpStatus httpStatus;

    /**
     * アプリケーション固有のエラーコード
     */
    private final String errorCode;

    /**
     * エラーの詳細情報
     */
    private final String details;

    /**
     * 基本的な例外を生成する。
     *
     * @param httpStatus HTTPステータスコード
     * @param message    エラーメッセージ
     * @param errorCode  アプリケーション固有のエラーコード
     */
    public PlaylistViewerNextException(HttpStatus httpStatus, String message, String errorCode) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        details = null;  // 詳細情報なし
    }

    /**
     * 原因となる例外を含む例外を生成する。
     *
     * @param httpStatus HTTPステータスコード
     * @param message    エラーメッセージ
     * @param errorCode  アプリケーション固有のエラーコード
     * @param cause      原因となる例外
     */
    public PlaylistViewerNextException(HttpStatus httpStatus, String message, String errorCode, Throwable cause) {
        super(message, cause);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        details = cause.getMessage();  // 原因となる例外のメッセージを詳細情報として使用
    }

    /**
     * 詳細情報を指定して例外を生成する。
     *
     * @param httpStatus HTTPステータスコード
     * @param message    エラーメッセージ
     * @param errorCode  アプリケーション固有のエラーコード
     * @param details    エラーの詳細情報
     */
    public PlaylistViewerNextException(HttpStatus httpStatus, String message, String errorCode, String details) {
        super(message);
        this.httpStatus = httpStatus;
        this.errorCode = errorCode;
        this.details = details;
    }
}
