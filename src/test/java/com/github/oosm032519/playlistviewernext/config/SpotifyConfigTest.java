package com.github.oosm032519.playlistviewernext.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import se.michaelthelin.spotify.SpotifyApi;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@TestPropertySource(properties = {
        "spotify.client.id=test-client-id",
        "spotify.client.secret=test-client-secret"
})
@DisplayName("SpotifyConfig のテスト")
class SpotifyConfigTest {

    @Autowired
    private SpotifyApi spotifyApi;

    @Autowired
    private SpotifyConfig spotifyConfig;

    @Nested
    @DisplayName("spotifyApi Bean のテスト")
    class SpotifyApiBeanTests {

        @Test
        @DisplayName("正しく設定されているか")
        void shouldBeConfiguredCorrectly() {
            assertThat(spotifyApi).isNotNull();
            assertThat(spotifyApi.getClientId()).isEqualTo("test-client-id");
            assertThat(spotifyApi.getClientSecret()).isEqualTo("test-client-secret");
        }

        @Test
        @DisplayName("シングルトンであるか")
        void shouldBeSingleton() {
            SpotifyApi anotherSpotifyApi = spotifyConfig.spotifyApi();
            assertThat(spotifyApi).isSameAs(anotherSpotifyApi);
        }
    }

    @Nested
    @DisplayName("SpotifyConfig のプロパティテスト")
    class SpotifyConfigPropertyTests {

        @Test
        @DisplayName("正しいプロパティが注入されているか")
        void shouldHaveCorrectProperties() {
            assertThat(spotifyConfig)
                    .hasFieldOrPropertyWithValue("clientId", "test-client-id")
                    .hasFieldOrPropertyWithValue("clientSecret", "test-client-secret");
        }
    }

    @Nested
    @DisplayName("エッジケースのテスト")
    class EdgeCaseTests {

        @Test
        @DisplayName("無効な設定でSpotifyApiBeanが作成されるか")
        void shouldCreateApiWithNullValuesForInvalidConfig() {
            SpotifyConfig invalidConfig = new SpotifyConfig();
            SpotifyApi api = invalidConfig.spotifyApi();

            assertThat(api).isNotNull();
            assertThat(api.getClientId()).isNull();
            assertThat(api.getClientSecret()).isNull();
        }
    }
}
