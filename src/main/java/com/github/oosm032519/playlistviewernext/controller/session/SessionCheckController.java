package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.model.CustomUserDetails;
import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionCheckController {

    private static final Logger logger = LoggerFactory.getLogger(SessionCheckController.class);

    private final JwtUtil jwtUtil;

    public SessionCheckController(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        logger.info("SessionCheckControllerが初期化されました。JwtUtil: {}", jwtUtil);
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession(HttpServletRequest request) {
        logger.info("セッションチェックが開始されました。リクエスト: {}", request);

        String userId = null;

        // Cookie から JWT を取得
        String jwt = getJwtFromCookie(request);
        logger.debug("Cookie から取得したトークン: {}", jwt != null ? jwt.substring(0, Math.min(jwt.length(), 10)) + "..." : "null");

        if (jwt != null) {
            try {
                logger.info("JWTトークンの検証を開始します。");
                Map<String, Object> claims = jwtUtil.validateToken(jwt);
                userId = (String) claims.get("sub");
                logger.info("JWTトークンの検証が成功しました。ユーザーID: {}", userId);
            } catch (JwtException e) {
                logger.warn("JWTトークンの検証エラー: {}", e.getMessage(), e);
            }
        } else {
            logger.warn("JWTトークンが Cookie に存在しません。");
        }

        // OAuth2 ログイン情報取得 (JWT から取得できなかった場合)
        if (userId == null) {
            logger.info("JWTトークンからユーザー情報を取得できなかったため、OAuth2ログイン情報を確認します。");
            if (SecurityContextHolder.getContext().getAuthentication() != null &&
                    SecurityContextHolder.getContext().getAuthentication().isAuthenticated()) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                logger.debug("認証済みプリンシパル: {}", principal);
                if (principal instanceof CustomUserDetails userDetails) {
                    userId = userDetails.getUsername();
                    logger.info("OAuth2ログイン情報から取得したユーザーID: {}", userId);
                } else {
                    logger.warn("認証済みですが、CustomUserDetailsではありません。プリンシパルの型: {}", principal.getClass().getName());
                }
            } else {
                logger.warn("SecurityContextHolderに認証情報が存在しません。");
            }
        }

        Map<String, Object> response = new HashMap<>();
        if (userId != null) {
            logger.info("認証成功。ユーザーID: {}", userId);
            response.put("status", "success");
            response.put("message", "User is authenticated");
            response.put("userId", userId);
        } else {
            logger.warn("認証されていないユーザーがセッションチェックを試みました。");
            response.put("status", "error");
            response.put("message", "User not authenticated");
        }
        return ResponseEntity.ok(response);
    }

    private String getJwtFromCookie(HttpServletRequest request) {
        logger.debug("Cookie から JWTトークンを取得します。");
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("jwt_token".equals(cookie.getName())) {
                    logger.info("JWTトークンが見つかりました。");
                    return cookie.getValue();
                }
            }
        }
        logger.warn("JWTトークンが見つかりませんでした。");
        return null;
    }
}
