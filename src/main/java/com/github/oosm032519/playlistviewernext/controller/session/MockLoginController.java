package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/session/mock-login")
@ConditionalOnProperty(name = "spotify.mock.enabled", havingValue = "true")
public class MockLoginController {

    private static final Logger logger = LoggerFactory.getLogger(MockLoginController.class);

    @Value("${frontend.url}")
    public String frontendUrl;

    @Autowired
    public RedisTemplate<String, String> redisTemplate;

    @Autowired
    private JwtUtil jwtUtil; // JwtUtil をAutowired

    /**
     * モックログインエンドポイント
     * モックのセッションIDとユーザー情報を生成し、クッキーを設定して返却する
     *
     * @param response HTTPレスポンス
     * @return モックユーザー情報を含むResponseEntity
     */
    @PostMapping
    public ResponseEntity<?> mockLogin(HttpServletResponse response) {
        logger.info("モックログイン処理を開始します。");

        try {
            // モックユーザー情報を生成
            String userId = "mock-user-id";
            String userName = "Mock User";
            String spotifyAccessToken = UUID.randomUUID().toString();

            // JWTトークンの生成
            Map<String, Object> fullSessionClaims = new HashMap<>();
            fullSessionClaims.put("sub", userId);
            fullSessionClaims.put("name", userName);
            fullSessionClaims.put("spotify_access_token", spotifyAccessToken);
            String fullSessionToken = jwtUtil.generateToken(fullSessionClaims);
            logger.info("JWTトークンを生成しました: {}", fullSessionToken);

            // セッションIDの生成
            String sessionId = UUID.randomUUID().toString();

            // Redisにセッション情報を保存 (JWTトークンをセッション情報として保存)
            redisTemplate.opsForValue().set("session:" + sessionId, fullSessionToken);
            redisTemplate.expire("session:" + sessionId, 3600, TimeUnit.SECONDS);
            logger.info("Redisにセッション情報を保存しました。セッションID: {}", sessionId);

            // sessionId Cookie を設定 (実処理と同じ設定)
            Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
            sessionIdCookie.setHttpOnly(true);
            sessionIdCookie.setSecure(true);
            sessionIdCookie.setPath("/");
            sessionIdCookie.setMaxAge(60 * 60 * 24 * 7); // 1週間
            response.addCookie(sessionIdCookie);

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("userId", userId); // userId をレスポンスボディに含める
            responseBody.put("userName", userName); // userName をレスポンスボディに含める
            responseBody.put("status", "success");
            responseBody.put("message", "モックログインに成功しました。");
            responseBody.put("sessionId", sessionId);

            logger.info("モックログイン処理が完了しました。セッションID: {}, ユーザーID: {}, ユーザー名: {}", sessionId, userId, userName);
            return ResponseEntity.ok()
                    .header("Set-Cookie", sessionIdCookie.toString()) // Set-Cookie ヘッダーを追加
                    .body(responseBody);
        } catch (Exception e) {
            logger.error("モックログイン処理中にエラーが発生しました。", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "error", "message", "モックログイン処理中にエラーが発生しました。"));
        }
    }
}
