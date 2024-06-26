package com.github.oosm032519.playlistviewernext.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        // アクセストークンを取得
        String accessToken = userRequest.getAccessToken().getTokenValue();

        // 新しい変更可能なマップを作成し、元の属性をコピー
        Map<String, Object> mutableAttributes = new HashMap<>(user.getAttributes());

        // アクセストークンを属性に追加
        mutableAttributes.put("access_token", accessToken);

        // 新しい DefaultOAuth2User オブジェクトを作成して返す
        return new DefaultOAuth2User(
                user.getAuthorities(),
                mutableAttributes,
                "id"
        );
    }
}
