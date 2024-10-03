package com.github.oosm032519.playlistviewernext.util;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.TooManyRequestsException;

public class RetryUtil {

    private static final Logger logger = LoggerFactory.getLogger(RetryUtil.class);

    public static final long DEFAULT_RETRY_INTERVAL_MILLIS = 5000;

    public static <T> T executeWithRetry(RetryableOperation<T> operation, int maxRetries, long initialIntervalMillis) throws SpotifyWebApiException {
        int retryCount = 0;
        long intervalMillis = initialIntervalMillis;

        while (true) {
            try {
                return operation.execute();
            } catch (SpotifyWebApiException e) {
                if (e instanceof TooManyRequestsException tooManyRequestsException) {
                    if (retryCount < maxRetries) {
                        int retryAfterSeconds = tooManyRequestsException.getRetryAfter();
                        intervalMillis = retryAfterSeconds * 1000L; // 秒をミリ秒に変換
                        logger.warn("TooManyRequests: {}秒後に再試行します... (試行回数: {})", retryAfterSeconds, retryCount + 1);
                        try {
                            Thread.sleep(intervalMillis);
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                            throw new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "再試行が中断されました。", ex);
                        }
                        retryCount++;
                        intervalMillis *= 2; // 指数バックオフ (必要に応じて)
                    } else {
                        throw e; // 再試行回数を超えた場合は例外をスロー
                    }
                } else {
                    throw e; // TooManyRequestsException以外の場合は例外をスロー
                }
            }
        }
    }

    @FunctionalInterface
    public interface RetryableOperation<T> {
        T execute() throws SpotifyWebApiException;
    }
}
