package com.github.oosm032519.playlistviewernext.service.auth;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyUserAuthenticationServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @InjectMocks
    private SpotifyAuthService authService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetClientCredentialsToken_正常系() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        ClientCredentialsRequest.Builder builder = mock(ClientCredentialsRequest.Builder.class);
        ClientCredentialsRequest clientCredentialsRequest = mock(ClientCredentialsRequest.class);
        ClientCredentials clientCredentials = mock(ClientCredentials.class);
        when(spotifyApi.clientCredentials()).thenReturn(builder);
        when(builder.build()).thenReturn(clientCredentialsRequest);
        when(clientCredentialsRequest.execute()).thenReturn(clientCredentials);
        when(clientCredentials.getAccessToken()).thenReturn("test-access-token");

        // Act
        authService.getClientCredentialsToken();

        // Assert
        verify(spotifyApi).setAccessToken("test-access-token");
    }

    @Test
    void testGetClientCredentialsToken_異常系_APIエラー() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        ClientCredentialsRequest.Builder builder = mock(ClientCredentialsRequest.Builder.class);
        ClientCredentialsRequest clientCredentialsRequest = mock(ClientCredentialsRequest.class);
        when(spotifyApi.clientCredentials()).thenReturn(builder);
        when(builder.build()).thenReturn(clientCredentialsRequest);
        when(clientCredentialsRequest.execute()).thenThrow(new IOException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> authService.getClientCredentialsToken())
                .isInstanceOf(IOException.class)
                .hasMessage("API error");
    }

    @Test
    void testSetAccessToken_正常系() {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);

        when(authentication.getName()).thenReturn("test-user");
        when(authorizedClientService.loadAuthorizedClient("spotify", "test-user")).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("test-access-token");

        // Act
        authService.setAccessToken(authentication);

        // Assert
        verify(spotifyApi).setAccessToken("test-access-token");
    }

    @Test
    void testSetAccessToken_異常系_認証情報なし() {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        when(authentication.getName()).thenReturn("test-user");
        when(authorizedClientService.loadAuthorizedClient("spotify", "test-user")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> authService.setAccessToken(authentication))
                .isInstanceOf(NullPointerException.class);
    }
}
