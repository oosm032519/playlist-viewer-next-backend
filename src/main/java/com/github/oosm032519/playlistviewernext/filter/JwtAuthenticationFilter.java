package com.github.oosm032519.playlistviewernext.filter;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.ErrorResponse;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
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

/**
 * JWTを使用した認証フィルター
 * リクエストごとにJWTトークンを検証し、認証情報をSecurityContextに設定する
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtUtil jwtUtil;

    @Lazy
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * コンストラクタ
     *
     * @param jwtUtil JWTユーティリティクラス
     */
    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        logger.info("JwtAuthenticationFilterが初期化されました");
    }

    /**
     * フィルター処理を実行する
     *
     * @param request  HTTPリクエスト
     * @param response HTTPレスポンス
     * @param chain    フィルターチェーン
     * @throws ServletException サーブレット例外
     * @throws IOException      I/O例外
     */
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
                    logger.warn("セッション情報がRedisに見つかりません - セッションID: {}", sessionId);
                    throw new AuthenticationException(
                            HttpStatus.UNAUTHORIZED,
                            "SESSION_NOT_FOUND",
                            "セッションが有効期限切れか、無効です。再度ログインしてください。"
                    );
                }

                // フルセッショントークンを検証
                Map<String, Object> fullSessionClaims = jwtUtil.validateToken(fullSessionToken);
                if (fullSessionClaims == null) {
                    logger.warn("フルセッショントークンの検証に失敗しました - セッションID: {}", sessionId);
                    throw new AuthenticationException(
                            HttpStatus.UNAUTHORIZED,
                            "INVALID_SESSION",
                            "セッションが有効期限切れか、無効です。再度ログインしてください。"
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
                }
            } catch (InvalidRequestException e) {
                // InvalidRequestException はそのまま再スロー
                HttpStatus status = e.getHttpStatus();
                String errorCode = e.getErrorCode();
                String message = e.getMessage();
                String details = e.getDetails();

                // エラーログに記録
                logger.error("Invalid request error occurred during JWT validation: {} - {} - {}", status, errorCode, message, e);

                // エラーレスポンスを返す
                ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);
                response.setStatus(status.value());
                response.setContentType("application/json");
                response.getWriter().write(errorResponse.toString());
                return;
            } catch (Exception e) {
                // その他の例外は AuthenticationException に変換
                logger.error("JWTトークンの検証エラー", e);
                throw new AuthenticationException(
                        HttpStatus.UNAUTHORIZED,
                        "JWT_VALIDATION_ERROR",
                        "ログイン処理中にエラーが発生しました。再度ログインしてください。",
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

    /**
     * リクエストからセッションIDを抽出する
     *
     * @param request HTTPリクエスト
     * @return セッションID、見つからない場合はnull
     */
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

    /**
     * JWTクレームを検証する
     *
     * @param claims 検証するクレーム
     * @return 検証が成功した場合はtrue
     * @throws InvalidRequestException クレームが無効な場合
     */
    private boolean validateClaims(Map<String, Object> claims) {
        logger.debug("クレームの検証を開始します: {}", claims);
        try {
            // 発行者の検証
            String issuer = (String) claims.get("iss");
            logger.debug("発行者の検証: {}", issuer);
            if (!jwtUtil.getIssuer().equals(issuer)) {
                logger.warn("無効な発行者: {}", issuer);
                throw new InvalidRequestException(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_ISSUER",
                        "無効な発行者です。"
                );
            }

            // 対象者の検証
            String audience = claims.get("aud") instanceof List ?
                    ((List<?>) claims.get("aud")).get(0).toString() :
                    (String) claims.get("aud");
            logger.debug("対象者の検証: {}", audience);
            if (!jwtUtil.getAudience().equals(audience)) {
                logger.warn("無効な対象者: {}", audience);
                throw new InvalidRequestException(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_AUDIENCE",
                        "無効な対象者です。"
                );
            }

            // 有効期限の検証
            Date expiration = (Date) claims.get("exp");
            logger.debug("有効期限の検証: {}", expiration);
            if (expiration.before(new Date())) {
                logger.warn("トークンの有効期限切れ: {}", expiration);
                throw new InvalidRequestException(
                        HttpStatus.BAD_REQUEST,
                        "TOKEN_EXPIRED",
                        "トークンの有効期限が切れています。"
                );
            }

            // 必須クレームの確認
            logger.debug("必須クレームの確認");
            if (!claims.containsKey("sub") || !claims.containsKey("name") || !claims.containsKey("spotify_access_token")) {
                logger.warn("必須クレームが不足しています");
                throw new InvalidRequestException(
                        HttpStatus.BAD_REQUEST,
                        "MISSING_CLAIMS",
                        "必須クレームが不足しています。"
                );
            }

            logger.info("クレームの検証が成功しました");
            return true;
        } catch (InvalidRequestException e) {
            logger.error("クレーム検証エラー", e);
            throw e; // InvalidRequestException を再スロー
        }
    }
}
