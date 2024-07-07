package com.github.oosm032519.playlistviewernext.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

class LogoutServiceTest {

    private LogoutService logoutService;

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
        logoutService = new LogoutService(authorizedClientService, logoutHandler);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testProcessLogoutWithAuthenticatedUser() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("testCookie", "testValue")});

        // Act
        logoutService.processLogout(request, response);

        // Assert
        verify(logoutHandler, times(1)).logout(request, response, authentication);
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void testProcessLogoutWithOAuth2AuthenticationToken() {
        // Arrange
        OAuth2AuthenticationToken oauthToken = mock(OAuth2AuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(oauthToken);
        when(oauthToken.getName()).thenReturn("testUser");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("testCookie", "testValue")});

        // Act
        logoutService.processLogout(request, response);

        // Assert
        verify(authorizedClientService, times(1)).removeAuthorizedClient("spotify", "testUser");
        verify(logoutHandler, times(1)).logout(request, response, oauthToken);
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void testProcessLogoutWithoutAuthentication() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("testCookie", "testValue")});

        // Act
        logoutService.processLogout(request, response);

        // Assert
        verify(logoutHandler, never()).logout(request, response, null);
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    @Test
    void testProcessLogoutWithNoCookies() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getCookies()).thenReturn(null);

        // Act
        logoutService.processLogout(request, response);

        // Assert
        verify(logoutHandler, times(1)).logout(request, response, authentication);
        verify(response, never()).addCookie(any(Cookie.class));
    }

    @Test
    void testProcessLogoutWithMultipleCookies() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie("cookie1", "value1"),
                new Cookie("cookie2", "value2"),
                new Cookie("cookie3", "value3")
        });

        // Act
        logoutService.processLogout(request, response);

        // Assert
        verify(logoutHandler, times(1)).logout(request, response, authentication);
        verify(response, times(3)).addCookie(any(Cookie.class));
    }
}
