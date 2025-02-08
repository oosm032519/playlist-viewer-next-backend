package com.github.oosm032519.playlistviewernext.controller.session;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestPropertySource(properties = {"spotify.mock.enabled=true"}) // モックモードを有効にする
class MockLoginControllerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private MockLoginController mockLoginController;

    @Value("${frontend.url}") // frontend.url を注入
    public String frontendUrl;

    /**
     * モックログインが成功した場合、セッションIDを含むレスポンスが返され、
     * セッション情報がRedisに保存され、Cookieが設定されることを確認する。
     */
    @Test
    void mockLogin_success() {
        // Arrange: モックの設定
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        doNothing().when(hashOperations).putAll(anyString(), anyMap());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act: テスト対象メソッドの実行
        ResponseEntity<?> responseEntity = mockLoginController.mockLogin(response);

        // Assert: レスポンスの検証
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertThat(responseBody)
                .containsEntry("status", "success")
                .containsEntry("message", "モックログインに成功しました。")
                .containsKey("sessionId")
                .containsKey("userId")
                .containsKey("userName");

        verify(redisTemplate.opsForHash(), times(1)).putAll(anyString(), anyMap());
        verify(redisTemplate, times(1)).expire(anyString(), eq(3600L), eq(TimeUnit.SECONDS));
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    /**
     * Redisへの操作中にエラーが発生した場合、INTERNAL_SERVER_ERRORが返されることを確認する。
     */
    @Test
    void mockLogin_redisError() {
        // Arrange: モックの設定 (Redis操作中に例外をスロー)
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        doThrow(new RuntimeException("Redis error")).when(hashOperations).putAll(anyString(), anyMap());

        // Act: テスト対象メソッドの実行
        ResponseEntity<?> responseEntity = mockLoginController.mockLogin(response);

        // Assert: レスポンスの検証
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertThat(responseBody)
                .containsEntry("status", "error")
                .containsEntry("message", "モックログイン処理中にエラーが発生しました。");

        verify(redisTemplate.opsForHash(), times(1)).putAll(anyString(), anyMap());
        verify(redisTemplate, never()).expire(anyString(), anyLong(), any());
        verify(response, never()).addCookie(any(Cookie.class));
    }
}
