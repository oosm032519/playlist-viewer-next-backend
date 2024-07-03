package com.github.oosm032519.playlistviewernext.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import se.michaelthelin.spotify.SpotifyApi;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spotify.client.id=test-client-id",
        "spotify.client.secret=test-client-secret"
})
class SpotifyConfigTest {

    @Autowired
    private SpotifyApi spotifyApi;

    @Test
    void spotifyApiBean_ShouldBeConfiguredCorrectly() {
        assertThat(spotifyApi).isNotNull();
        assertThat(spotifyApi.getClientId()).isEqualTo("test-client-id");
        assertThat(spotifyApi.getClientSecret()).isEqualTo("test-client-secret");
    }
}
