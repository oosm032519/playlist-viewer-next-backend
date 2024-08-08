package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
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

        HttpSession session = request.getSession(false);
        String jsessionId = session != null ? session.getId() : null;
        String userId = null;
        String userName = null;
        String spotifyAccessToken = null;

        if (jsessionId != null) {
            try {
                logger.info("JSESSIONIDを使用してセッション情報を取得します。JSESSIONID: {}", jsessionId);
                String fullSessionToken = redisTemplate.opsForValue().get("session:" + jsessionId);
                if (fullSessionToken != null) {
                    Map<String, Object> fullSessionClaims = jwtUtil.validateToken(fullSessionToken);
                    userId = (String) fullSessionClaims.get("sub");
                    userName = (String) fullSessionClaims.get("name");
                    spotifyAccessToken = (String) fullSessionClaims.get("spotify_access_token");
                    logger.info("Redisから取得したセッション情報 - ユーザーID: {}, ユーザー名: {}", userId, userName);
                } else {
                    logger.warn("Redisにセッション情報が見つかりません。JSESSIONID: {}", jsessionId);
                }
            } catch (JOSEException | ParseException e) {
                logger.warn("セッション情報の検証エラー: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("JSESSIONIDが存在しません。");
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
}
