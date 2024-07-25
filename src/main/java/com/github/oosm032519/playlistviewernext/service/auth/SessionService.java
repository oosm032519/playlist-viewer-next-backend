package com.github.oosm032519.playlistviewernext.service.auth;

import com.github.oosm032519.playlistviewernext.model.SpotifySession;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final JdbcTemplate jdbcTemplate;

    public SessionService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public void saveSpotifySession(String sessionId, SpotifySession spotifySession) {
        String sql = "UPDATE SPRING_SESSION_ATTRIBUTES SET ATTRIBUTE_BYTES = ? WHERE SESSION_PRIMARY_ID = ? AND ATTRIBUTE_NAME = ?";
        jdbcTemplate.update(sql, spotifySession.getAccessToken().getBytes(), sessionId, "accessToken");
        jdbcTemplate.update(sql, spotifySession.getUserId().getBytes(), sessionId, "userId");
    }

    public SpotifySession getSpotifySession(String sessionId) {
        String sql = "SELECT ATTRIBUTE_NAME, ATTRIBUTE_BYTES FROM SPRING_SESSION_ATTRIBUTES WHERE SESSION_PRIMARY_ID = ?";
        return jdbcTemplate.query(sql, new Object[]{sessionId}, rs -> {
            String accessToken = null;
            String userId = null;
            while (rs.next()) {
                String attributeName = rs.getString("ATTRIBUTE_NAME");
                byte[] attributeBytes = rs.getBytes("ATTRIBUTE_BYTES");
                if ("accessToken".equals(attributeName)) {
                    accessToken = new String(attributeBytes);
                } else if ("userId".equals(attributeName)) {
                    userId = new String(attributeBytes);
                }
            }
            return (accessToken != null && userId != null) ? new SpotifySession(accessToken, userId) : null;
        });
    }
}
