package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
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
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@SpringBootTest
public class SessionCheckControllerTest {

    @Autowired
    private SessionCheckController sessionCheckController;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private HashOperations<String, Object, Object> hashOperations;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
    }

    @Test
    public void checkSession_noSessionId() {
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> sessionCheckController.checkSession(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void checkSession_invalidSession() {
        Cookie cookie = new Cookie("sessionId", "invalidSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(hashOperations.entries("session:invalidSessionId")).thenReturn(Collections.emptyMap());

        assertThatThrownBy(() -> sessionCheckController.checkSession(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void checkSession_validSession() {
        Cookie cookie = new Cookie("sessionId", "validSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        Map<Object, Object> sessionData = new HashMap<>();
        sessionData.put("userId", "user123");
        sessionData.put("userName", "Test User");
        sessionData.put("spotifyAccessToken", "testToken");
        when(hashOperations.entries("session:validSessionId")).thenReturn(sessionData);

        ResponseEntity<?> responseEntity = sessionCheckController.checkSession(request);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo("success");
        assertThat(responseBody.get("userId")).isEqualTo("user123");
        assertThat(responseBody.get("userName")).isEqualTo("Test User");
        assertThat(responseBody.get("spotifyAccessToken")).isEqualTo("testToken");
    }


    @Test
    public void logout_noSessionId() {
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> sessionCheckController.logout(request, response))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void logout_validSessionId() {
        Cookie cookie = new Cookie("sessionId", "validSessionIdForLogout");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(redisTemplate.delete("session:validSessionIdForLogout")).thenReturn(true);

        ResponseEntity<?> responseEntity = sessionCheckController.logout(request, response);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        Map<String, Object> responseBody = (Map<String, Object>) responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo("success");
        assertThat(responseBody.get("message")).isEqualTo("ログアウトしました。");
    }

    @Test
    public void logout_invalidSessionId() {
        Cookie cookie = new Cookie("sessionId", "invalidSessionIdForLogout");
        when(request.getCookies()).thenReturn(new Cookie[]{cookie});
        when(redisTemplate.delete("session:invalidSessionIdForLogout")).thenReturn(false); // Redisから削除失敗

        assertThatThrownBy(() -> sessionCheckController.logout(request, response))
                .isInstanceOf(DatabaseAccessException.class)
                .hasMessageContaining("ログアウト処理中にエラーが発生しました。再度お試しください。");
    }
}
