package com.github.oosm032519.playlistviewernext.service.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Arrays;
import java.util.Optional;

/**
 * LogoutService handles the logout process including clearing the security context,
 * removing OAuth2 authorized clients, and expiring cookies.
 */
public class LogoutService {

    private static final Logger logger = LoggerFactory.getLogger(LogoutService.class);
    private static final String SPOTIFY_CLIENT_REGISTRATION_ID = "spotify";

    private final OAuth2AuthorizedClientService authorizedClientService;
    private final SecurityContextLogoutHandler logoutHandler;

    /**
     * Constructs a new LogoutService with the given OAuth2AuthorizedClientService and SecurityContextLogoutHandler.
     *
     * @param authorizedClientService the OAuth2AuthorizedClientService instance
     * @param logoutHandler           the SecurityContextLogoutHandler instance
     */
    public LogoutService(OAuth2AuthorizedClientService authorizedClientService, SecurityContextLogoutHandler logoutHandler) {
        this.authorizedClientService = authorizedClientService;
        this.logoutHandler = logoutHandler;
    }

    /**
     * Processes the logout by clearing the security context, removing OAuth2 clients, and expiring cookies.
     *
     * @param request  the HttpServletRequest instance
     * @param response the HttpServletResponse instance
     */
    public void processLogout(HttpServletRequest request, HttpServletResponse response) {
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .ifPresentOrElse(
                        auth -> performLogout(auth, request, response),
                        () -> logger.debug("No authentication found in SecurityContext")
                );
        removeAllCookies(request, response);
        SecurityContextHolder.clearContext();
        logger.info("Logout process completed successfully");
    }

    private void performLogout(Authentication auth, HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Performing logout for authentication: {}", auth);
        removeOAuth2AuthorizedClient(auth);
        logoutHandler.logout(request, response, auth);
        logger.debug("Logout handler executed");
    }

    private void removeOAuth2AuthorizedClient(Authentication auth) {
        if (auth instanceof OAuth2AuthenticationToken oauthToken) {
            logger.debug("Removing OAuth2 authorized client for user: {}", oauthToken.getName());
            authorizedClientService.removeAuthorizedClient(SPOTIFY_CLIENT_REGISTRATION_ID, oauthToken.getName());
            logger.debug("OAuth2 authorized client removed");
        } else {
            logger.debug("Authentication is not an OAuth2AuthenticationToken");
        }
    }

    private void removeAllCookies(HttpServletRequest request, HttpServletResponse response) {
        logger.debug("Removing all cookies");
        Optional.ofNullable(request.getCookies())
                .ifPresent(cookies -> {
                    logger.debug("Found {} cookies to remove", cookies.length);
                    Arrays.stream(cookies).forEach(cookie -> expireCookie(cookie, response));
                });
        logger.debug("All cookies removed");
    }

    private void expireCookie(Cookie cookie, HttpServletResponse response) {
        logger.debug("Expiring cookie: {}", cookie.getName());
        cookie.setValue("");
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        logger.debug("Cookie expired: {}", cookie.getName());
    }
}
