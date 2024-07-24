package com.github.oosm032519.playlistviewernext.controller.session;

import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionCheckController {

    private static final Logger logger = LoggerFactory.getLogger(SessionCheckController.class);

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession(HttpSession session) {
        logger.info("セッションチェックが開始されました。");

        String accessToken = (String) session.getAttribute("accessToken");
        String userId = (String) session.getAttribute("userId");

        if (accessToken != null && userId != null) {
            logger.debug("認証されたユーザーが見つかりました。ユーザーID: {}", userId);
            return ResponseEntity.ok(Map.of(
                    "status", "success",
                    "message", "Access token is present",
                    "userId", userId,
                    "tokenPreview", accessToken.substring(0, Math.min(accessToken.length(), 10)) + "..."
            ));
        } else {
            logger.warn("認証されていないユーザーがセッションチェックを試みました。");
            return ResponseEntity.ok(Map.of("status", "error", "message", "User not authenticated"));
        }
    }
}
