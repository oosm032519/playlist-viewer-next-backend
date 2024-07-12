package com.github.oosm032519.playlistviewernext.controller.session;

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

    private final OAuth2AuthorizedClientService authorizedClientService;

    public SessionCheckController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession(
            @AuthenticationPrincipal OAuth2User principal,
            OAuth2AuthenticationToken authentication) {

        return Optional.ofNullable(principal)
                .filter(_ -> authentication != null)
                .map(p -> getSessionResponse(p, authentication))
                .orElse(ResponseEntity.ok(Map.of("status", "error", "message", "User not authenticated")));
    }

    private ResponseEntity<Map<String, Object>> getSessionResponse(OAuth2User principal, OAuth2AuthenticationToken authentication) {
        try {
            OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient("spotify", authentication.getName());
            return Optional.ofNullable(client)
                    .map(c -> createSuccessResponse(c, principal))
                    .orElse(ResponseEntity.ok(Map.of("status", "error", "message", "No access token found")));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of("status", "error", "message", "Error loading authorized client: " + e.getMessage()));
        }
    }

    private ResponseEntity<Map<String, Object>> createSuccessResponse(OAuth2AuthorizedClient client, OAuth2User principal) {
        String accessToken = client.getAccessToken().getTokenValue();
        String userId = principal.getAttribute("id");
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Access token is present",
                "userId", Objects.requireNonNull(userId),
                "tokenPreview", accessToken.substring(0, Math.min(accessToken.length(), 10)) + "..."
        ));
    }
}
