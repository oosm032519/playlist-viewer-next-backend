package com.github.oosm032519.playlistviewernext.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Service
public class SessionService {

    private static final String ACCESS_TOKEN_KEY = "spotifyAccessToken";

    public void setAccessToken(String accessToken) {
        HttpSession session = getSession();
        session.setAttribute(ACCESS_TOKEN_KEY, accessToken);
    }

    public String getAccessToken() {
        HttpSession session = getSession();
        return (String) session.getAttribute(ACCESS_TOKEN_KEY);
    }

    private HttpSession getSession() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
        return attr.getRequest().getSession(true);
    }
}
