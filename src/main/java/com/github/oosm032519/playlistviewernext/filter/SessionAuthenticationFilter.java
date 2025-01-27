package com.github.oosm032519.playlistviewernext.filter;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.util.ServletUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * セッションIDを使用した認証フィルター
 * リクエストごとにセッションIDを検証し、認証情報をSecurityContextに設定する
 */
@Component
public class SessionAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(SessionAuthenticationFilter.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

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

        String sessionId = ServletUtil.extractSessionIdFromRequest(request);
        if (sessionId != null) {
            logger.info("セッションIDをCookieから取得しました: {}", sessionId);
            try {
                // Redisからセッション情報を取得
                Map<Object, Object> sessionData = redisTemplate.opsForHash().entries("session:" + sessionId);
                if (sessionData == null || sessionData.isEmpty()) {
                    logger.warn("セッション情報がRedisに見つかりません - セッションID: {}", sessionId);
                    throw new AuthenticationException(
                            HttpStatus.UNAUTHORIZED,
                            "セッションが有効期限切れか、無効です。再度ログインしてください。"
                    );
                }

                logger.info("Redisからセッション情報を取得しました。認証情報を作成します。");

                // ユーザー情報を取得
                String userId = (String) sessionData.get("userId");
                String userName = (String) sessionData.get("userName");
                String spotifyAccessToken = (String) sessionData.get("spotifyAccessToken");

                logger.info("ユーザー認証成功 - ユーザーID: {}, ユーザー名: {}", userId, userName);

                // OAuth2Userオブジェクトを作成
                Map<String, Object> attributes = new HashMap<>();
                attributes.put("id", userId);
                attributes.put("name", userName);
                attributes.put("spotify_access_token", spotifyAccessToken);
                OAuth2User oauth2User = new DefaultOAuth2User(
                        Collections.singletonList((GrantedAuthority) () -> "ROLE_USER"),
                        attributes,
                        "id"
                );

                // 認証トークンを作成し、SecurityContextHolderに設定
                OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                        oauth2User,
                        oauth2User.getAuthorities(),
                        "spotify" // clientRegistrationId (任意の値)
                );
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (AuthenticationException e) {
                logger.error("認証エラーが発生しました (doFilterInternal): ", e);
                throw e;
            } catch (Exception e) {
                logger.error("セッション情報の取得中にエラーが発生しました", e);
                throw new AuthenticationException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "セッション情報の取得中にエラーが発生しました。",
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
}
