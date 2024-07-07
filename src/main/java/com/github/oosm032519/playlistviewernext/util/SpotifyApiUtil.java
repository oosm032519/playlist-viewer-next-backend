package com.github.oosm032519.playlistviewernext.util;

import se.michaelthelin.spotify.SpotifyApi;
import org.springframework.stereotype.Component;

@Component
public class SpotifyApiUtil {

    private final SpotifyApi spotifyApi;

    public SpotifyApiUtil(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public void setAccessToken(String accessToken) {
        spotifyApi.setAccessToken(accessToken);
    }
}
