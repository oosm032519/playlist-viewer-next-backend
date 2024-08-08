package com.github.oosm032519.playlistviewernext.controller.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class SessionIdControllerTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SessionIdController sessionIdController;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void getSessionId_WithValidTemporaryToken_ReturnsSessionId() {
        // Arrange
        String temporaryToken = "validToken";
        String sessionId = "sessionId123";
        Map<String, String> requestBody = Map.of("temporaryToken", temporaryToken);

        when(valueOperations.get("temp:" + temporaryToken)).thenReturn(sessionId);
        when(redisTemplate.delete("temp:" + temporaryToken)).thenReturn(true);

        // Act
        ResponseEntity<?> response = sessionIdController.getSessionId(requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("sessionId", sessionId));

        verify(valueOperations).get("temp:" + temporaryToken);
        verify(redisTemplate).delete("temp:" + temporaryToken);
    }

    @Test
    void getSessionId_WithMissingTemporaryToken_ReturnsBadRequest() {
        // Arrange
        Map<String, String> requestBody = Map.of();

        // Act
        ResponseEntity<?> response = sessionIdController.getSessionId(requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isEqualTo("一時トークンが提供されていません");

        verifyNoInteractions(valueOperations);
        verifyNoInteractions(redisTemplate);
    }

    @Test
    void getSessionId_WithInvalidTemporaryToken_ReturnsNotFound() {
        // Arrange
        String temporaryToken = "invalidToken";
        Map<String, String> requestBody = Map.of("temporaryToken", temporaryToken);

        when(valueOperations.get("temp:" + temporaryToken)).thenReturn(null);

        // Act
        ResponseEntity<?> response = sessionIdController.getSessionId(requestBody);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        verify(valueOperations).get("temp:" + temporaryToken);
    }
}
