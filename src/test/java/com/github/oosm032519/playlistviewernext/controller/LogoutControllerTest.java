package com.github.oosm032519.playlistviewernext.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LogoutControllerTest {

    private LogoutController logoutController;

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private SecurityContextLogoutHandler logoutHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logoutController = new LogoutController(authorizedClientService);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testLogoutWithAuthenticatedUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("testCookie", "testValue")});

        // Act
        ResponseEntity<String> response = logoutController.logout(request, this.response);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ログアウトしました", response.getBody());
        verify(authorizedClientService, never()).removeAuthorizedClient(anyString(), anyString());
        verify(this.response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void testLogoutWithOAuth2AuthenticationToken() {
        // Arrange
        OAuth2AuthenticationToken oauthToken = mock(OAuth2AuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(oauthToken);
        when(oauthToken.getName()).thenReturn("testUser");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("testCookie", "testValue")});

        // Act
        ResponseEntity<String> response = logoutController.logout(request, this.response);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ログアウトしました", response.getBody());
        verify(authorizedClientService, times(1)).removeAuthorizedClient("spotify", "testUser");
        verify(this.response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void testLogoutWithoutAuthentication() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("testCookie", "testValue")});

        // Act
        ResponseEntity<String> response = logoutController.logout(request, this.response);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ログアウトしました", response.getBody());
        verify(authorizedClientService, never()).removeAuthorizedClient(anyString(), anyString());
        verify(this.response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void testLogoutWithNoCookies() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getCookies()).thenReturn(null);

        // Act
        ResponseEntity<String> response = logoutController.logout(request, this.response);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ログアウトしました", response.getBody());
        verify(authorizedClientService, never()).removeAuthorizedClient(anyString(), anyString());
        verify(this.response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void testLogoutWithMultipleCookies() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie("cookie1", "value1"),
                new Cookie("cookie2", "value2"),
                new Cookie("cookie3", "value3")
        });

        // Act
        ResponseEntity<String> response = logoutController.logout(request, this.response);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("ログアウトしました", response.getBody());
        verify(authorizedClientService, never()).removeAuthorizedClient(anyString(), anyString());
        verify(this.response, times(3)).addCookie(any(Cookie.class));
    }
}
