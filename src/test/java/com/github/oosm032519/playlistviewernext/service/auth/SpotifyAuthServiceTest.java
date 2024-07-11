// SpotifyAuthServiceTest.java

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
class SpotifyAuthServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @InjectMocks
    private SpotifyAuthService authService;

    /**
     * 各テストの前に実行されるセットアップメソッド
     */
    @BeforeEach
    void setUp() {
    }

    /**
     * 正常系のテスト: クライアントクレデンシャルトークンを取得する
     *
     * @throws IOException
     * @throws SpotifyWebApiException
     * @throws ParseException
     */
    @Test
    void testGetClientCredentialsToken_正常系() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange: モックの設定
        ClientCredentialsRequest.Builder builder = mock(ClientCredentialsRequest.Builder.class);
        ClientCredentialsRequest clientCredentialsRequest = mock(ClientCredentialsRequest.class);
        ClientCredentials clientCredentials = mock(ClientCredentials.class);
        when(spotifyApi.clientCredentials()).thenReturn(builder);
        when(builder.build()).thenReturn(clientCredentialsRequest);
        when(clientCredentialsRequest.execute()).thenReturn(clientCredentials);
        when(clientCredentials.getAccessToken()).thenReturn("test-access-token");

        // Act: メソッドの実行
        authService.getClientCredentialsToken();

        // Assert: 結果の検証
        verify(spotifyApi).setAccessToken("test-access-token");
    }

    /**
     * 異常系のテスト: APIエラーが発生する場合
     *
     * @throws IOException
     * @throws SpotifyWebApiException
     * @throws ParseException
     */
    @Test
    void testGetClientCredentialsToken_異常系_APIエラー() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange: モックの設定
        ClientCredentialsRequest.Builder builder = mock(ClientCredentialsRequest.Builder.class);
        ClientCredentialsRequest clientCredentialsRequest = mock(ClientCredentialsRequest.class);
        when(spotifyApi.clientCredentials()).thenReturn(builder);
        when(builder.build()).thenReturn(clientCredentialsRequest);
        when(clientCredentialsRequest.execute()).thenThrow(new IOException("API error"));

        // Act & Assert: メソッドの実行と例外の検証
        assertThatThrownBy(() -> authService.getClientCredentialsToken())
                .isInstanceOf(IOException.class)
                .hasMessage("API error");
    }

    /**
     * 正常系のテスト: アクセストークンを設定する
     */
    @Test
    void testSetAccessToken_正常系() {
        // Arrange: モックの設定
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);

        when(authentication.getName()).thenReturn("test-user");
        when(authorizedClientService.loadAuthorizedClient("spotify", "test-user")).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("test-access-token");

        // Act: メソッドの実行
        authService.setAccessToken(authentication);

        // Assert: 結果の検証
        verify(spotifyApi).setAccessToken("test-access-token");
    }

    /**
     * 異常系のテスト: 認証情報が存在しない場合
     */
    @Test
    void testSetAccessToken_異常系_認証情報なし() {
        // Arrange: モックの設定
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        when(authentication.getName()).thenReturn("test-user");
        when(authorizedClientService.loadAuthorizedClient("spotify", "test-user")).thenReturn(null);

        // Act & Assert: メソッドの実行と例外の検証
        assertThatThrownBy(() -> authService.setAccessToken(authentication))
                .isInstanceOf(NullPointerException.class);
    }
}
