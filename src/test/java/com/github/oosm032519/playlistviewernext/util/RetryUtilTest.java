package com.github.oosm032519.playlistviewernext.util;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.TooManyRequestsException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class RetryUtilTest {

    @Nested
    @DisplayName("executeWithRetryメソッドのテスト")
    class ExecuteWithRetryTest {

        @Test
        @DisplayName("正常系: 最初の試行で成功する場合")
        void successOnFirstAttempt() throws Exception {
            // テストデータ
            String expectedResult = "success";
            RetryUtil.RetryableOperation<String> operation = () -> expectedResult;

            // 実行
            String result = RetryUtil.executeWithRetry(operation, 3, 100);

            // 検証
            assertThat(result).isEqualTo(expectedResult);
        }

        @Test
        @DisplayName("異常系: TooManyRequestsExceptionが発生し、リトライ後に成功する場合")
        void successAfterRetry() throws Exception {
            // モックの設定
            RetryUtil.RetryableOperation<String> operation = mock(RetryUtil.RetryableOperation.class);
            TooManyRequestsException exception = mock(TooManyRequestsException.class);
            when(exception.getRetryAfter()).thenReturn(1);

            // 1回目は例外、2回目は成功するように設定
            when(operation.execute())
                    .thenThrow(exception)
                    .thenReturn("success");

            // 実行
            String result = RetryUtil.executeWithRetry(operation, 3, 100);

            // 検証
            assertThat(result).isEqualTo("success");
            verify(operation, times(2)).execute();
        }

        @Test
        @DisplayName("異常系: 最大リトライ回数を超えた場合")
        void failureAfterMaxRetries() throws SpotifyWebApiException {
            // モックの設定
            RetryUtil.RetryableOperation<String> operation = mock(RetryUtil.RetryableOperation.class);
            TooManyRequestsException exception = mock(TooManyRequestsException.class);
            when(exception.getRetryAfter()).thenReturn(1);

            try {
                when(operation.execute()).thenThrow(exception);

                // 実行
                RetryUtil.executeWithRetry(operation, 2, 100);
                fail("TooManyRequestsExceptionが発生するはずです");
            } catch (Exception e) {
                // 検証
                assertThat(e).isInstanceOf(TooManyRequestsException.class);
                verify(operation, times(3)).execute(); // 初回 + 2回のリトライ
            }
        }

        @Test
        @DisplayName("異常系: リトライ中に割り込みが発生した場合")
        void interruptedDuringRetry() throws Exception {
            // モックの設定
            RetryUtil.RetryableOperation<String> operation = mock(RetryUtil.RetryableOperation.class);
            TooManyRequestsException exception = mock(TooManyRequestsException.class);
            when(exception.getRetryAfter()).thenReturn(1);

            // スレッドを割り込みするように設定
            when(operation.execute()).thenThrow(exception);
            Thread.currentThread().interrupt();

            // 実行と検証
            assertThatThrownBy(() -> RetryUtil.executeWithRetry(operation, 3, 100))
                    .isInstanceOf(InternalServerException.class)
                    .hasMessageContaining("再試行が中断されました。");
        }
    }
}
