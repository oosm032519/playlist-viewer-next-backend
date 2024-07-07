package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.LogoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RestController
public class LogoutController {

    private static final Logger logger = LoggerFactory.getLogger(LogoutController.class);
    private static final String LOGOUT_SUCCESS_MESSAGE = "ログアウトしました";
    private static final String LOGOUT_ERROR_MESSAGE = "ログアウト処理中にエラーが発生しました";

    private final LogoutService logoutService;

    public LogoutController(OAuth2AuthorizedClientService authorizedClientService) {
        this.logoutService = createLogoutService(authorizedClientService);
        logger.debug("LogoutController initialized");
    }

    protected LogoutService createLogoutService(OAuth2AuthorizedClientService authorizedClientService) {
        return new LogoutService(authorizedClientService, new SecurityContextLogoutHandler());
    }

    @PostMapping("/api/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Logout process started");
        try {
            logoutService.processLogout(request, response);
            return ResponseEntity.ok(LOGOUT_SUCCESS_MESSAGE);
        } catch (Exception e) {
            logger.error("Logout process failed", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(LOGOUT_ERROR_MESSAGE);
        }
    }
}
