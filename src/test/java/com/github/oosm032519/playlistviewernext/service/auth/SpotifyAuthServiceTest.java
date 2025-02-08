package com.github.oosm032519.playlistviewernext.service.auth;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
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

    /**
     * クライアントクレデンシャルトークンが正常に取得され、SpotifyApiインスタンスに設定されることを確認する。
     */
    @Test
    void getClientCredentialsToken_Success() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange: モックの設定
        ClientCredentials clientCredentials = mock(ClientCredentials.class);
        when(clientCredentialsRequest.execute()).thenReturn(clientCredentials);
        when(clientCredentials.getAccessToken()).thenReturn("test-access-token");

        // Act: テスト対象メソッドの実行
        spotifyAuthService.getClientCredentialsToken();

        // Assert: SpotifyApiにアクセストークンが設定されたことを確認
        verify(spotifyApi).setAccessToken("test-access-token");
        verify(clientCredentialsRequest).execute();
    }

    /**
     * クライアントクレデンシャルトークンの取得中にIOExceptionが発生した場合、
     * InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void getClientCredentialsToken_IOExceptionThrown() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange: IOExceptionをスローするモックの設定
        when(clientCredentialsRequest.execute()).thenThrow(new IOException("Test IO Exception"));

        // Act & Assert: InternalServerExceptionがスローされることを確認
        assertThatThrownBy(() -> spotifyAuthService.getClientCredentialsToken())
                .isInstanceOf(InternalServerException.class)
                .hasMessage("クライアントクレデンシャルトークンの取得中にエラーが発生しました。")
                .hasCauseInstanceOf(IOException.class);
    }

    /**
     * クライアントクレデンシャルトークンの取得中にParseExceptionが発生した場合、
     * InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void getClientCredentialsToken_ParseExceptionThrown() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange: ParseExceptionをスローするモックの設定
        when(clientCredentialsRequest.execute()).thenThrow(new ParseException("Test Parse Exception"));

        // Act & Assert: InternalServerExceptionがスローされることを確認
        assertThatThrownBy(() -> spotifyAuthService.getClientCredentialsToken())
                .isInstanceOf(InternalServerException.class)
                .hasMessage("クライアントクレデンシャルトークンの取得中にエラーが発生しました。")
                .hasCauseInstanceOf(ParseException.class);
    }
}
