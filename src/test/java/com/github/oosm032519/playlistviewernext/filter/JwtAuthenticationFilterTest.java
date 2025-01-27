package com.github.oosm032519.playlistviewernext.filter;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "redisTemplate", redisTemplate);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_ValidToken() throws ServletException, IOException {
        // Arrange
        Cookie sessionCookie = new Cookie("sessionId", "testSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn("validToken");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "userId");
        claims.put("name", "userName");
        claims.put("spotify_access_token", "spotifyToken");
        claims.put("iss", "testIssuer");
        claims.put("aud", "testAudience");
        claims.put("exp", new Date(System.currentTimeMillis() + 1000000));

        when(jwtUtil.validateToken("validToken")).thenReturn(claims);
        when(jwtUtil.getIssuer()).thenReturn("testIssuer");
        when(jwtUtil.getAudience()).thenReturn("testAudience");

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isInstanceOf(OAuth2AuthenticationToken.class);
    }

    @Test
    void testDoFilterInternal_InvalidToken() {
        // Arrange
        Cookie sessionCookie = new Cookie("sessionId", "testSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn("invalidToken");
        when(jwtUtil.validateToken("invalidToken")).thenReturn(null);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
        assertEquals("ログイン処理中にエラーが発生しました。再度ログインしてください。", exception.getMessage());
    }

    @Test
    void testDoFilterInternal_NoSessionId() throws ServletException, IOException {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_NullCookies() throws ServletException, IOException {
        // Arrange
        when(request.getCookies()).thenReturn(null);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_NoSessionInRedis() {
        // Arrange
        Cookie sessionCookie = new Cookie("sessionId", "testSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn(null);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> jwtAuthenticationFilter.doFilterInternal(request, response, filterChain));
        assertEquals("ログイン処理中にエラーが発生しました。再度ログインしてください。", exception.getMessage());
    }

    @Test
    void testValidateClaims_InvalidIssuer() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "invalidIssuer");
        claims.put("aud", "testAudience");
        claims.put("exp", new Date(System.currentTimeMillis() + 1000000));
        claims.put("sub", "userId");
        claims.put("name", "userName");
        claims.put("spotify_access_token", "spotifyToken");

        when(jwtUtil.getIssuer()).thenReturn("testIssuer");

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "validateClaims", claims);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("無効な発行者です。", exception.getMessage());
    }

    @Test
    void testValidateClaims_InvalidAudience() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "testIssuer");
        claims.put("aud", "invalidAudience");
        claims.put("exp", new Date(System.currentTimeMillis() + 1000000));
        claims.put("sub", "userId");
        claims.put("name", "userName");
        claims.put("spotify_access_token", "spotifyToken");

        when(jwtUtil.getIssuer()).thenReturn("testIssuer");
        when(jwtUtil.getAudience()).thenReturn("testAudience");

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "validateClaims", claims);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("無効な対象者です。", exception.getMessage());
    }

    @Test
    void testValidateClaims_ExpiredToken() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "testIssuer");
        claims.put("aud", "testAudience");
        claims.put("exp", new Date(System.currentTimeMillis() - 1000)); // 過去の日付
        claims.put("sub", "userId");
        claims.put("name", "userName");
        claims.put("spotify_access_token", "spotifyToken");

        when(jwtUtil.getIssuer()).thenReturn("testIssuer");
        when(jwtUtil.getAudience()).thenReturn("testAudience");

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "validateClaims", claims);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("トークンの有効期限が切れています。", exception.getMessage());
    }

    @Test
    void testValidateClaims_InvalidClaimsValues() {
        // Arrange
        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "testIssuer");
        claims.put("aud", "testAudience");
        claims.put("exp", new Date(System.currentTimeMillis() + 1000000));
        claims.put("sub", ""); // 無効なsub
        claims.put("name", "userName");
        claims.put("spotify_access_token", "spotifyToken");

        when(jwtUtil.getIssuer()).thenReturn("testIssuer");
        when(jwtUtil.getAudience()).thenReturn("testAudience");

        // Act & Assert
        InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
            ReflectionTestUtils.invokeMethod(jwtAuthenticationFilter, "validateClaims", claims);
        });

        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        assertEquals("必須クレームの値が無効です。", exception.getMessage());
    }

    @Test
    void testHandleInvalidRequestException() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("sessionId", "testSessionId")});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn("validToken");

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "testIssuer");
        claims.put("aud", "testAudience");
        claims.put("exp", new Date(System.currentTimeMillis() + 1000000));
        claims.put("sub", "userId");
        claims.put("name", "userName");
        claims.put("spotify_access_token", "spotifyToken");

        // Simulate InvalidRequestException
        InvalidRequestException invalidRequestException = new InvalidRequestException(HttpStatus.BAD_REQUEST, "不正なリクエストです。");
        when(jwtUtil.validateToken("validToken")).thenThrow(invalidRequestException);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });

        assertEquals("不正なリクエストです。", exception.getMessage());
    }

    @Test
    void testHandleGeneralException() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("sessionId", "testSessionId")});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn("validToken");

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "testIssuer");
        claims.put("aud", "testAudience");
        claims.put("exp", new Date(System.currentTimeMillis() + 1000000));
        claims.put("sub", "userId");
        claims.put("name", "userName");
        claims.put("spotify_access_token", "spotifyToken");

        // Simulate a general exception
        when(jwtUtil.validateToken("validToken")).thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class, () -> {
            jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);
        });

        assertEquals("ログイン処理中にエラーが発生しました。再度ログインしてください。", exception.getMessage());
    }
}
