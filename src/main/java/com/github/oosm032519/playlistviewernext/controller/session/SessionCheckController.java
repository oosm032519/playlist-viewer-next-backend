package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
import com.github.oosm032519.playlistviewernext.util.ServletUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * セッション管理を行うコントローラークラス
 */
@RestController
@RequestMapping("/api/session")
public class SessionCheckController {

    private static final Logger logger = LoggerFactory.getLogger(SessionCheckController.class);

    @Lazy
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * セッションをチェックするエンドポイント
     *
     * @param request HTTPリクエスト
     * @return セッションの状態を示すレスポンスエンティティ
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkSession(HttpServletRequest request) {
        logger.info("セッションチェック開始 - リクエスト情報: メソッド={}, URI={}, リモートアドレス={}",
                request.getMethod(), request.getRequestURI(), request.getRemoteAddr());

        String sessionId = ServletUtil.extractSessionIdFromRequest(request);
        logger.debug("取得されたセッションID: {}", sessionId);

        Map<String, Object> response = new HashMap<>();
        if (sessionId != null) {
            return handleSessionValidation(sessionId, response);
        } else {
            // セッションIDがない場合は AuthenticationException をスロー
            logger.warn("有効なセッションIDが存在しません。未認証の可能性があります。");
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "セッションが有効期限切れか、無効です。再度ログインしてください。"
            );
        }
    }

    /**
     * セッションの検証を行う
     *
     * @param sessionId セッションID
     * @param response  レスポンスマップ
     * @return レスポンスエンティティ
     */
    private ResponseEntity<?> handleSessionValidation(String sessionId, Map<String, Object> response) {
        logger.info("セッションIDが存在します。Redisからセッション情報を取得します。");
        String redisKey = "session:" + sessionId;
        Map<Object, Object> sessionData = redisTemplate.opsForHash().entries(redisKey);
        logger.debug("Redisキー: {}, 取得されたセッションデータ: {}", redisKey, sessionData != null ? "存在" : "なし");

        if (sessionData != null && !sessionData.isEmpty()) {
            logger.info("Redisからセッション情報を取得しました。");
            return createSuccessResponse(response, sessionData);
        } else {
            // Redisにセッション情報がない場合は AuthenticationException をスロー
            logger.warn("Redisにセッション情報が見つかりません。セッションID: {}", sessionId);
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "セッションが有効期限切れか、無効です。再度ログインしてください。"
            );
        }
    }

    /**
     * 成功レスポンスを作成する
     *
     * @param response    レスポンスマップ
     * @param sessionData セッションデータ
     * @return レスポンスエンティティ
     */
    private ResponseEntity<Map<String, Object>> createSuccessResponse(Map<String, Object> response, Map<Object, Object> sessionData) {
        String userId = (String) sessionData.get("userId");
        String userName = (String) sessionData.get("userName");
        String spotifyAccessToken = (String) sessionData.get("spotifyAccessToken");

        response.put("status", "success");
        response.put("message", "User is authenticated");
        response.put("userId", userId);
        response.put("userName", userName);
        response.put("spotifyAccessToken", spotifyAccessToken);

        logger.info("セッション検証成功 - ユーザーID: {}, ユーザー名: {}, Spotifyトークン: {}",
                userId, userName, spotifyAccessToken != null ? "取得済み" : "なし");
        return ResponseEntity.ok(response);
    }

    /**
     * ログアウト処理を行うエンドポイント
     *
     * @param request  HTTPリクエスト
     * @param response HTTPレスポンス
     * @return ログアウトの状態を示すレスポンスエンティティ
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        logger.info("ログアウト処理開始");

        String sessionId = ServletUtil.extractSessionIdFromRequest(request);
        logger.debug("取得されたセッションID: {}", sessionId);

        Map<String, Object> responseBody = new HashMap<>();
        if (sessionId != null) {
            return handleLogout(sessionId, response, responseBody);
        } else {
            // セッションIDがない場合は AuthenticationException をスロー
            logger.warn("有効なセッションIDが存在しません。");
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "有効なセッションIDが存在しません。"
            );
        }
    }

    /**
     * ログアウト処理を行う
     *
     * @param sessionId    セッションID
     * @param response     HTTPレスポンス
     * @param responseBody レスポンスマップ
     * @return レスポンスエンティティ
     */
    private ResponseEntity<?> handleLogout(String sessionId, HttpServletResponse response, Map<String, Object> responseBody) {
        String redisKey = "session:" + sessionId;
        Boolean deleteResult = redisTemplate.delete(redisKey);
        logger.debug("Redisキー: {}, 削除結果: {}", redisKey, deleteResult);

        if (Boolean.TRUE.equals(deleteResult)) {
            logger.info("Redisからセッション情報を削除しました。セッションID: {}", sessionId);
            clearSessionCookie(response);
            responseBody.put("status", "success");
            responseBody.put("message", "ログアウトしました。");
            return ResponseEntity.ok(responseBody);
        } else {
            // Redisにセッション情報がない場合は DatabaseAccessException をスロー
            logger.warn("Redisにセッション情報が見つかりません。セッションID: {}", sessionId);
            throw new DatabaseAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ログアウト処理中にエラーが発生しました。再度お試しください。"
            );
        }
    }

    /**
     * セッションIDのCookieをクリアする
     *
     * @param response HTTPレスポンス
     */
    private void clearSessionCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("sessionId", null);
        cookie.setMaxAge(0);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        response.addCookie(cookie);
    }
}
