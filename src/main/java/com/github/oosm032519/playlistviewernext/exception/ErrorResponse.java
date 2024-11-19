package com.github.oosm032519.playlistviewernext.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * APIエラーレスポンスを表現するクラス。
 * HTTPステータス、エラーコード、メッセージ、詳細情報を含むエラーレスポンスを生成する。
 */
@Getter
public class ErrorResponse {
    /**
     * HTTPステータスコード
     */
    private final HttpStatus status;

    /**
     * エラーメッセージ
     */
    private final String message;

    /**
     * エラーを識別するための一意のコード
     */
    private final String errorCode;

    /**
     * エラーの詳細情報（オプション）
     */
    private final String details;

    /**
     * 詳細情報なしでErrorResponseを生成するコンストラクタ。
     *
     * @param status    HTTPステータス
     * @param errorCode エラーコード
     * @param message   エラーメッセージ
     */
    public ErrorResponse(HttpStatus status, String errorCode, String message) {
        this(status, errorCode, message, null);
    }

    /**
     * 完全なエラー情報を含むErrorResponseを生成するコンストラクタ。
     *
     * @param status    HTTPステータス
     * @param errorCode エラーコード
     * @param message   エラーメッセージ
     * @param details   エラーの詳細情報
     */
    public ErrorResponse(HttpStatus status, String errorCode, String message, String details) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.details = details;
    }

    /**
     * エラーレスポンスをJSON形式の文字列に変換する。
     *
     * @return JSON形式のエラーレスポンス文字列
     */
    @Override
    public String toString() {
        // JSON形式でエラー情報を構築
        return "{" +
                "\"status\":\"" + status + "\"" +
                ",\"errorCode\":\"" + errorCode + "\"" +
                ",\"message\":\"" + message + "\"" +
                ",\"details\":\"" + details + "\"" +
                "}";
    }
}
