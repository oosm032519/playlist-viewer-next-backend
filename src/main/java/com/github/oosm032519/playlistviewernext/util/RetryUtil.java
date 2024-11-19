package com.github.oosm032519.playlistviewernext.util;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.TooManyRequestsException;

/**
 * Spotify APIへのリクエスト実行時のリトライ処理を提供するユーティリティクラス。
 * レート制限に対するリトライロジックを実装し、指数バックオフを使用して再試行を行う。
 */
public class RetryUtil {

    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);

    /**
     * デフォルトのリトライ間隔（ミリ秒）
     */
    public static final long DEFAULT_RETRY_INTERVAL_MILLIS = 5000;

    /**
     * リトライ可能な操作を実行し、必要に応じて再試行を行う。
     *
     * @param operation             実行する操作
     * @param maxRetries            最大リトライ回数
     * @param initialIntervalMillis 初期リトライ間隔（ミリ秒）
     * @param <T>                   戻り値の型
     * @return 操作の実行結果
     * @throws SpotifyWebApiException  Spotify API呼び出し時の例外
     * @throws InternalServerException リトライ処理が中断された場合の例外
     */
    public static <T> T executeWithRetry(RetryableOperation<T> operation, int maxRetries, long initialIntervalMillis) throws SpotifyWebApiException {
        int retryCount = 0;
        long intervalMillis = initialIntervalMillis;

        while (true) {
            try {
                return operation.execute();
            } catch (SpotifyWebApiException e) {
                if (e instanceof TooManyRequestsException tooManyRequestsException) {
                    // レート制限に達した場合の処理
                    if (retryCount < maxRetries) {
                        // Spotify APIからのレスポンスに基づいてリトライ間隔を設定
                        int retryAfterSeconds = tooManyRequestsException.getRetryAfter();
                        intervalMillis = retryAfterSeconds * 1000L;
                        logger.warn("TooManyRequests: {}秒後に再試行します... (試行回数: {})", retryAfterSeconds, retryCount + 1);

                        try {
                            Thread.sleep(intervalMillis);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            throw new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "再試行が中断されました。", ex);
                        }

                        // 指数バックオフの適用
                        retryCount++;
                        intervalMillis *= 2;
                    } else {
                        throw e;
                    }
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * リトライ可能な操作を定義するための関数型インターフェース。
     *
     * @param <T> 操作の戻り値の型
     */
    @FunctionalInterface
    public interface RetryableOperation<T> {
        /**
         * リトライ可能な操作を実行する。
         *
         * @return 操作の実行結果
         * @throws SpotifyWebApiException Spotify API呼び出し時の例外
         */
        T execute() throws SpotifyWebApiException;
    }
}
