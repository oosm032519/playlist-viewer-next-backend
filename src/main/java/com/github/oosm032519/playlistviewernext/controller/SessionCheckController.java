package com.github.oosm032519.playlistviewernext.controller;

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

    @Autowired
    public SessionCheckController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession(
            @AuthenticationPrincipal OAuth2User principal,
            OAuth2AuthenticationToken authentication) {

        Map<String, Object> response = new HashMap<>();

        if (principal == null || authentication == null) {
            response.put("status", "error");
            response.put("message", "User not authenticated");
            return ResponseEntity.ok(response);
        }

        OAuth2AuthorizedClient authorizedClient;
        try {
            authorizedClient = authorizedClientService
                    .loadAuthorizedClient("spotify", authentication.getName());
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Error loading authorized client: " + e.getMessage());
            return ResponseEntity.ok(response);
        }

        if (authorizedClient == null) {
            response.put("status", "error");
            response.put("message", "No access token found");
            return ResponseEntity.ok(response);
        }

        String accessToken = authorizedClient.getAccessToken().getTokenValue();
        String userId = principal.getAttribute("id");

        response.put("status", "success");
        response.put("message", "Access token is present");
        response.put("userId", userId);
        response.put("tokenPreview", accessToken.substring(0, Math.min(accessToken.length(), 10)) + "...");

        return ResponseEntity.ok(response);
    }
}
