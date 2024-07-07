package com.github.oosm032519.playlistviewernext.security;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

@Component
public class AuthService {

    public String getAccessToken(@AuthenticationPrincipal OAuth2User principal) {
        if (principal == null) {
            return null;
        }
        return (String) principal.getAttributes().get("access_token");
    }
}
