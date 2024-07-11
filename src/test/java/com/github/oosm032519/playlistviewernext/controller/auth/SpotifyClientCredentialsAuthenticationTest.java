// SpotifyClientCredentialsAuthenticationTest.java

package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
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
class SpotifyClientCredentialsAuthenticationTest {

    // SpotifyAuthServiceのモックオブジェクト
    @Mock
    private SpotifyAuthService authService;

    // テスト対象のSpotifyClientCredentialsAuthenticationオブジェクト
    @InjectMocks
    private SpotifyClientCredentialsAuthentication authController;

    // 各テストメソッドの前に実行される設定メソッド
    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    // 認証が成功する場合のテストメソッド
    @Test
    void authenticate_Successfully() throws IOException, ParseException, SpotifyWebApiException {
        // When: 認証メソッドを呼び出す
        authController.authenticate();

        // Then: authServiceのgetClientCredentialsTokenメソッドが呼び出されることを検証
        verify(authService).getClientCredentialsToken();
    }

    // 認証時に例外が発生する場合のテストメソッド
    @Test
    void authenticate_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given: authServiceのgetClientCredentialsTokenメソッドがRuntimeExceptionをスローするように設定
        doThrow(new RuntimeException("Auth error")).when(authService).getClientCredentialsToken();

        // When: 認証メソッドを呼び出す
        try {
            authController.authenticate();
        } catch (RuntimeException e) {
            // Then: スローされた例外のメッセージが期待通りであることを検証
            assertThat(e.getMessage()).isEqualTo("認証エラー");
        }

        // authServiceのgetClientCredentialsTokenメソッドが呼び出されたことを検証
        verify(authService).getClientCredentialsToken();
    }
}
