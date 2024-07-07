package com.github.oosm032519.playlistviewernext.service.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SpotifyOAuth2UserService extends DefaultOAuth2UserService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyOAuth2UserService.class);
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String USER_NAME_ATTRIBUTE_KEY = "id";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        logger.debug("loadUser メソッドが呼び出されました。");

        OAuth2User user = super.loadUser(userRequest);
        logger.debug("OAuth2User が正常にロードされました: {}", user);

        String accessToken = userRequest.getAccessToken().getTokenValue();
        logger.debug("アクセストークンを取得しました: {}", accessToken);

        Map<String, Object> enhancedAttributes = new java.util.HashMap<>(Map.copyOf(user.getAttributes()));
        enhancedAttributes.put(ACCESS_TOKEN_KEY, accessToken);
        logger.debug("拡張された属性: {}", enhancedAttributes);

        DefaultOAuth2User enhancedUser = new DefaultOAuth2User(
                user.getAuthorities(),
                enhancedAttributes,
                USER_NAME_ATTRIBUTE_KEY
        );
        logger.debug("拡張されたOAuth2User を作成しました: {}", enhancedUser);

        return enhancedUser;
    }
}
