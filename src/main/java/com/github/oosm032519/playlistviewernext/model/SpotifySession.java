package com.github.oosm032519.playlistviewernext.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SpotifySession {
    private String accessToken;
    private String userId;

    public SpotifySession(String accessToken, String userId) {
        this.accessToken = accessToken;
        this.userId = userId;
    }
}
