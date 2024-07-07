package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.SpotifyAuthService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistAuthControllerTest {

    @Mock
    private SpotifyAuthService authService;

    @InjectMocks
    private PlaylistAuthController authController;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void authenticate_Successfully() throws IOException, ParseException, SpotifyWebApiException {
        // When
        authController.authenticate();

        // Then
        verify(authService).getClientCredentialsToken();
    }

    @Test
    void authenticate_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        doThrow(new RuntimeException("Auth error")).when(authService).getClientCredentialsToken();

        // When
        try {
            authController.authenticate();
        } catch (RuntimeException e) {
            // Then
            assertThat(e.getMessage()).isEqualTo("認証エラー");
        }

        verify(authService).getClientCredentialsToken();
    }
}
