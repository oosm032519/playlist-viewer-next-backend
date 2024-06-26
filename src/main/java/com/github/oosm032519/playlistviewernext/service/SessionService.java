package com.github.oosm032519.playlistviewernext.service;

import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Service
public class SessionService {

    private static final String ACCESS_TOKEN_KEY = "spotifyAccessToken";
    private static final String USER_ID_KEY = "spotifyUserId";

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

    public void setUserId(String userId) {
        HttpSession session = getSession();
        session.setAttribute(USER_ID_KEY, userId);
    }

    public String getUserId() {
        HttpSession session = getSession();
        return (String) session.getAttribute(USER_ID_KEY);
    }
}
