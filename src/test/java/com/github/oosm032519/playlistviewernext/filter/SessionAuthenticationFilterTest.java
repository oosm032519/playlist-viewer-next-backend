package com.github.oosm032519.playlistviewernext.filter;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionAuthenticationFilterTest {

    @InjectMocks
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext(); // テスト開始前にSecurityContextをクリア
    }

    @Test
    void doFilterInternal_ValidSessionId_AuthenticationSuccess() throws ServletException, IOException {
        // Arrange
        String sessionId = "validSessionId";
        Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
        when(request.getCookies()).thenReturn(new Cookie[]{sessionIdCookie});
        Map<Object, Object> sessionData = createSessionData("user123", "Test User", "testToken");

        // HashOperations のモックをテストメソッド内で設定
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("session:" + sessionId)).thenReturn(sessionData);

        // Act
        sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        OAuth2User oAuth2User = authentication.getPrincipal();
        assertThat((String) oAuth2User.getAttribute("id")).isEqualTo("user123");
        assertThat((String) oAuth2User.getAttribute("name")).isEqualTo("Test User");
        assertThat((String) oAuth2User.getAttribute("spotify_access_token")).isEqualTo("testToken");
    }

    private Map<Object, Object> createSessionData(String userId, String userName, String spotifyAccessToken) {
        Map<Object, Object> sessionData = new HashMap<>();
        sessionData.put("userId", userId);
        sessionData.put("userName", userName);
        sessionData.put("spotifyAccessToken", spotifyAccessToken);
        return sessionData;
    }

    @Test
    void doFilterInternal_InvalidSessionId_AuthenticationFailure() throws ServletException, IOException {
        // Arrange
        String sessionId = "invalidSessionId";
        Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
        when(request.getCookies()).thenReturn(new Cookie[]{sessionIdCookie});

        // HashOperations のモックをテストメソッド内で設定
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("session:" + sessionId)).thenReturn(null);

        // Act & Assert
        assertThrows(AuthenticationException.class, () -> {
            sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_NoSessionId_NoAuthentication() throws ServletException, IOException {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void doFilterInternal_RedisError_AuthenticationException() throws ServletException, IOException {
        // Arrange
        String sessionId = "errorSessionId";
        Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
        when(request.getCookies()).thenReturn(new Cookie[]{sessionIdCookie});

        // HashOperations のモックをテストメソッド内で設定
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("session:" + sessionId)).thenThrow(new RuntimeException("Redis error"));

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });
        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
