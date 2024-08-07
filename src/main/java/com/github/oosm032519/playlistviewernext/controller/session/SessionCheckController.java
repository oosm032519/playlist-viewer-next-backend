package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionCheckController {

    private static final Logger logger = LoggerFactory.getLogger(SessionCheckController.class);

    private final JwtUtil jwtUtil;

    @Lazy
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public SessionCheckController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        logger.info("SessionCheckControllerが初期化されました。JwtUtil: {}", jwtUtil);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession(HttpServletRequest request) {
        logger.info("セッションチェックが開始されました。リクエスト: {}", request);

        String sessionId = null;
        String userId = null;
        String userName = null;
        String spotifyAccessToken = null;

        String jwt = getJwtFromAuthorizationHeader(request);
        logger.debug("Authorizationヘッダーから取得したトークン: {}", jwt != null ? jwt.substring(0, Math.min(jwt.length(), 10)) + "..." : "null");

        if (jwt != null) {
            try {
                logger.info("JWTトークンの検証を開始します。");
                Map<String, Object> claims = jwtUtil.validateToken(jwt);
                sessionId = (String) claims.get("session_id");
                logger.info("JWTトークンの検証が成功しました。セッションID: {}", sessionId);

                // Redisからフルセッション情報を取得
                String fullSessionToken = redisTemplate.opsForValue().get("session:" + sessionId);
                if (fullSessionToken != null) {
                    Map<String, Object> fullSessionClaims = jwtUtil.validateToken(fullSessionToken);
                    userId = (String) fullSessionClaims.get("sub");
                    userName = (String) fullSessionClaims.get("name");
                    spotifyAccessToken = (String) fullSessionClaims.get("spotify_access_token");
                    logger.info("Redisから取得したセッション情報 - ユーザーID: {}, ユーザー名: {}", userId, userName);
                } else {
                    logger.warn("Redisにセッション情報が見つかりません。セッションID: {}", sessionId);
                }
            } catch (JOSEException | ParseException e) {
                logger.warn("JWTトークンの検証エラー: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("JWTトークンがAuthorizationヘッダーに存在しません。");
        }

        Map<String, Object> response = new HashMap<>();
        if (userId != null) {
            logger.info("認証成功。ユーザーID: {}", userId);
            response.put("status", "success");
            response.put("message", "User is authenticated");
            response.put("userId", userId);
            response.put("userName", userName);
            response.put("spotifyAccessToken", spotifyAccessToken);
        } else {
            logger.warn("認証されていないユーザーがセッションチェックを試みました。");
            response.put("status", "error");
            response.put("message", "User not authenticated");
        }
        return ResponseEntity.ok(response);
    }

    private String getJwtFromAuthorizationHeader(HttpServletRequest request) {
        logger.debug("AuthorizationヘッダーからJWTトークンを取得します。");
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            logger.info("JWTトークンが見つかりました。");
            return authorizationHeader.substring(7);
        }
        logger.warn("JWTトークンが見つかりませんでした。");
        return null;
    }
}
