package com.github.oosm032519.playlistviewernext.service.analytics;

/**
 * プレイリストの分析中に発生する例外を表します。
 */
public class PlaylistAnalyticsException extends Exception {

    /**
     * コンストラクタ
     *
     * @param message エラーメッセージ
     * @param cause   原因となった例外
     */
    public PlaylistAnalyticsException(String message, Throwable cause) {
        super(message, cause);
    }
}
