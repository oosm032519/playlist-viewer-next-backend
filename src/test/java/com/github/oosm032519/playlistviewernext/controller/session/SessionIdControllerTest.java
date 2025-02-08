package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SessionIdControllerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SessionIdController sessionIdController;

    /**
     * 一時トークンが提供され、対応するセッションIDがRedisに存在する場合、セッションIDが返され、一時トークンが削除されることを確認する。
     */
    @Test
    void getSessionId_temporaryTokenExists_returnsSessionId() {
        // Arrange: テストデータの準備
        String temporaryToken = "testToken";
        String sessionId = "testSessionId";
        Map<String, String> requestBody = Map.of("temporaryToken", temporaryToken);

        // Redisのモック設定
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("temp:" + temporaryToken)).thenReturn(sessionId);
        when(redisTemplate.delete("temp:" + temporaryToken)).thenReturn(true);

        // Act: テスト対象メソッドの実行
        ResponseEntity<?> response = sessionIdController.getSessionId(requestBody);

        // Assert: レスポンスの検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("sessionId", sessionId));

        // Redisへのアクセス検証
        verify(valueOperations, times(1)).get("temp:" + temporaryToken);
        verify(redisTemplate, times(1)).delete("temp:" + temporaryToken);
    }

    /**
     * 一時トークンがリクエストボディに含まれていない場合、InvalidRequestExceptionがスローされることを確認する。
     */
    @Test
    void getSessionId_temporaryTokenMissing_throwsInvalidRequestException() {
        // Arrange: テストデータの準備
        Map<String, String> requestBody = Map.of(); // temporaryTokenがない

        // Act & Assert: テスト対象メソッドの実行と例外検証
        // JUnitのassertThrowsからAssertJのassertThatThrownByに変更
        assertThatThrownBy(() -> sessionIdController.getSessionId(requestBody))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("ログイン処理中にエラーが発生しました。再度ログインしてください。")
                .extracting("httpStatus").isEqualTo(HttpStatus.BAD_REQUEST);

        // Redisへのアクセスがないことを検証
        verify(redisTemplate, never()).opsForValue();
    }

    /**
     * 一時トークンに対応するセッションIDがRedisに見つからない場合、DatabaseAccessExceptionがスローされることを確認する。
     */
    @Test
    void getSessionId_sessionIdNotFound_throwsDatabaseAccessException() {
        // Arrange: テストデータの準備
        String temporaryToken = "testToken";
        Map<String, String> requestBody = Map.of("temporaryToken", temporaryToken);

        // Redisのモック設定
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("temp:" + temporaryToken)).thenReturn(null); // sessionIdが見つからない

        // Act & Assert: テスト対象メソッドの実行と例外検証
        // JUnitのassertThrowsからAssertJのassertThatThrownByに変更
        assertThatThrownBy(() -> sessionIdController.getSessionId(requestBody))
                .isInstanceOf(DatabaseAccessException.class)
                .hasMessageContaining("ログイン処理中にエラーが発生しました。再度ログインしてください。")
                .extracting("httpStatus").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        // Redisへのアクセス検証 (deleteは呼ばれない)
        verify(valueOperations, times(1)).get("temp:" + temporaryToken);
        verify(redisTemplate, never()).delete(anyString());
    }
}
