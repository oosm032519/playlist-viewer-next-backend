package com.github.oosm032519.playlistviewernext.service;

import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private SessionService sessionService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);

        // アクセストークンをセッションに保存
        String accessToken = userRequest.getAccessToken().getTokenValue();
        sessionService.setAccessToken(accessToken);

        // ユーザーIDをセッションに保存
        String userId = user.getAttribute("id");
        sessionService.setUserId(userId);

        // 新しい変更可能なマップを作成し、元の属性をコピー
        Map<String, Object> mutableAttributes = new HashMap<>(user.getAttributes());

        // 新しい DefaultOAuth2User オブジェクトを作成して返す
        return new DefaultOAuth2User(user.getAuthorities(), mutableAttributes, "id");
    }
}
