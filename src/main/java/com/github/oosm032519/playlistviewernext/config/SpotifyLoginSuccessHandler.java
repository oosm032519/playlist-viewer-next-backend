package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Spotify認証成功時の処理を行うハンドラークラス。
 * ユーザー情報の取得、JWTトークンの生成、一時トークンの発行などを行います。
 */
@Component
public class SpotifyLoginSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyLoginSuccessHandler.class);

    private final JwtUtil jwtUtil;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SpotifyApi spotifyApi;

    /**
     * コンストラクタ。
     *
     * @param jwtUtil JWTユーティリティクラス
     */
    public SpotifyLoginSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    /**
     * 認証成功時の処理を行います。
     *
     * @param request        HTTPリクエスト
     * @param response       HTTPレスポンス
     * @param authentication 認証情報
     * @throws IOException リダイレクト時に発生する可能性がある例外
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String userId = oauth2User.getAttribute("id");
        String spotifyAccessToken = oauth2User.getAttribute("access_token");

        // Spotifyユーザー名を取得
        String userName = getSpotifyUserName(userId, spotifyAccessToken);

        // JWTトークンの生成
        Map<String, Object> fullSessionClaims = new HashMap<>();
        fullSessionClaims.put("sub", userId);
        fullSessionClaims.put("name", userName);
        fullSessionClaims.put("spotify_access_token", spotifyAccessToken);
        String fullSessionToken = jwtUtil.generateToken(fullSessionClaims);

        // 一時トークンとセッションIDの生成
        String temporaryToken = UUID.randomUUID().toString();
        String sessionId = UUID.randomUUID().toString();

        // Redisに一時トークンとセッション情報を保存
        redisTemplate.opsForValue().set("temp:" + temporaryToken, sessionId, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("session:" + sessionId, fullSessionToken);
        redisTemplate.expire("session:" + sessionId, 3600, TimeUnit.SECONDS);

        // フロントエンドにリダイレクト
        response.sendRedirect(frontendUrl + "#token=" + temporaryToken);
    }

    /**
     * SpotifyAPIを使用してユーザー名を取得します。
     *
     * @param userId             ユーザーID
     * @param spotifyAccessToken Spotifyアクセストークン
     * @return ユーザー名（取得できない場合はユーザーID）
     */
    private String getSpotifyUserName(String userId, String spotifyAccessToken) {
        try {
            spotifyApi.setAccessToken(spotifyAccessToken);
            User user = spotifyApi.getCurrentUsersProfile().build().execute();
            return user.getDisplayName();
        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            logger.error("Spotifyユーザー名の取得中にエラーが発生しました。 userId: {}, spotifyAccessToken: {}", userId, spotifyAccessToken, e);
            // エラーが発生した場合は、デフォルトでuserIdを返す
            return userId;
        }
    }
}
