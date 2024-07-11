package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.LogoutService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

// LogoutControllerのテストクラス
class LogoutControllerTest {

    private LogoutController logoutController;

    @Mock
    private LogoutService logoutService;

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    /**
     * 各テストの前に実行されるセットアップメソッド
     * モックの初期化とLogoutControllerのインスタンス化を行う
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logoutController = new LogoutController(authorizedClientService) {
            @Override
            protected LogoutService createLogoutService(OAuth2AuthorizedClientService authorizedClientService) {
                return logoutService;
            }
        };
    }

    // ログアウトが成功する場合のテスト
    @Test
    void testLogoutSuccess() {
        // Arrange: ログアウトサービスのモックが何もしないように設定
        doNothing().when(logoutService).processLogout(request, response);

        // Act: ログアウトメソッドを呼び出し
        ResponseEntity<String> responseEntity = logoutController.logout(request, response);

        // Assert: ステータスコードとレスポンスボディの検証
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("ログアウトしました", responseEntity.getBody());
    }

    // ログアウトが失敗する場合のテスト
    @Test
    void testLogoutFailure() {
        // Arrange: ログアウトサービスのモックが例外をスローするように設定
        doThrow(new RuntimeException("Logout failed")).when(logoutService).processLogout(request, response);

        // Act: ログアウトメソッドを呼び出し
        ResponseEntity<String> responseEntity = logoutController.logout(request, response);

        // Assert: ステータスコードとレスポンスボディの検証
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("ログアウト処理中にエラーが発生しました", responseEntity.getBody());
    }
}
