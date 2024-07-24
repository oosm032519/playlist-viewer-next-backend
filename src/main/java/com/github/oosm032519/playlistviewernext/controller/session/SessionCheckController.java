package com.github.oosm032519.playlistviewernext.controller.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api/session")
public class SessionCheckController {

    private static final Logger logger = LoggerFactory.getLogger(SessionCheckController.class);

    private final OAuth2AuthorizedClientService authorizedClientService;

    public SessionCheckController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        logger.info("SessionCheckControllerが初期化されました。");
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession(
            @AuthenticationPrincipal OAuth2User principal,
            OAuth2AuthenticationToken authentication) {
        logger.info("セッションチェックが開始されました。Principal: {}, Authentication: {}",
                principal != null ? principal.getName() : "null",
                authentication != null ? authentication.getName() : "null");

        return Optional.ofNullable(principal)
                .filter(_ -> authentication != null)
                .map(p -> {
                    logger.debug("認証されたユーザーが見つかりました。ユーザー名: {}", p.getName());
                    return getSessionResponse(p, authentication);
                })
                .orElseGet(() -> {
                    logger.warn("認証されていないユーザーがセッションチェックを試みました。");
                    return ResponseEntity.ok(Map.of("status", "error", "message", "User not authenticated"));
                });
    }

    private ResponseEntity<Map<String, Object>> getSessionResponse(OAuth2User principal, OAuth2AuthenticationToken authentication) {
        logger.debug("セッションレスポンスの生成を開始します。ユーザー名: {}", principal.getName());
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("spotify", authentication.getName());
            logger.info("OAuth2AuthorizedClientのロードが完了しました。クライアント: {}", client != null ? "取得成功" : "取得失敗");
            return Optional.ofNullable(client)
                    .map(c -> {
                        logger.debug("アクセストークンが見つかりました。トークンの有効期限: {}", c.getAccessToken().getExpiresAt());
                        return createSuccessResponse(c, principal);
                    })
                    .orElseGet(() -> {
                        logger.warn("アクセストークンが見つかりませんでした。ユーザー名: {}", authentication.getName());
                        return ResponseEntity.ok(Map.of("status", "error", "message", "No access token found"));
                    });
        } catch (RuntimeException e) {
            logger.error("認可済みクライアントのロード中にエラーが発生しました。", e);
            return ResponseEntity.ok(Map.of("status", "error", "message", "Error loading authorized client: " + e.getMessage()));
        }
    }

    private ResponseEntity<Map<String, Object>> createSuccessResponse(OAuth2AuthorizedClient client, OAuth2User principal) {
        String accessToken = client.getAccessToken().getTokenValue();
        String userId = principal.getAttribute("id");
        logger.info("成功レスポンスを作成します。ユーザーID: {}", userId);
        logger.debug("アクセストークンのプレビュー: {}", accessToken.substring(0, Math.min(accessToken.length(), 10)) + "...");
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Access token is present",
                "userId", Objects.requireNonNull(userId),
                "tokenPreview", accessToken.substring(0, Math.min(accessToken.length(), 10)) + "..."
        ));
    }
}
