package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyClientCredentialsAuthenticationTest {

    @Mock
    private SpotifyAuthService authService;

    @InjectMocks
    private SpotifyClientCredentialsAuthentication authController;

    @Test
    void authenticate_Successfully() throws Exception {
        authController.authenticate();
        verify(authService, times(1)).getClientCredentialsToken();
    }

    @Test
    void authenticate_HandlesExceptionGracefully() throws Exception {
        doThrow(new RuntimeException("Auth error")).when(authService).getClientCredentialsToken();

        SpotifyApiException exception = assertThrows(SpotifyApiException.class, () -> authController.authenticate());

        verify(authService, times(1)).getClientCredentialsToken();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals("CLIENT_CREDENTIALS_AUTH_ERROR", exception.getErrorCode());
        assertEquals("Spotify APIへの接続中にエラーが発生しました。しばらく時間をおいてから再度お試しください。", exception.getMessage());
    }
}
