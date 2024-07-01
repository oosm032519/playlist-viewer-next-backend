package com.github.oosm032519.playlistviewernext.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller
public class LoginSuccessController {

    private static final Logger logger = LoggerFactory.getLogger(LoginSuccessController.class);

    @GetMapping("/loginSuccess")
    public String loginSuccess(@AuthenticationPrincipal OAuth2User principal) {
        String userId = principal.getAttribute("id");
        String accessToken = principal.getAttribute("access_token");
        logger.info("User successfully authenticated: {}", userId);
        logger.info("Access token: {}", accessToken);

        return "redirect:http://localhost:3000/";
    }
}
