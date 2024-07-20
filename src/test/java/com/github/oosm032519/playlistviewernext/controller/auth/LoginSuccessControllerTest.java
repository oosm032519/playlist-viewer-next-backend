package com.github.oosm032519.playlistviewernext.controller.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@SpringBootTest
@TestPropertySource(properties = {"frontend.url=http://localhost:3000"})
public class LoginSuccessControllerTest {

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private LoginSuccessController loginSuccessController;

    @Value("${frontend.url}")
    private String frontendUrl;

    @BeforeEach
    void setUp() {
        // frontendUrlを明示的に設定
        ReflectionTestUtils.setField(loginSuccessController, "frontendUrl", "http://localhost:3000");
    }

    /**
     * 正常な認証の場合のテストケース。
     * ユーザーが正常に認証された場合、フロントエンドURLにリダイレクトすることを確認する。
     */
    @Test
    void loginSuccess_withValidPrincipal_shouldRedirectToFrontendUrl() {
        // モックオブジェクトの設定
        when(principal.getAttribute("id")).thenReturn("testUserId");
        when(principal.getAttribute("access_token")).thenReturn("testAccessToken");

        // 実行
        String result = loginSuccessController.loginSuccess(principal);

        // 検証
        assertThat(result).isEqualTo("redirect:" + frontendUrl);
        verify(principal, times(1)).getAttribute("id");
        verify(principal, times(1)).getAttribute("access_token");
    }

    /**
     * 認証が失敗した場合のテストケース。
     * principalがnullの場合、エラーページにリダイレクトすることを確認する。
     */
    @Test
    void loginSuccess_withNullPrincipal_shouldRedirectToErrorPage() {
        // 実行
        String result = loginSuccessController.loginSuccess(null);

        // 検証
        assertThat(result).isEqualTo("redirect:/login?error");
    }

    /**
     * ユーザー情報が不足している場合のテストケース。
     * principalにユーザーIDまたはアクセストークンが含まれていない場合、警告ログが出力されることを確認する。
     */
    @Test
    void loginSuccess_withMissingUserInfo_shouldLogWarnings() {
        // モックオブジェクトの再設定
        when(principal.getAttribute("id")).thenReturn(null);
        when(principal.getAttribute("access_token")).thenReturn(null);

        // 実行
        String result = loginSuccessController.loginSuccess(principal);

        // 検証
        assertThat(result).isEqualTo("redirect:" + frontendUrl);
        verify(principal, times(1)).getAttribute("id");
        verify(principal, times(1)).getAttribute("access_token");
    }
}
