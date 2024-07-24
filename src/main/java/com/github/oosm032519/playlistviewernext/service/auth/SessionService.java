package com.github.oosm032519.playlistviewernext.service.auth;

import com.github.oosm032519.playlistviewernext.model.SpotifySession;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SessionService {

    private final StringRedisTemplate redisTemplate;

    public SessionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveSpotifySession(String sessionId, SpotifySession spotifySession) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String sessionKey = "spring:session:sessions:" + sessionId;
        hashOps.put(sessionKey, "accessToken", spotifySession.getAccessToken());
        hashOps.put(sessionKey, "userId", spotifySession.getUserId());
    }

    public SpotifySession getSpotifySession(String sessionId) {
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();
        String sessionKey = "spring:session:sessions:" + sessionId;
        Map<String, String> sessionData = hashOps.entries(sessionKey);
        if (sessionData.isEmpty()) {
            return null;
        }
        return new SpotifySession(sessionData.get("accessToken"), sessionData.get("userId"));
    }
}
