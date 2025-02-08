package com.github.oosm032519.playlistviewernext.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAuthenticationServiceのテスト")
class UserAuthenticationServiceTest {

    @Mock
    private OAuth2User principal;

    private UserAuthenticationService userAuthenticationService;

    @BeforeEach
    void setUp() {
        userAuthenticationService = new UserAuthenticationService();
    }

    @Nested
    @DisplayName("getAccessTokenメソッドのテスト")
    class GetAccessTokenTest {

        /**
         * OAuth2Userから有効なアクセストークンが取得できることを確認する。
         */
        @Test
        @DisplayName("有効なアクセストークンが取得できる場合")
        void getAccessToken_WithValidToken_ReturnsToken() {
            // Arrange: テストデータの準備
            String expectedToken = "validAccessToken";
            Map<String, Object> attributes = Collections.singletonMap("spotify_access_token", expectedToken);
            when(principal.getAttributes()).thenReturn(attributes);

            // Act: テスト対象メソッドの実行
            String actualToken = userAuthenticationService.getAccessToken(principal);

            // Assert: 結果の検証
            assertThat(actualToken).isEqualTo(expectedToken);
        }

        /**
         * 認証されていない場合（principalがnull）に、nullが返されることを確認する。
         */
        @Test
        @DisplayName("認証されていない場合（principalがnull）")
        void getAccessToken_WithNullPrincipal_ReturnsNull() {
            // Act: テスト対象メソッドの実行
            String result = userAuthenticationService.getAccessToken(null);

            // Assert: 結果の検証
            assertThat(result).isNull();
        }

        /**
         * OAuth2Userの属性にアクセストークンが存在しない場合に、nullが返されることを確認する。
         */
        @Test
        @DisplayName("アクセストークンが存在しない場合")
        void getAccessToken_WithNoAccessToken_ReturnsNull() {
            // Arrange: モックの設定
            when(principal.getAttributes()).thenReturn(Collections.emptyMap());

            // Act: テスト対象メソッドの実行
            String result = userAuthenticationService.getAccessToken(principal);

            // Assert: 結果の検証
            assertThat(result).isNull();
        }

        /**
         * OAuth2Userの属性にアクセストークンがnullで存在する場合に、nullが返されることを確認する。
         */
        @Test
        @DisplayName("アクセストークンがnullの場合")
        void getAccessToken_WithNullAccessToken_ReturnsNull() {
            // Arrange: テストデータの準備
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("spotify_access_token", null);
            when(principal.getAttributes()).thenReturn(attributes);

            // Act: テスト対象メソッドの実行
            String result = userAuthenticationService.getAccessToken(principal);

            // Assert: 結果の検証
            assertThat(result).isNull();
        }
    }
}
