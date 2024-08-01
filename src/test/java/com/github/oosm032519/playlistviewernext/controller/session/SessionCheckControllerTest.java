package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.model.CustomUserDetails;
import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class SessionCheckControllerTest {

    private SessionCheckController sessionCheckController;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest request;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sessionCheckController = new SessionCheckController(jwtUtil);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void checkSession_WithValidJwt_ShouldReturnSuccessResponse() {
        // Arrange
        String jwt = "valid.jwt.token";
        String userId = "testUser";
        Cookie jwtCookie = new Cookie("JWT", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtUtil.validateToken(jwt)).thenReturn(Map.of("sub", userId));

        // Act
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "success");
        assertThat(response.getBody()).containsEntry("message", "User is authenticated");
        assertThat(response.getBody()).containsEntry("userId", userId);
    }

    @Test
    void checkSession_WithInvalidJwt_ShouldReturnErrorResponse() {
        // Arrange
        String jwt = "invalid.jwt.token";
        Cookie jwtCookie = new Cookie("JWT", jwt);
        when(request.getCookies()).thenReturn(new Cookie[]{jwtCookie});
        when(jwtUtil.validateToken(jwt)).thenThrow(new JwtException("Invalid token"));

        // Act
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "error");
        assertThat(response.getBody()).containsEntry("message", "User not authenticated");
    }

    @Test
    void checkSession_WithNoJwtButAuthenticatedUser_ShouldReturnSuccessResponse() {
        // Arrange
        when(request.getCookies()).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        CustomUserDetails userDetails = new CustomUserDetails("testUser", "password", Collections.emptyList(), "spotifyToken");
        when(authentication.getPrincipal()).thenReturn(userDetails);

        // Act
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "success");
        assertThat(response.getBody()).containsEntry("message", "User is authenticated");
        assertThat(response.getBody()).containsEntry("userId", "testUser");
    }

    @Test
    void checkSession_WithNoJwtAndNoAuthenticatedUser_ShouldReturnErrorResponse() {
        // Arrange
        when(request.getCookies()).thenReturn(null);
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "error");
        assertThat(response.getBody()).containsEntry("message", "User not authenticated");
    }

    @Test
    void checkSession_WithEmptyCookies_ShouldReturnErrorResponse() {
        // Arrange
        when(request.getCookies()).thenReturn(new Cookie[]{});
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "error");
        assertThat(response.getBody()).containsEntry("message", "User not authenticated");
    }

    @Test
    void checkSession_WithNonJwtCookie_ShouldReturnErrorResponse() {
        // Arrange
        Cookie nonJwtCookie = new Cookie("NonJWT", "some-value");
        when(request.getCookies()).thenReturn(new Cookie[]{nonJwtCookie});
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(request);

        // Assert
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "error");
        assertThat(response.getBody()).containsEntry("message", "User not authenticated");
    }
}
