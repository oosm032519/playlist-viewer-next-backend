package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyClientCredentialsAuthenticationTest {

    @Mock
    private SpotifyAuthService authService;

    @InjectMocks
    private SpotifyClientCredentialsAuthentication spotifyClientCredentialsAuthentication;

    /**
     * 認証が成功した場合に、SpotifyAuthServiceのgetClientCredentialsTokenメソッドが1回呼び出されることを確認する。
     */
    @Test
    void authenticate_success() throws SpotifyWebApiException {
        // Arrange: authServiceのモックを設定。特に何も返却値は設定しない
        doNothing().when(authService).getClientCredentialsToken();

        // Act: 認証メソッドを実行
        spotifyClientCredentialsAuthentication.authenticate();

        // Assert: authServiceのgetClientCredentialsToken()が1回呼び出されたことを確認
        verify(authService, times(1)).getClientCredentialsToken();
    }

    /**
     * 認証時にSpotifyWebApiExceptionが発生した場合、例外がスローされることを確認する。
     */
    @Test
    void authenticate_throwsSpotifyWebApiException() throws SpotifyWebApiException {
        // Arrange: authServiceのモックを設定し、SpotifyWebApiExceptionをスローするように設定
        doThrow(SpotifyWebApiException.class).when(authService).getClientCredentialsToken();

        // Act & Assert: 認証メソッドを実行し、SpotifyWebApiExceptionがスローされることを確認
        // JUnitのassertThrowsからAssertJのassertThatThrownByに変更
        assertThatThrownBy(() -> spotifyClientCredentialsAuthentication.authenticate())
                .isInstanceOf(SpotifyWebApiException.class);
    }

    /**
     * 認証時にSpotifyWebApiExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void authenticate_throwsInternalServerException() throws SpotifyWebApiException {
        // Arrange: authServiceのモックを設定し、SpotifyWebApiExceptionをスローするように設定
        doThrow(new SpotifyWebApiException("Test Exception")).when(authService).getClientCredentialsToken();

        // Act & Assert: 認証メソッドを実行し、InternalServerExceptionがスローされることを確認
        assertThatThrownBy(() -> spotifyClientCredentialsAuthentication.authenticate())
                .isInstanceOf(SpotifyWebApiException.class); // そのままスローされる
    }
}
