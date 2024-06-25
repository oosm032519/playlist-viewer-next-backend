package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionCheckController {

    @Autowired
    private SessionService sessionService;

    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkSession() {
        Map<String, Object> response = new HashMap<>();
        String accessToken = sessionService.getAccessToken();

        if (accessToken != null && !accessToken.isEmpty()) {
            response.put("status", "success");
            response.put("message", "Access token is present in the session");
            // セキュリティ上の理由から、トークンの一部のみを表示
            response.put("tokenPreview", accessToken.substring(0, Math.min(accessToken.length(), 10)) + "...");
        } else {
            response.put("status", "error");
            response.put("message", "No access token found in the session");
        }

        return ResponseEntity.ok(response);
    }
}
