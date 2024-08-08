package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import com.nimbusds.jose.JOSEException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
        logger.info("セッションチェック開始 - リクエスト情報: メソッド={}, URI={}, リモートアドレス={}",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

        String sessionId = extractSessionIdFromRequest(request);
        logger.debug("取得されたセッションID: {}", sessionId);

        String userId = null;
        String userName = null;
        String spotifyAccessToken = null;

        if (sessionId != null) {
            logger.info("セッションIDが存在します。Redisからセッション情報を取得します。");
            try {
                String redisKey = "session:" + sessionId;
                String fullSessionToken = redisTemplate.opsForValue().get(redisKey);
                logger.debug("Redisキー: {}, 取得されたトークン: {}", redisKey, fullSessionToken != null ? "存在" : "なし");

                if (fullSessionToken != null) {
                    logger.info("Redisからセッショントークンを取得しました。トークンの検証を開始します。");
                    Map<String, Object> fullSessionClaims = jwtUtil.validateToken(fullSessionToken);
                    userId = (String) fullSessionClaims.get("sub");
                    userName = (String) fullSessionClaims.get("name");
                    spotifyAccessToken = (String) fullSessionClaims.get("spotify_access_token");
                    logger.info("トークン検証成功 - ユーザーID: {}, ユーザー名: {}, Spotifyトークン: {}",
                            userId, userName, spotifyAccessToken != null ? "取得済み" : "なし");
                } else {
                    logger.warn("Redisにセッション情報が見つかりません。セッションID: {}", sessionId);
                }
            } catch (JOSEException | ParseException e) {
                logger.error("セッション情報の検証中にエラーが発生しました。エラー詳細: ", e);
            }
        } else {
            logger.warn("有効なセッションIDが存在しません。未認証の可能性があります。");
        }

        Map<String, Object> response = new HashMap<>();
        if (userId != null) {
            logger.info("認証成功。レスポンスの作成を開始します。");
            response.put("status", "success");
            response.put("message", "User is authenticated");
            response.put("userId", userId);
            response.put("userName", userName);
            response.put("spotifyAccessToken", spotifyAccessToken);
            logger.debug("作成されたレスポンス: {}", response);
        } else {
            logger.warn("認証失敗。エラーレスポンスを作成します。");
            response.put("status", "error");
            response.put("message", "User not authenticated");
            logger.debug("作成されたエラーレスポンス: {}", response);
        }

        logger.info("セッションチェック完了。レスポンスステータス: {}", response.get("status"));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request, HttpServletResponse response) {
        logger.info("ログアウト処理開始");

        String sessionId = extractSessionIdFromRequest(request);
        logger.debug("取得されたセッションID: {}", sessionId);

        if (sessionId != null) {
            logger.info("セッションIDが存在します。Redisからセッション情報を削除します。");
            try {
                String redisKey = "session:" + sessionId;
                Boolean deleteResult = redisTemplate.delete(redisKey);
                logger.debug("Redisキー: {}, 削除結果: {}", redisKey, deleteResult);

                if (Boolean.TRUE.equals(deleteResult)) {
                    logger.info("Redisからセッション情報を削除しました。セッションID: {}", sessionId);

                    // セッションIDのCookieを削除
                    Cookie cookie = new Cookie("sessionId", null);
                    cookie.setMaxAge(0);
                    cookie.setPath("/");
                    response.addCookie(cookie);

                    Map<String, Object> responseBody = new HashMap<>();
                    responseBody.put("status", "success");
                    responseBody.put("message", "ログアウトしました。");
                    return ResponseEntity.ok(responseBody);
                } else {
                    logger.warn("Redisにセッション情報が見つかりません。セッションID: {}", sessionId);
                    Map<String, Object> responseBody = new HashMap<>();
                    responseBody.put("status", "error");
                    responseBody.put("message", "セッション情報が見つかりません。");
                    return ResponseEntity.badRequest().body(responseBody);
                }
            } catch (Exception e) {
                logger.error("ログアウト処理中にエラーが発生しました。エラー詳細: ", e);
                Map<String, Object> responseBody = new HashMap<>();
                responseBody.put("status", "error");
                responseBody.put("message", "ログアウト処理中にエラーが発生しました。");
                return ResponseEntity.internalServerError().body(responseBody);
            }
        } else {
            logger.warn("有効なセッションIDが存在しません。");
            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", "error");
            responseBody.put("message", "有効なセッションIDが存在しません。");
            return ResponseEntity.badRequest().body(responseBody);
        }
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
}
