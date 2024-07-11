// SessionCheckController.java

package com.github.oosm032519.playlistviewernext.controller.session;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionCheckController {

    private final OAuth2AuthorizedClientService authorizedClientService;

    /**
     * コンストラクタ
     *
     * @param authorizedClientService OAuth2AuthorizedClientServiceのインスタンス
     */
    @Autowired
    public SessionCheckController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    /**
     * セッションの状態をチェックするエンドポイント
     *
     * @param principal      認証されたユーザーの情報
     * @param authentication OAuth2認証トークン
     * @return セッションの状態を含むResponseEntity
     */
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession(
            @AuthenticationPrincipal OAuth2User principal,
            OAuth2AuthenticationToken authentication) {

        // レスポンス用のマップを初期化
        Map<String, Object> response = new HashMap<>();

        // ユーザーが認証されているかどうかをチェック
        if (principal != null && authentication != null) {
            try {
                // 認証されたクライアントをロード
                OAuth2AuthorizedClient authorizedClient = authorizedClientService
                        .loadAuthorizedClient("spotify", authentication.getName());

                // クライアントが存在する場合、アクセストークンとユーザーIDを取得
                if (authorizedClient != null) {
                    String accessToken = authorizedClient.getAccessToken().getTokenValue();
                    String userId = principal.getAttribute("id");

                    // レスポンスに成功メッセージとユーザー情報を追加
                    response.put("status", "success");
                    response.put("message", "Access token is present");
                    response.put("userId", userId);
                    response.put("tokenPreview", accessToken.substring(0, Math.min(accessToken.length(), 10)) + "...");
                } else {
                    // クライアントが存在しない場合、エラーメッセージを追加
                    response.put("status", "error");
                    response.put("message", "No access token found");
                }
            } catch (RuntimeException e) {
                // 例外が発生した場合、エラーメッセージを追加
                response.put("status", "error");
                response.put("message", "Error loading authorized client: " + e.getMessage());
            }
        } else {
            // ユーザーが認証されていない場合、エラーメッセージを追加
            response.put("status", "error");
            response.put("message", "User not authenticated");
        }

        // レスポンスを返却
        return ResponseEntity.ok(response);
    }
}
