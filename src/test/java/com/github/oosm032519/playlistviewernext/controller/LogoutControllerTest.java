package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.controller.auth.LogoutController;
import com.github.oosm032519.playlistviewernext.service.auth.LogoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class LogoutControllerTest {

    private LogoutController logoutController;

    @Mock
    private LogoutService logoutService;

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logoutController = new LogoutController(authorizedClientService);
        logoutController = new LogoutController(authorizedClientService) {
            @Override
            protected LogoutService createLogoutService(OAuth2AuthorizedClientService authorizedClientService) {
                return logoutService;
            }
        };
    }

    @Test
    void testLogoutSuccess() {
        // Arrange
        doNothing().when(logoutService).processLogout(request, response);

        // Act
        ResponseEntity<String> responseEntity = logoutController.logout(request, response);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("ログアウトしました", responseEntity.getBody());
    }

    @Test
    void testLogoutFailure() {
        // Arrange
        doThrow(new RuntimeException("Logout failed")).when(logoutService).processLogout(request, response);

        // Act
        ResponseEntity<String> responseEntity = logoutController.logout(request, response);

        // Assert
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("ログアウト処理中にエラーが発生しました", responseEntity.getBody());
    }
}
