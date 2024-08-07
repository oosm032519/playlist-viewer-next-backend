// com.github.oosm032519.playlistviewernext.controller.TestCookieController.java
package com.github.oosm032519.playlistviewernext.controller;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}
