// File: src/main/java/com/github/oosm032519/playlistviewernext/controller/LoginSuccessController.java

package com.github.oosm032519.playlistviewernext.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Controller
public class LoginSuccessController {

    @GetMapping("/loginSuccess")
    public String loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        String userId = principal.getAttribute("id");
        String accessToken = principal.getAttribute("access_token");
        System.out.println("User successfully authenticated: " + userId);
        System.out.println("Access token: " + accessToken);

        // URLエンコードしたユーザーIDをクエリパラメータとして追加
        String encodedUserId = URLEncoder.encode(Objects.requireNonNull(userId), StandardCharsets.UTF_8);

        // フロントエンドのURLにリダイレクト（クエリパラメータ付き）
        return "redirect:http://localhost:3000?loginSuccess=true&userId=" + encodedUserId;
    }
}
