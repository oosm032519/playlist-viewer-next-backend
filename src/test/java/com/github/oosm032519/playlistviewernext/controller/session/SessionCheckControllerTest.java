package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
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
        when(redisTemplate.opsForValue().get("session:invalidSessionId")).thenReturn(null);

        assertThatThrownBy(() -> sessionCheckController.checkSession(request))
                .isInstanceOf(AuthenticationException.class);
    }

    @Test
    public void logout_noSessionId() {
        when(request.getCookies()).thenReturn(null);

        assertThatThrownBy(() -> sessionCheckController.logout(request, response))
                .isInstanceOf(AuthenticationException.class);
    }
}
