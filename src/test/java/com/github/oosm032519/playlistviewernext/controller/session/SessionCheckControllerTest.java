package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
import com.github.oosm032519.playlistviewernext.exception.ErrorResponse;
import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
public class SessionCheckControllerTest {

    @Autowired
    private SessionCheckController sessionCheckController;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCheckSession_authenticatedUser() throws Exception {
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:validSessionId")).thenReturn("validToken");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "userId123");
        claims.put("name", "John Doe");
        claims.put("spotify_access_token", "spotifyToken");
        when(jwtUtil.validateToken("validToken")).thenReturn(claims);

        ResponseEntity<?> responseEntity = sessionCheckController.checkSession(request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isInstanceOf(Map.class);
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertThat(responseBody).containsEntry("status", "success");
        assertThat(responseBody).containsEntry("userId", "userId123");
        assertThat(responseBody).containsEntry("userName", "John Doe");
        assertThat(responseBody).containsEntry("spotifyAccessToken", "spotifyToken");

        verify(jwtUtil).validateToken("validToken");
        verify(redisTemplate.opsForValue()).get("session:validSessionId");
    }

    @Test
    public void testCheckSession_unauthenticatedUser_noCookies() {
        when(request.getCookies()).thenReturn(null);

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> sessionCheckController.checkSession(request));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getErrorCode()).isEqualTo("SESSION_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("セッションが有効期限切れか、無効です。再度ログインしてください。");
    }

    @Test
    public void testCheckSession_unauthenticatedUser_invalidSessionId() {
        Cookie cookie = new Cookie("sessionId", "invalidSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:invalidSessionId")).thenReturn(null);

        ResponseEntity<?> responseEntity = sessionCheckController.checkSession(request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
        assertThat(errorResponse.getErrorCode()).isEqualTo("SESSION_NOT_FOUND");

        verify(redisTemplate.opsForValue()).get("session:invalidSessionId");
    }

    @Test
    public void testCheckSession_validationError() throws Exception {
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:validSessionId")).thenReturn("validToken");

        when(jwtUtil.validateToken("validToken")).thenThrow(new RuntimeException("Token validation error"));

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> sessionCheckController.checkSession(request));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getErrorCode()).isEqualTo("SESSION_VALIDATION_ERROR");
        assertThat(exception.getMessage()).isEqualTo("セッションの検証中にエラーが発生しました。再度ログインしてください。");
    }

    @Test
    public void testLogout_successful() {
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(redisTemplate.delete("session:validSessionId")).thenReturn(true);

        ResponseEntity<?> responseEntity = sessionCheckController.logout(request, response);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody()).isInstanceOf(Map.class);
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertThat(responseBody).containsEntry("status", "success");
        assertThat(responseBody).containsEntry("message", "ログアウトしました。");

        verify(redisTemplate).delete("session:validSessionId");
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    public void testLogout_noSessionId() {
        when(request.getCookies()).thenReturn(null);

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> sessionCheckController.logout(request, response));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getErrorCode()).isEqualTo("SESSION_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("有効なセッションIDが存在しません。");
    }

    @Test
    public void testLogout_sessionNotFoundInRedis() {
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(redisTemplate.delete("session:validSessionId")).thenReturn(false);

        ResponseEntity<?> responseEntity = sessionCheckController.logout(request, response);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(responseEntity.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
        assertThat(errorResponse.getErrorCode()).isEqualTo("SESSION_NOT_FOUND");
        assertThat(errorResponse.getMessage()).isEqualTo("ログアウト処理中にエラーが発生しました。再度お試しください。");

        verify(redisTemplate).delete("session:validSessionId");
    }

    @Test
    public void testLogout_redisException() {
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doThrow(new RuntimeException("Redis error")).when(redisTemplate).delete("session:validSessionId");

        DatabaseAccessException exception = assertThrows(DatabaseAccessException.class,
                () -> sessionCheckController.logout(request, response));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getErrorCode()).isEqualTo("LOGOUT_ERROR");
        assertThat(exception.getMessage()).isEqualTo("ログアウト処理中にエラーが発生しました。再度お試しください。");
    }

    @Test
    public void testCheckSession_authenticationException() throws Exception {
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:validSessionId")).thenReturn("validToken");

        when(jwtUtil.validateToken("validToken")).thenThrow(new AuthenticationException(HttpStatus.UNAUTHORIZED, "TEST_ERROR", "Test error"));

        ResponseEntity<?> responseEntity = sessionCheckController.checkSession(request);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(responseEntity.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
        assertThat(errorResponse.getErrorCode()).isEqualTo("TEST_ERROR");
        assertThat(errorResponse.getMessage()).isEqualTo("Test error");
    }
}
