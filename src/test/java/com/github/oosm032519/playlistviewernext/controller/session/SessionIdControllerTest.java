package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void getSessionId_ValidTemporaryToken_ReturnsSessionId() {
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
        assertThat(response.getBody()).isInstanceOf(Map.class);
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertThat(responseBody).containsEntry("sessionId", sessionId);

        verify(valueOperations).get("temp:" + temporaryToken);
        verify(redisTemplate).delete("temp:" + temporaryToken);
    }

    @Test
    void getSessionId_MissingTemporaryToken_ThrowsInvalidRequestException() {
        // Arrange
        Map<String, String> requestBody = Map.of();

        // Act & Assert
        assertThatThrownBy(() -> sessionIdController.getSessionId(requestBody))
                .isInstanceOf(InvalidRequestException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.BAD_REQUEST)
                .hasFieldOrPropertyWithValue("errorCode", "TEMPORARY_TOKEN_MISSING")
                .hasMessage("一時トークンが提供されていません。");
    }

    @Test
    void getSessionId_SessionIdNotFound_ThrowsDatabaseAccessException() {
        // Arrange
        String temporaryToken = "invalidToken";
        Map<String, String> requestBody = Map.of("temporaryToken", temporaryToken);

        when(valueOperations.get("temp:" + temporaryToken)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> sessionIdController.getSessionId(requestBody))
                .isInstanceOf(DatabaseAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
                .hasFieldOrPropertyWithValue("errorCode", "REDIS_ACCESS_ERROR")
                .hasMessage("Redisアクセス中にエラーが発生しました。");
    }

    @Test
    void getSessionId_RedisAccessError_ThrowsDatabaseAccessException() {
        // Arrange
        String temporaryToken = "validToken";
        Map<String, String> requestBody = Map.of("temporaryToken", temporaryToken);

        when(valueOperations.get("temp:" + temporaryToken)).thenThrow(new RuntimeException("Redis connection error"));

        // Act & Assert
        assertThatThrownBy(() -> sessionIdController.getSessionId(requestBody))
                .isInstanceOf(DatabaseAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
                .hasFieldOrPropertyWithValue("errorCode", "REDIS_ACCESS_ERROR")
                .hasMessage("Redisアクセス中にエラーが発生しました。");
    }
}
