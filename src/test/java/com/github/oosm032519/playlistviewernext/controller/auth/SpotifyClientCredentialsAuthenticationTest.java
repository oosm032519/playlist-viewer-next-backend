package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyClientCredentialsAuthenticationTest {

    @Mock
    private SpotifyAuthService authService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SpotifyClientCredentialsAuthentication spotifyClientCredentialsAuthentication;

    @Test
    void authenticate_success() throws SpotifyWebApiException {
        // given: authServiceのモックを設定。特に何も返却値は設定しない
        doNothing().when(authService).getClientCredentialsToken();

        // when: 認証メソッドを実行
        spotifyClientCredentialsAuthentication.authenticate();

        // then: authServiceのgetClientCredentialsToken()が1回呼び出されたことを確認
        verify(authService, times(1)).getClientCredentialsToken();
    }

    @Test
    void authenticate_throwsSpotifyWebApiException() throws SpotifyWebApiException {
        // given: authServiceのモックを設定し、SpotifyWebApiExceptionをスローするように設定
        doThrow(SpotifyWebApiException.class).when(authService).getClientCredentialsToken();

        // when & then: 認証メソッドを実行し、SpotifyWebApiExceptionがスローされることを確認
        org.junit.jupiter.api.Assertions.assertThrows(SpotifyWebApiException.class, () -> {
            spotifyClientCredentialsAuthentication.authenticate();
        });
    }
}
