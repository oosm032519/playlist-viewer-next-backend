// UserAuthenticationServiceTest.java

package com.github.oosm032519.playlistviewernext.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * UserAuthenticationServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
class UserAuthenticationServiceTest {

    @Mock
    private OAuth2User principal;

    private final UserAuthenticationService userAuthenticationService = new UserAuthenticationService();

    /**
     * アクセストークンが正常に取得できる場合のテスト
     */
    @Test
    void getAccessToken_成功時() {
        // Arrange: モックのOAuth2Userから返されるアクセストークンを設定
        String accessToken = "validAccessToken";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("access_token", accessToken);
        when(principal.getAttributes()).thenReturn(attributes);

        // Act: アクセストークンを取得
        String result = userAuthenticationService.getAccessToken(principal);

        // Assert: 取得したアクセストークンが期待通りであることを確認
        assertThat(result).isEqualTo(accessToken);
    }

    /**
     * principalがnullの場合のテスト
     */
    @Test
    void getAccessToken_認証されていない場合() {
        // Act: nullのprincipalでアクセストークンを取得
        String result = userAuthenticationService.getAccessToken(null);

        // Assert: 取得したアクセストークンがnullであることを確認
        assertThat(result).isNull();
    }

    /**
     * アクセストークンが存在しない場合のテスト
     */
    @Test
    void getAccessToken_アクセストークンがない場合() {
        // Arrange: モックのOAuth2Userから返される属性にアクセストークンが含まれないように設定
        when(principal.getAttributes()).thenReturn(new HashMap<>());

        // Act: アクセストークンを取得
        String result = userAuthenticationService.getAccessToken(principal);

        // Assert: 取得したアクセストークンがnullであることを確認
        assertThat(result).isNull();
    }
}
