package com.github.oosm032519.playlistviewernext.controller.session;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MockLoginControllerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private MockLoginController mockLoginController;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mockLoginController, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void mockLogin_success() {
        // Arrange
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        // putAll() は void を返すので、doNothing() を使用する
        doNothing().when(hashOperations).putAll(anyString(), anyMap());
        when(redisTemplate.expire(anyString(), anyLong(), any())).thenReturn(true);

        // Act
        ResponseEntity<?> responseEntity = mockLoginController.mockLogin(response);

        // Assert
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

    @Test
    void mockLogin_redisError() {
        // Arrange
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        // putAll() で例外をスローさせる場合は、doThrow() を使用する
        doThrow(new RuntimeException("Redis error")).when(hashOperations).putAll(anyString(), anyMap());

        // Act
        ResponseEntity<?> responseEntity = mockLoginController.mockLogin(response);

        // Assert
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
