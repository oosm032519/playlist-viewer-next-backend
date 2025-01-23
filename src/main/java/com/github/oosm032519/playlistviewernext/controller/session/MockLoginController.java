package com.github.oosm032519.playlistviewernext.controller.session;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/mock-login")
@ConditionalOnProperty(name = "spotify.mock.enabled", havingValue = "true")
public class MockLoginController {

    private static final Logger logger = LoggerFactory.getLogger(MockLoginController.class);

    @Value("${frontend.url}")
    public String frontendUrl;

    @Autowired
    public RedisTemplate<String, String> redisTemplate;

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

        // ダミーのユーザー情報とJWTトークンを生成
        String userId = "mock-user-id";
        String userName = "Mock User";
        String spotifyAccessToken = "mock-access-token";
        String fullSessionToken = "mock-full-session-token"; // JWTモック化は不要なので固定値でOK

        // セッションIDの生成
        String sessionId = UUID.randomUUID().toString();

        // Redisにセッション情報を保存
        redisTemplate.opsForValue().set("session:" + sessionId, fullSessionToken); // fullSessionToken を session ID に紐付けて保存
        redisTemplate.expire("session:" + sessionId, 3600, TimeUnit.SECONDS);

        // sessionId Cookie を設定 (実処理と同じ設定)
        Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
        sessionIdCookie.setHttpOnly(true);
        sessionIdCookie.setSecure(true);
        sessionIdCookie.setPath("/");
        sessionIdCookie.setMaxAge(60 * 60 * 24 * 7); // 1週間
        response.addCookie(sessionIdCookie);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("userId", userId);
        responseBody.put("userName", userName);
        responseBody.put("status", "success");
        responseBody.put("message", "モックログインに成功しました。");

        logger.info("モックログイン処理が完了しました。セッションID: {}", sessionId);
        return ResponseEntity.ok(responseBody);
    }
}
