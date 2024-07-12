package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.LogoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogoutControllerTest {

    @InjectMocks
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
        logoutController = new LogoutController(authorizedClientService) {
            @Override
            protected LogoutService createLogoutService(OAuth2AuthorizedClientService authorizedClientService) {
                return logoutService;
            }
        };
    }

    @Test
    void logout_shouldReturnOkStatus_whenLogoutSucceeds() {
        // Arrange
        doNothing().when(logoutService).processLogout(request, response);

        // Act
        ResponseEntity<String> responseEntity = logoutController.logout(request, response);

        // Assert
        assertAll(
                () -> assertEquals(HttpStatus.OK, responseEntity.getStatusCode()),
                () -> assertEquals("ログアウトしました", responseEntity.getBody()),
                () -> verify(logoutService, times(1)).processLogout(request, response)
        );
    }

    @Test
    void logout_shouldReturnInternalServerError_whenLogoutFails() {
        // Arrange
        doThrow(new RuntimeException("Logout failed")).when(logoutService).processLogout(request, response);

        // Act
        ResponseEntity<String> responseEntity = logoutController.logout(request, response);

        // Assert
        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()),
                () -> assertEquals("ログアウト処理中にエラーが発生しました", responseEntity.getBody()),
                () -> verify(logoutService, times(1)).processLogout(request, response)
        );
    }

    @Test
    void logout_shouldHandleNullPointerException() {
        // Arrange
        doThrow(new NullPointerException("Null pointer during logout")).when(logoutService).processLogout(request, response);

        // Act
        ResponseEntity<String> responseEntity = logoutController.logout(request, response);

        // Assert
        assertAll(
                () -> assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode()),
                () -> assertEquals("ログアウト処理中にエラーが発生しました", responseEntity.getBody()),
                () -> verify(logoutService, times(1)).processLogout(request, response)
        );
    }
}
