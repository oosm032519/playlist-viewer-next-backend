package com.github.oosm032519.playlistviewernext.filter;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.*;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    @Lazy
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        logger.info("JwtAuthenticationFilterが初期化されました");
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {
        logger.debug("doFilterInternalメソッドが開始されました - リクエストURI: {}", request.getRequestURI());

        String sessionId = extractSessionIdFromRequest(request);
        if (sessionId != null) {
            try {
                // Redisからセッション情報を取得
                String fullSessionToken = redisTemplate.opsForValue().get("session:" + sessionId);
                if (fullSessionToken == null) {
                    // セッション情報がない場合は PlaylistViewerNextException をスロー
                    logger.warn("セッション情報がRedisに見つかりません - セッションID: {}", sessionId);
                    throw new PlaylistViewerNextException(
                            HttpStatus.UNAUTHORIZED,
                            "SESSION_NOT_FOUND",
                            "セッション情報が見つかりません。"
                    );
                }

                // フルセッショントークンを検証
                Map<String, Object> fullSessionClaims = jwtUtil.validateToken(fullSessionToken);
                if (fullSessionClaims == null) {
                    // トークン検証に失敗した場合は PlaylistViewerNextException をスロー
                    logger.warn("フルセッショントークンの検証に失敗しました - セッションID: {}", sessionId);
                    throw new PlaylistViewerNextException(
                            HttpStatus.UNAUTHORIZED,
                            "INVALID_SESSION",
                            "無効なセッションです。"
                    );
                }

                if (validateClaims(fullSessionClaims)) {
                    String userId = (String) fullSessionClaims.get("sub");
                    String userName = (String) fullSessionClaims.get("name");
                    String spotifyAccessToken = (String) fullSessionClaims.get("spotify_access_token");

                    logger.info("ユーザー認証成功 - ユーザーID: {}, ユーザー名: {}", userId, userName);

                    // OAuth2AuthenticationToken を作成
                    Map<String, Object> attributes = new HashMap<>();
                    attributes.put("id", userId);
                    attributes.put("name", userName);
                    attributes.put("spotify_access_token", spotifyAccessToken);
                    OAuth2User oauth2User = new DefaultOAuth2User(
                            Collections.singletonList((GrantedAuthority) () -> "ROLE_USER"),
                            attributes,
                            "id"
                    );
                    OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                            oauth2User,
                            oauth2User.getAuthorities(),
                            "spotify"
                    );

                    // SecurityContextHolder に設定
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                } else {
                    // クレーム検証に失敗した場合は PlaylistViewerNextException をスロー
                    logger.warn("無効なJWTクレーム");
                    throw new PlaylistViewerNextException(
                            HttpStatus.UNAUTHORIZED,
                            "INVALID_JWT_CLAIMS",
                            "無効なJWTクレームです。"
                    );
                }
            } catch (Exception e) {
                // JWTトークンの検証中にエラーが発生した場合は PlaylistViewerNextException をスロー
                logger.error("JWTトークンの検証エラー", e);
                throw new PlaylistViewerNextException(
                        HttpStatus.UNAUTHORIZED,
                        "JWT_VALIDATION_ERROR",
                        "JWTトークンの検証中にエラーが発生しました。",
                        e
                );
            }
        } else {
            logger.warn("セッションIDが見つかりません");
        }

        logger.debug("フィルターチェーンを続行します");
        chain.doFilter(request, response);
        logger.debug("doFilterInternalメソッドが完了しました");
    }

    private String extractSessionIdFromRequest(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("sessionId".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean validateClaims(Map<String, Object> claims) {
        logger.debug("クレームの検証を開始します: {}", claims);
        try {
            String issuer = (String) claims.get("iss");
            logger.debug("発行者の検証: {}", issuer);
            if (!jwtUtil.getIssuer().equals(issuer)) {
                // 発行者検証に失敗した場合は PlaylistViewerNextException をスロー
                logger.warn("無効な発行者: {}", issuer);
                throw new PlaylistViewerNextException(
                        HttpStatus.UNAUTHORIZED,
                        "INVALID_ISSUER",
                        "無効な発行者です。"
                );
            }

            String audience = claims.get("aud") instanceof List ?
                    ((List<?>) claims.get("aud")).get(0).toString() :
                    (String) claims.get("aud");
            logger.debug("対象者の検証: {}", audience);
            if (!jwtUtil.getAudience().equals(audience)) {
                // 対象者検証に失敗した場合は PlaylistViewerNextException をスロー
                logger.warn("無効な対象者: {}", audience);
                throw new PlaylistViewerNextException(
                        HttpStatus.UNAUTHORIZED,
                        "INVALID_AUDIENCE",
                        "無効な対象者です。"
                );
            }

            Date expiration = (Date) claims.get("exp");
            logger.debug("有効期限の検証: {}", expiration);
            if (expiration.before(new Date())) {
                // 有効期限切れの場合は PlaylistViewerNextException をスロー
                logger.warn("トークンの有効期限切れ: {}", expiration);
                throw new PlaylistViewerNextException(
                        HttpStatus.UNAUTHORIZED,
                        "TOKEN_EXPIRED",
                        "トークンの有効期限が切れています。"
                );
            }

            logger.debug("必須クレームの確認");
            if (!claims.containsKey("sub") || !claims.containsKey("name") || !claims.containsKey("spotify_access_token")) {
                // 必須クレームがない場合は PlaylistViewerNextException をスロー
                logger.warn("必須クレームが不足しています");
                throw new PlaylistViewerNextException(
                        HttpStatus.UNAUTHORIZED,
                        "MISSING_CLAIMS",
                        "必須クレームが不足しています。"
                );
            }

            logger.info("クレームの検証が成功しました");
            return true;
        } catch (PlaylistViewerNextException e) {
            logger.error("クレーム検証エラー", e);
            return false;
        }
    }
}
