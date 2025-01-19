package com.github.oosm032519.playlistviewernext.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * ユーザー認証サービスクラス
 * OAuth2ユーザーのアクセストークンを取得するメソッドを提供する
 */
@Component
public class UserAuthenticationService {

    @Value("${spotify.mock.enabled}")
    private boolean mockEnabled;

    /**
     * 現在認証されているユーザーのアクセストークンを取得する
     *
     * @param principal 認証されたOAuth2ユーザー
     * @return アクセストークン、または認証されていない場合はnull
     */
    public String getAccessToken(@AuthenticationPrincipal OAuth2User principal) {
        if (mockEnabled) {
            return "mock-access-token";
        }

        if (isPrincipalNull(principal)) {
            return null;
        }
        return extractAccessToken(principal);
    }

    /**
     * principalがnullかどうかをチェックする
     *
     * @param principal 認証されたOAuth2ユーザー
     * @return principalがnullの場合はtrue、それ以外はfalse
     */
    private boolean isPrincipalNull(OAuth2User principal) {
        return principal == null;
    }

    /**
     * principalの属性からアクセストークンを取得する
     *
     * @param principal 認証されたOAuth2ユーザー
     * @return アクセストークン
     */
    private String extractAccessToken(OAuth2User principal) {
        return (String) principal.getAttributes().get("spotify_access_token"); // "spotify_access_token" 属性を取得
    }
}
