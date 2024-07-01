package com.github.oosm032519.playlistviewernext.service;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String USER_NAME_ATTRIBUTE_KEY = "id";

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User user = super.loadUser(userRequest);
        String accessToken = userRequest.getAccessToken().getTokenValue();

        Map<String, Object> enhancedAttributes = new java.util.HashMap<>(Map.copyOf(user.getAttributes()));
        enhancedAttributes.put(ACCESS_TOKEN_KEY, accessToken);

        return new DefaultOAuth2User(
                user.getAuthorities(),
                enhancedAttributes,
                USER_NAME_ATTRIBUTE_KEY
        );
    }
}
