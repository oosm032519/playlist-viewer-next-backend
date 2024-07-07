package com.github.oosm032519.playlistviewernext.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import se.michaelthelin.spotify.SpotifyApi;

import static org.mockito.Mockito.*;

class SpotifyApiUtilTest {

    private SpotifyApi spotifyApi;
    private SpotifyApiUtil spotifyApiUtil;

    @BeforeEach
    void setUp() {
        spotifyApi = mock(SpotifyApi.class);
        spotifyApiUtil = new SpotifyApiUtil(spotifyApi);
    }

    @Test
    @DisplayName("Should set access token")
    void shouldSetAccessToken() {
        String accessToken = "validToken";

        spotifyApiUtil.setAccessToken(accessToken);

        verify(spotifyApi).setAccessToken(accessToken);
    }
}
