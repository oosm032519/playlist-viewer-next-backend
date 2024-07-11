// SpotifyOAuth2UserService.java

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

/**
 * SpotifyOAuth2UserService クラスは、Spotify の OAuth2 認証を処理するサービスです。
 * OAuth2UserService を拡張し、ユーザー情報のロードと拡張を行います。
 */
@Service
public class SpotifyOAuth2UserService extends DefaultOAuth2UserService {

    // ロガーの初期化
    private static final Logger logger = LoggerFactory.getLogger(SpotifyOAuth2UserService.class);

    // アクセストークンのキー
    private static final String ACCESS_TOKEN_KEY = "access_token";

    // ユーザー名属性のキー
    private static final String USER_NAME_ATTRIBUTE_KEY = "id";

    /**
     * OAuth2UserRequest を使用してユーザー情報をロードし、拡張された OAuth2User を返します。
     *
     * @param userRequest OAuth2UserRequest オブジェクト
     * @return 拡張された OAuth2User オブジェクト
     * @throws OAuth2AuthenticationException 認証エラーが発生した場合
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // メソッドの呼び出しをログに記録
        logger.debug("loadUser メソッドが呼び出されました。");

        // 親クラスの loadUser メソッドを呼び出してユーザー情報を取得
        OAuth2User user = super.loadUser(userRequest);
        logger.debug("OAuth2User が正常にロードされました: {}", user);

        // アクセストークンを取得
        String accessToken = userRequest.getAccessToken().getTokenValue();
        logger.debug("アクセストークンを取得しました: {}", accessToken);

        // ユーザー属性を拡張してアクセストークンを追加
        Map<String, Object> enhancedAttributes = new java.util.HashMap<>(Map.copyOf(user.getAttributes()));
        enhancedAttributes.put(ACCESS_TOKEN_KEY, accessToken);
        logger.debug("拡張された属性: {}", enhancedAttributes);

        // 拡張された OAuth2User を作成
        DefaultOAuth2User enhancedUser = new DefaultOAuth2User(
                user.getAuthorities(),
                enhancedAttributes,
                USER_NAME_ATTRIBUTE_KEY
        );
        logger.debug("拡張されたOAuth2User を作成しました: {}", enhancedUser);

        // 拡張されたユーザーを返す
        return enhancedUser;
    }
}
