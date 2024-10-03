package com.github.oosm032519.playlistviewernext.util;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Objects;

public class RetryUtil {

    public static final long DEFAULT_RETRY_INTERVAL_MILLIS = 5000; // デフォルトの再試行間隔（ミリ秒）
    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);

    public static <T> T executeWithRetry(RetryableOperation<T> operation, int maxRetries, long initialIntervalMillis) {
        int retryCount = 0;
        long intervalMillis = initialIntervalMillis;

        while (true) {
            try {
                return operation.execute();
            } catch (HttpClientErrorException httpException) {
                if (retryCount < maxRetries) {
                    // Retry-Afterヘッダーがあればその値を使用
                    String retryAfterHeader = Objects.requireNonNull(httpException.getResponseHeaders()).getFirst("Retry-After");
                    if (retryAfterHeader != null) {
                        try {
                            intervalMillis = Long.parseLong(retryAfterHeader) * 1000; // 秒をミリ秒に変換
                        } catch (NumberFormatException ex) {
                            logger.warn("Retry-After ヘッダーの値が数値に変換できません: {}. デフォルトのインターバルを使用します。", retryAfterHeader);
                            intervalMillis = DEFAULT_RETRY_INTERVAL_MILLIS;
                        }
                    } else {
                        intervalMillis = DEFAULT_RETRY_INTERVAL_MILLIS; // Retry-After ヘッダーがない場合はデフォルトを使用
                    }

                    logger.warn("Spotify APIエラーが発生しました。{}秒後に再試行します... (試行回数: {})", intervalMillis / 1000, retryCount + 1);
                    try {
                        Thread.sleep(intervalMillis);
                    } catch (InterruptedException ex) {
                        // 割り込みが発生した場合は、InternalServerExceptionにラップしてスロー
                        Thread.currentThread().interrupt();
                        throw new InternalServerException(
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                "再試行が中断されました。",
                                ex
                        );
                    }

                    intervalMillis *= 2; // 指数バックオフ
                    retryCount++;
                } else {
                    // 再試行条件を満たさない場合は、例外をそのままスロー
                    throw httpException;
                }
            }
        }
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws HttpClientErrorException;
    }
}
