package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;


@ExtendWith(MockitoExtension.class)
class SpotifyClientCredentialsAuthenticationTest {

    @Mock
    private SpotifyAuthService authService;

    @InjectMocks
    private SpotifyClientCredentialsAuthentication spotifyClientCredentialsAuthentication;

    @Test
    void authenticate_success() throws SpotifyWebApiException {
        // SpotifyAuthServiceのモックを設定し、getClientCredentialsToken()が例外をスローしないようにする
        doNothing().when(authService).getClientCredentialsToken();

        // authenticate()メソッドを実行
        spotifyClientCredentialsAuthentication.authenticate();
    }

    @Test
    void authenticate_throwsSpotifyWebApiException() throws SpotifyWebApiException {
        // SpotifyAuthServiceのモックを設定し、getClientCredentialsToken()がSpotifyWebApiExceptionをスローするようにする
        doThrow(SpotifyWebApiException.class).when(authService).getClientCredentialsToken();

        // authenticate()メソッドを実行し、SpotifyWebApiExceptionがスローされることを確認
        org.junit.jupiter.api.Assertions.assertThrows(SpotifyWebApiException.class, () -> spotifyClientCredentialsAuthentication.authenticate());
    }

    @Test
    void getRequestParams() {
        // MockHttpServletRequestを使用して、テスト用のリクエストパラメータを設定
        MockHttpServletRequest mockRequest = new MockHttpServletRequest();
        mockRequest.addParameter("param1", "value1");
        mockRequest.addParameter("param2", "value2", "value3");

        // テスト対象のクラスにMockHttpServletRequestを注入
        spotifyClientCredentialsAuthentication = new SpotifyClientCredentialsAuthentication(authService, mockRequest);

        // getRequestParams()メソッドを実行し、結果を取得
        String params = spotifyClientCredentialsAuthentication.getRequestParams();

        // 結果が期待値と一致することを確認
        assertThat(params).isEqualTo("param1=value1&param2=value2,value3");


        // パラメータがない場合のテスト
        mockRequest = new MockHttpServletRequest();
        spotifyClientCredentialsAuthentication = new SpotifyClientCredentialsAuthentication(authService, mockRequest);
        params = spotifyClientCredentialsAuthentication.getRequestParams();
        assertThat(params).isEmpty();
    }
}
