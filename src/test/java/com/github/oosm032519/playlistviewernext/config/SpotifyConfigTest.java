package com.github.oosm032519.playlistviewernext.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.michaelthelin.spotify.SpotifyApi;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
        "spotify.client.id=test-client-id",
        "spotify.client.secret=test-client-secret"
})
class SpotifyConfigTest {

    @Autowired
    private SpotifyApi spotifyApi;

    @Autowired
    private SpotifyConfig spotifyConfig;

    @Test
    void spotifyApiBean_ShouldBeConfiguredCorrectly() {
        // SpotifyApiインスタンスが正しく生成されていることを確認
        assertThat(spotifyApi).isNotNull();

        // クライアントIDとシークレットが正しく設定されていることを確認
        assertThat(spotifyApi.getClientId()).isEqualTo("test-client-id");
        assertThat(spotifyApi.getClientSecret()).isEqualTo("test-client-secret");
    }

    @Test
    void spotifyConfig_ShouldHaveCorrectProperties() {
        // プロパティが正しく注入されていることを確認
        assertThat(spotifyConfig).hasFieldOrPropertyWithValue("clientId", "test-client-id");
        assertThat(spotifyConfig).hasFieldOrPropertyWithValue("clientSecret", "test-client-secret");
    }

    @Test
    void spotifyApiBean_ShouldBeSingleton() {
        // SpotifyApiのBeanがシングルトンであることを確認
        SpotifyApi anotherSpotifyApi = spotifyConfig.spotifyApi();
        assertThat(spotifyApi).isSameAs(anotherSpotifyApi);
    }

    @Test
    void spotifyApiBean_WithInvalidConfig_ShouldCreateApiWithNullValues() {
        SpotifyConfig invalidConfig = new SpotifyConfig();
        // クライアントIDとシークレットを設定しない

        SpotifyApi api = invalidConfig.spotifyApi();

        assertThat(api).isNotNull();
        assertThat(api.getClientId()).isNull();
        assertThat(api.getClientSecret()).isNull();
    }


    @Test
    void spotifyApiBean_ShouldBeThreadSafe() {
        List<SpotifyApi> apis = IntStream.range(0, 100)
                .parallel()
                .mapToObj(_ -> spotifyConfig.spotifyApi())
                .collect(Collectors.toList());

        assertThat(apis).allMatch(api -> api == spotifyApi);
    }

}
