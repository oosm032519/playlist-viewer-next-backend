package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
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
    public void testCheckSession_unauthenticatedUser() {
        // モックの設定
        when(request.getCookies()).thenReturn(null);

        // メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = sessionCheckController.checkSession(request);

        // アサーション
        assertThat(responseEntity.getBody()).containsEntry("status", "error");
        assertThat(responseEntity.getBody()).containsEntry("message", "User not authenticated");
    }
}
