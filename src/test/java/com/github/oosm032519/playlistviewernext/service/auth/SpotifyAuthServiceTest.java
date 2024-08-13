package com.github.oosm032519.playlistviewernext.service.auth;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyAuthServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private ClientCredentialsRequest.Builder clientCredentialsBuilder;

    @Mock
    private ClientCredentialsRequest clientCredentialsRequest;

    @InjectMocks
    private SpotifyAuthService spotifyAuthService;

    @BeforeEach
    void setUp() {
        when(spotifyApi.clientCredentials()).thenReturn(clientCredentialsBuilder);
        when(clientCredentialsBuilder.build()).thenReturn(clientCredentialsRequest);
    }

    @Test
    void getClientCredentialsToken_Success() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        ClientCredentials clientCredentials = mock(ClientCredentials.class);
        when(clientCredentialsRequest.execute()).thenReturn(clientCredentials);
        when(clientCredentials.getAccessToken()).thenReturn("test-access-token");

        // Act
        spotifyAuthService.getClientCredentialsToken();

        // Assert
        verify(spotifyApi).setAccessToken("test-access-token");
        verify(clientCredentialsRequest).execute();
    }

    @Test
    void getClientCredentialsToken_IOExceptionThrown() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        when(clientCredentialsRequest.execute()).thenThrow(new IOException("Test IO Exception"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyAuthService.getClientCredentialsToken())
                .isInstanceOf(com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException.class)
                .hasMessage("クライアントクレデンシャルトークンの取得中にエラーが発生しました。")
                .hasCauseInstanceOf(IOException.class);
    }

    @Test
    void getClientCredentialsToken_SpotifyWebApiExceptionThrown() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        when(clientCredentialsRequest.execute()).thenThrow(new SpotifyWebApiException("Test Spotify Web API Exception"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyAuthService.getClientCredentialsToken())
                .isInstanceOf(com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException.class)
                .hasMessage("クライアントクレデンシャルトークンの取得中にエラーが発生しました。")
                .hasCauseInstanceOf(SpotifyWebApiException.class);
    }

    @Test
    void getClientCredentialsToken_ParseExceptionThrown() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        when(clientCredentialsRequest.execute()).thenThrow(new ParseException("Test Parse Exception"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyAuthService.getClientCredentialsToken())
                .isInstanceOf(com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException.class)
                .hasMessage("クライアントクレデンシャルトークンの取得中にエラーが発生しました。")
                .hasCauseInstanceOf(ParseException.class);
    }
}
