package com.github.oosm032519.playlistviewernext.filter;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import com.nimbusds.jose.JOSEException;
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
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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

    @Mock
    private PrintWriter writer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtAuthenticationFilter, "redisTemplate", redisTemplate);
        SecurityContextHolder.clearContext();
    }

    @Test
    void testDoFilterInternal_ValidToken() throws ServletException, IOException, ParseException, JOSEException {
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
    void testDoFilterInternal_InvalidToken() throws ServletException, IOException, ParseException, JOSEException {
        // Arrange
        Cookie sessionCookie = new Cookie("sessionId", "testSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn("invalidToken");
        when(jwtUtil.validateToken("invalidToken")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(writer).write(contains("無効なセッション"));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
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
    void testDoFilterInternal_NoSessionInRedis() throws ServletException, IOException {
        // Arrange
        Cookie sessionCookie = new Cookie("sessionId", "testSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn(null);
        when(response.getWriter()).thenReturn(writer);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(writer).write(contains("セッション情報が見つかりません"));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_InvalidIssuer() throws ServletException, IOException, ParseException, JOSEException {
        // Arrange
        Cookie sessionCookie = new Cookie("sessionId", "testSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn("validToken");

        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "userId");
        claims.put("name", "userName");
        claims.put("spotify_access_token", "spotifyToken");
        claims.put("iss", "invalidIssuer");
        claims.put("aud", "testAudience");
        claims.put("exp", new Date(System.currentTimeMillis() + 1000000));

        when(jwtUtil.validateToken("validToken")).thenReturn(claims);
        when(jwtUtil.getIssuer()).thenReturn("testIssuer");
        when(response.getWriter()).thenReturn(writer);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(writer).write(contains("無効なJWTクレーム"));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_InvalidAudience() throws ServletException, IOException, ParseException, JOSEException {
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
        claims.put("aud", "invalidAudience");
        claims.put("exp", new Date(System.currentTimeMillis() + 1000000));

        when(jwtUtil.validateToken("validToken")).thenReturn(claims);
        when(jwtUtil.getIssuer()).thenReturn("testIssuer");
        when(jwtUtil.getAudience()).thenReturn("testAudience");
        when(response.getWriter()).thenReturn(writer);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(writer).write(contains("無効なJWTクレーム"));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_ExpiredToken() throws ServletException, IOException, ParseException, JOSEException {
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
        claims.put("exp", new Date(System.currentTimeMillis() - 1000000)); // Expired token

        when(jwtUtil.validateToken("validToken")).thenReturn(claims);
        when(jwtUtil.getIssuer()).thenReturn("testIssuer");
        when(jwtUtil.getAudience()).thenReturn("testAudience");
        when(response.getWriter()).thenReturn(writer);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(writer).write(contains("無効なJWTクレーム"));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_MissingRequiredClaims() throws ServletException, IOException, ParseException, JOSEException {
        // Arrange
        Cookie sessionCookie = new Cookie("sessionId", "testSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn("validToken");

        Map<String, Object> claims = new HashMap<>();
        claims.put("iss", "testIssuer");
        claims.put("aud", "testAudience");
        claims.put("exp", new Date(System.currentTimeMillis() + 1000000));
        // Missing required claims: sub, name, spotify_access_token

        when(jwtUtil.validateToken("validToken")).thenReturn(claims);
        when(jwtUtil.getIssuer()).thenReturn("testIssuer");
        when(jwtUtil.getAudience()).thenReturn("testAudience");
        when(response.getWriter()).thenReturn(writer);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(writer).write(contains("無効なJWTクレーム"));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_JOSEException() throws ServletException, IOException, ParseException, JOSEException {
        // Arrange
        Cookie sessionCookie = new Cookie("sessionId", "testSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn("validToken");

        when(jwtUtil.validateToken("validToken")).thenThrow(new JOSEException("JOSE error"));
        when(response.getWriter()).thenReturn(writer);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(writer).write(contains("JWTトークンの検証エラー"));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    @Test
    void testDoFilterInternal_ParseException() throws ServletException, IOException, ParseException, JOSEException {
        // Arrange
        Cookie sessionCookie = new Cookie("sessionId", "testSessionId");
        when(request.getCookies()).thenReturn(new Cookie[]{sessionCookie});
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        when(valueOperations.get("session:testSessionId")).thenReturn("validToken");

        when(jwtUtil.validateToken("validToken")).thenThrow(new ParseException("Parse error", 0));
        when(response.getWriter()).thenReturn(writer);

        // Act
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert
        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        verify(writer).write(contains("JWTトークンの検証エラー"));
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
