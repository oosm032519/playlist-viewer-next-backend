// UserAuthenticationService.java

package com.github.oosm032519.playlistviewernext.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

/**
 * ユーザー認証サービスクラス
 * OAuth2ユーザーのアクセストークンを取得するメソッドを提供します。
 */
@Component
public class UserAuthenticationService {

    /**
     * 現在認証されているユーザーのアクセストークンを取得します。
     *
     * @param principal 認証されたOAuth2ユーザー
     * @return アクセストークン、または認証されていない場合はnull
     */
    public String getAccessToken(@AuthenticationPrincipal OAuth2User principal) {
        // principalがnullの場合、nullを返します
        if (principal == null) {
            return null;
        }
        // principalの属性からアクセストークンを取得して返します
        return (String) principal.getAttributes().get("access_token");
    }
}
