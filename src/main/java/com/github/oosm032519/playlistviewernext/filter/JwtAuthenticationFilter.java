package com.github.oosm032519.playlistviewernext.filter;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import com.nimbusds.jose.JOSEException;
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
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.text.ParseException;
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

        String sessionId = extractSessionIdFromCookie(request);
        if (sessionId != null) {
            try {
                // Redisからセッション情報（JWTトークン）を取得
                String jwtToken = redisTemplate.opsForValue().get("session:" + sessionId);
                if (jwtToken == null) {
                    logger.warn("セッション情報がRedisに見つかりません - セッションID: {}", sessionId);
                    throw new BadCredentialsException("セッション情報が見つかりません");
                }

                // JWTトークンを検証
                Map<String, Object> claims = jwtUtil.validateToken(jwtToken);
                if (claims == null) {
                    logger.warn("JWTトークンの検証に失敗しました - セッションID: {}", sessionId);
                    throw new BadCredentialsException("無効なセッション");
                }

                if (validateClaims(claims)) {
                    String userId = (String) claims.get("sub");
                    String userName = (String) claims.get("name");
                    String spotifyAccessToken = (String) claims.get("spotify_access_token");

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
                    logger.warn("無効なJWTクレーム");
                    throw new BadCredentialsException("無効なJWTクレーム");
                }
            } catch (JOSEException | ParseException e) {
                logger.error("JWTトークンの検証エラー", e);
                handleAuthenticationError(response, "JWTトークンの検証エラー: " + e.getMessage());
                return;
            } catch (BadCredentialsException e) {
                logger.error("無効なJWTクレーム", e);
                handleAuthenticationError(response, "無効なJWTクレーム: " + e.getMessage());
                return;
            }
        } else {
            logger.warn("セッションIDが見つかりません");
        }

        logger.debug("フィルターチェーンを続行します");
        chain.doFilter(request, response);
        logger.debug("doFilterInternalメソッドが完了しました");
    }

    private String extractSessionIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("session_id".equals(cookie.getName())) {
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
                logger.warn("無効な発行者: {}", issuer);
                throw new BadCredentialsException("無効な発行者");
            }

            String audience = claims.get("aud") instanceof List ?
                    ((List<?>) claims.get("aud")).get(0).toString() :
                    (String) claims.get("aud");
            logger.debug("対象者の検証: {}", audience);
            if (!jwtUtil.getAudience().equals(audience)) {
                logger.warn("無効な対象者: {}", audience);
                throw new BadCredentialsException("無効な対象者");
            }

            Date expiration = (Date) claims.get("exp");
            logger.debug("有効期限の検証: {}", expiration);
            if (expiration.before(new Date())) {
                logger.warn("トークンの有効期限切れ: {}", expiration);
                throw new BadCredentialsException("トークンの有効期限が切れています");
            }

            logger.debug("必須クレームの確認");
            if (!claims.containsKey("sub") || !claims.containsKey("name") || !claims.containsKey("spotify_access_token")) {
                logger.warn("必須クレームが不足しています");
                throw new BadCredentialsException("必須クレームが不足しています");
            }

            logger.info("クレームの検証が成功しました");
            return true;
        } catch (BadCredentialsException e) {
            logger.error("クレーム検証エラー", e);
            return false;
        }
    }

    private void handleAuthenticationError(HttpServletResponse response, String errorMessage) throws IOException {
        logger.error("認証エラーの処理: {}", errorMessage);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write("{\"error\": \"" + errorMessage + "\"}");
        logger.debug("エラーレスポンスが送信されました");
    }
}
