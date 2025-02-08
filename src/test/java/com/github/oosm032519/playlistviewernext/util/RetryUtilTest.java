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

        /**
         * 最初の試行で操作が成功する場合、結果が正しく返され、再試行が行われないことを確認する。
         */
        @Test
        @DisplayName("正常系: 最初の試行で成功する場合")
        void successOnFirstAttempt() throws Exception {
            // Arrange: テストデータの準備
            String expectedResult = "success";
            RetryUtil.RetryableOperation<String> operation = () -> expectedResult;

            // Act: テスト対象メソッドの実行
            String result = RetryUtil.executeWithRetry(operation, 3, 100);

            // Assert: 結果の検証
            assertThat(result).isEqualTo(expectedResult);
        }

        /**
         * TooManyRequestsExceptionが発生し、リトライ後に操作が成功する場合、
         * 最終的に成功結果が返され、指定された回数リトライが行われることを確認する。
         */
        @Test
        @DisplayName("異常系: TooManyRequestsExceptionが発生し、リトライ後に成功する場合")
        void successAfterRetry() throws Exception {
            // Arrange: モックの設定
            RetryUtil.RetryableOperation<String> operation = mock(RetryUtil.RetryableOperation.class);
            TooManyRequestsException exception = mock(TooManyRequestsException.class);
            when(exception.getRetryAfter()).thenReturn(1);

            // 1回目は例外、2回目は成功するように設定
            when(operation.execute())
                    .thenThrow(exception)
                    .thenReturn("success");

            // Act: テスト対象メソッドの実行
            String result = RetryUtil.executeWithRetry(operation, 3, 100);

            // Assert: 結果の検証
            assertThat(result).isEqualTo("success");
            verify(operation, times(2)).execute();
        }

        /**
         * リトライ回数が上限に達しても操作が失敗し続ける場合、TooManyRequestsExceptionがスローされることを確認する。
         */
        @Test
        @DisplayName("異常系: 最大リトライ回数を超えた場合")
        void failureAfterMaxRetries() throws SpotifyWebApiException {
            // Arrange: モックの設定
            RetryUtil.RetryableOperation<String> operation = mock(RetryUtil.RetryableOperation.class);
            TooManyRequestsException exception = mock(TooManyRequestsException.class);
            when(exception.getRetryAfter()).thenReturn(1);

            try {
                when(operation.execute()).thenThrow(exception);

                // Act: テスト対象メソッドの実行
                RetryUtil.executeWithRetry(operation, 2, 100);
                fail("TooManyRequestsExceptionが発生するはずです");
            } catch (Exception e) {
                // Assert: 例外の検証
                assertThat(e).isInstanceOf(TooManyRequestsException.class);
                verify(operation, times(3)).execute(); // 初回 + 2回のリトライ
            }
        }

        /**
         * リトライ中にスレッドが割り込まれた場合、InternalServerExceptionがスローされることを確認する。
         */
        @Test
        @DisplayName("異常系: リトライ中に割り込みが発生した場合")
        void interruptedDuringRetry() throws Exception {
            // Arrange: モックの設定
            RetryUtil.RetryableOperation<String> operation = mock(RetryUtil.RetryableOperation.class);
            TooManyRequestsException exception = mock(TooManyRequestsException.class);
            when(exception.getRetryAfter()).thenReturn(1);

            // スレッドを割り込みするように設定
            when(operation.execute()).thenThrow(exception);
            Thread.currentThread().interrupt();

            // Act & Assert: InternalServerExceptionがスローされることの確認
            assertThatThrownBy(() -> RetryUtil.executeWithRetry(operation, 3, 100))
                    .isInstanceOf(InternalServerException.class)
                    .hasMessageContaining("再試行が中断されました。");
        }
    }
}
