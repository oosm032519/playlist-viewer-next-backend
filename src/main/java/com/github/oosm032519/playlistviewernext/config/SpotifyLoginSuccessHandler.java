package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class SpotifyLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private SpotifyApi spotifyApi;

    public SpotifyLoginSuccessHandler(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String userId = oauth2User.getAttribute("id");
        String spotifyAccessToken = oauth2User.getAttribute("access_token");

        String userName = getSpotifyUserName(userId, spotifyAccessToken);

        Map<String, Object> fullSessionClaims = new HashMap<>();
        fullSessionClaims.put("sub", userId);
        fullSessionClaims.put("name", userName);
        fullSessionClaims.put("spotify_access_token", spotifyAccessToken);
        String fullSessionToken = jwtUtil.generateToken(fullSessionClaims);

        String temporaryToken = UUID.randomUUID().toString();
        String sessionId = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set("temp:" + temporaryToken, sessionId, 5, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set("session:" + sessionId, fullSessionToken);
        redisTemplate.expire("session:" + sessionId, 3600, TimeUnit.SECONDS);

        response.sendRedirect(frontendUrl + "#token=" + temporaryToken);
    }

    private String getSpotifyUserName(String userId, String spotifyAccessToken) {
        try {
            spotifyApi.setAccessToken(spotifyAccessToken);
            User user = spotifyApi.getCurrentUsersProfile().build().execute();
            return user.getDisplayName();
        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            return userId;
        }
    }
}
