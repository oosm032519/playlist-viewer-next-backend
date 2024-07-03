package com.github.oosm032519.playlistviewernext.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

@RestController
public class LogoutController {

    private static final String SPOTIFY_CLIENT_REGISTRATION_ID = "spotify";
    private static final String LOGOUT_SUCCESS_MESSAGE = "ログアウトしました";

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final SecurityContextLogoutHandler logoutHandler;

    public LogoutController(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        this.logoutHandler = new SecurityContextLogoutHandler();
    }

    @PostMapping("/api/logout")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .ifPresent(auth -> performLogout(auth, request, response));
        removeAllCookies(request, response);
        return ResponseEntity.ok(LOGOUT_SUCCESS_MESSAGE);
    }

    private void performLogout(Authentication auth, HttpServletRequest request, HttpServletResponse response) {
        removeOAuth2AuthorizedClient(auth);
        logoutHandler.logout(request, response, auth);
    }

    private void removeOAuth2AuthorizedClient(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            authorizedClientService.removeAuthorizedClient(SPOTIFY_CLIENT_REGISTRATION_ID, oauthToken.getName());
        }
    }

    private void removeAllCookies(HttpServletRequest request, HttpServletResponse response) {
        Optional.ofNullable(request.getCookies())
                .ifPresent(cookies -> Arrays.stream(cookies)
                        .forEach(cookie -> expireCookie(cookie, response)));
    }

    private void expireCookie(Cookie cookie, HttpServletResponse response) {
        cookie.setValue("");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
