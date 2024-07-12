package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyClientCredentialsAuthenticationTest {

    @Mock
    private SpotifyAuthService authService;

    @InjectMocks
    private SpotifyClientCredentialsAuthentication authController;

    @Test
    void authenticate_Successfully() throws IOException, ParseException, SpotifyWebApiException {
        authController.authenticate();
        verify(authService, times(1)).getClientCredentialsToken();
    }

    @Test
    void authenticate_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        doThrow(new RuntimeException("Auth error")).when(authService).getClientCredentialsToken();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authController.authenticate());
        verify(authService, times(1)).getClientCredentialsToken();
        assert "認証エラー".equals(exception.getMessage());
    }
}
