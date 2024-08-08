package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import com.nimbusds.jose.JOSEException;
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
import org.springframework.http.ResponseEntity;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
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
        // モックの設定
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

        // メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.checkSession(request);

        // アサーション
        assertThat(responseEntity.getBody()).containsEntry("status", "success");
        assertThat(responseEntity.getBody()).containsEntry("userId", "userId123");
        assertThat(responseEntity.getBody()).containsEntry("userName", "John Doe");
        assertThat(responseEntity.getBody()).containsEntry("spotifyAccessToken", "spotifyToken");

        // 検証
        verify(jwtUtil).validateToken("validToken");
        verify(redisTemplate.opsForValue()).get("session:validSessionId");
    }

    @Test
    public void testCheckSession_unauthenticatedUser_noCookies() {
        // モックの設定
        when(request.getCookies()).thenReturn(null);

        // メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.checkSession(request);

        // アサーション
        assertThat(responseEntity.getBody()).containsEntry("status", "error");
        assertThat(responseEntity.getBody()).containsEntry("message", "User not authenticated");
    }

    @Test
    public void testCheckSession_unauthenticatedUser_invalidSessionId() {
        // モックの設定
        Cookie cookie = new Cookie("sessionId", "invalidSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:invalidSessionId")).thenReturn(null);

        // メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.checkSession(request);

        // アサーション
        assertThat(responseEntity.getBody()).containsEntry("status", "error");
        assertThat(responseEntity.getBody()).containsEntry("message", "User not authenticated");

        // 検証
        verify(redisTemplate.opsForValue()).get("session:invalidSessionId");
    }

    @Test
    public void testCheckSession_joseException() throws Exception {
        // モックの設定
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:validSessionId")).thenReturn("validToken");

        when(jwtUtil.validateToken("validToken")).thenThrow(new JOSEException("Invalid token"));

        // メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.checkSession(request);

        // アサーション
        assertThat(responseEntity.getBody()).containsEntry("status", "error");
        assertThat(responseEntity.getBody()).containsEntry("message", "User not authenticated");
    }

    @Test
    public void testCheckSession_parseException() throws Exception {
        // モックの設定
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});

        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:validSessionId")).thenReturn("validToken");

        when(jwtUtil.validateToken("validToken")).thenThrow(new ParseException("Invalid token", 0));

        // メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.checkSession(request);

        // アサーション
        assertThat(responseEntity.getBody()).containsEntry("status", "error");
        assertThat(responseEntity.getBody()).containsEntry("message", "User not authenticated");
    }

    @Test
    public void testLogout_successful() {
        // モックの設定
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(redisTemplate.delete("session:validSessionId")).thenReturn(true);

        // メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.logout(request, response);

        // アサーション
        assertThat(responseEntity.getBody()).containsEntry("status", "success");
        assertThat(responseEntity.getBody()).containsEntry("message", "ログアウトしました。");

        // 検証
        verify(redisTemplate).delete("session:validSessionId");
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    public void testLogout_noSessionId() {
        // モックの設定
        when(request.getCookies()).thenReturn(null);

        // メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.logout(request, response);

        // アサーション
        assertThat(responseEntity.getBody()).containsEntry("status", "error");
        assertThat(responseEntity.getBody()).containsEntry("message", "有効なセッションIDが存在しません。");
    }

    @Test
    public void testLogout_sessionNotFoundInRedis() {
        // モックの設定
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(redisTemplate.delete("session:validSessionId")).thenReturn(false);

        // メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.logout(request, response);

        // アサーション
        assertThat(responseEntity.getBody()).containsEntry("status", "error");
        assertThat(responseEntity.getBody()).containsEntry("message", "セッション情報が見つかりません。");

        // 検証
        verify(redisTemplate).delete("session:validSessionId");
    }

    @Test
    public void testLogout_redisException() {
        // モックの設定
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        doThrow(new RuntimeException("Redis error")).when(redisTemplate).delete("session:validSessionId");

        // メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.logout(request, response);

        // アサーション
        assertThat(responseEntity.getBody()).containsEntry("status", "error");
        assertThat(responseEntity.getBody()).containsEntry("message", "ログアウト処理中にエラーが発生しました。");
    }
}
