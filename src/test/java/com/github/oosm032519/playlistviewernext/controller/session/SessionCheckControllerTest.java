package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
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

        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.checkSession(request);

        assertThat(responseEntity.getBody()).containsEntry("status", "success");
        assertThat(responseEntity.getBody()).containsEntry("userId", "userId123");
        assertThat(responseEntity.getBody()).containsEntry("userName", "John Doe");
        assertThat(responseEntity.getBody()).containsEntry("spotifyAccessToken", "spotifyToken");

        verify(jwtUtil).validateToken("validToken");
        verify(redisTemplate.opsForValue()).get("session:validSessionId");
    }

    @Test
    public void testCheckSession_unauthenticatedUser_noCookies() {
        when(request.getCookies()).thenReturn(null);

        PlaylistViewerNextException exception = assertThrows(PlaylistViewerNextException.class,
                () -> sessionCheckController.checkSession(request));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getErrorCode()).isEqualTo("SESSION_NOT_FOUND");
        assertThat(exception.getMessage()).isEqualTo("有効なセッションIDが存在しません。");
    }

    @Test
    public void testCheckSession_unauthenticatedUser_invalidSessionId() {
        Cookie cookie = new Cookie("sessionId", "invalidSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:invalidSessionId")).thenReturn(null);

        PlaylistViewerNextException exception = assertThrows(PlaylistViewerNextException.class,
                () -> sessionCheckController.checkSession(request));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getErrorCode()).isEqualTo("SESSION_VALIDATION_ERROR");
        assertThat(exception.getMessage()).isEqualTo("セッション情報の検証中にエラーが発生しました。");

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

        PlaylistViewerNextException exception = assertThrows(PlaylistViewerNextException.class,
                () -> sessionCheckController.checkSession(request));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getErrorCode()).isEqualTo("SESSION_VALIDATION_ERROR");
        assertThat(exception.getMessage()).isEqualTo("セッション情報の検証中にエラーが発生しました。");
    }

    @Test
    public void testLogout_successful() {
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(redisTemplate.delete("session:validSessionId")).thenReturn(true);

        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.logout(request, response);

        assertThat(responseEntity.getBody()).containsEntry("status", "success");
        assertThat(responseEntity.getBody()).containsEntry("message", "ログアウトしました。");

        verify(redisTemplate).delete("session:validSessionId");
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    public void testLogout_noSessionId() {
        when(request.getCookies()).thenReturn(null);

        PlaylistViewerNextException exception = assertThrows(PlaylistViewerNextException.class,
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

        PlaylistViewerNextException exception = assertThrows(PlaylistViewerNextException.class,
                () -> sessionCheckController.logout(request, response));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getErrorCode()).isEqualTo("LOGOUT_ERROR");
        assertThat(exception.getMessage()).isEqualTo("ログアウト処理中にエラーが発生しました。");

        verify(redisTemplate).delete("session:validSessionId");
    }

    @Test
    public void testLogout_redisException() {
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doThrow(new RuntimeException("Redis error")).when(redisTemplate).delete("session:validSessionId");

        PlaylistViewerNextException exception = assertThrows(PlaylistViewerNextException.class,
                () -> sessionCheckController.logout(request, response));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getErrorCode()).isEqualTo("LOGOUT_ERROR");
        assertThat(exception.getMessage()).isEqualTo("ログアウト処理中にエラーが発生しました。");
    }
}
