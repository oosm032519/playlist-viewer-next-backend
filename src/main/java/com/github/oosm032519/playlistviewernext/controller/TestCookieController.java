package com.github.oosm032519.playlistviewernext.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class TestCookieController {

    @GetMapping("/test-cookie")
    public ResponseEntity<Map<String, String>> setTestCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("testCookie", "testValue");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "None");
        response.addCookie(cookie);

        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("testCookie", "testValue");
        return ResponseEntity.ok(responseBody);
    }

    @PostMapping("/test-cookie")
    public ResponseEntity<Map<String, String>> receiveTestCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String testCookieValue = null;
        String sessionId = null;

        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("testCookie".equals(cookie.getName())) {
                    testCookieValue = cookie.getValue();
                } else if ("sessionId".equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                }
                if (testCookieValue != null && sessionId != null) {
                    break;
                }
            }
        }

        Map<String, String> responseBody = new HashMap<>();
        if (testCookieValue != null && sessionId != null) {
            responseBody.put("message", "Received testCookie with value: " + testCookieValue + " and sessionId: " + sessionId);
        } else if (testCookieValue != null) {
            responseBody.put("message", "Received testCookie with value: " + testCookieValue + ", but no sessionId");
        } else if (sessionId != null) {
            responseBody.put("message", "Received sessionId: " + sessionId + ", but no testCookie");
        } else {
            responseBody.put("message", "No testCookie or sessionId received");
        }
        return ResponseEntity.ok(responseBody);
    }
}
