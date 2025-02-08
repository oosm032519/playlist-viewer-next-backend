package com.github.oosm032519.playlistviewernext.service.recommendation;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackRecommendationServiceTest {

    @Mock
    private SpotifyRecommendationService recommendationService;

    @InjectMocks
    private TrackRecommendationService trackRecommendationService;

    private Map<String, Float> maxAudioFeatures;
    private Map<String, Float> minAudioFeatures;
    private List<String> artists;

    /**
     * 有効なパラメータが与えられた場合、推奨トラックのリストが正常に返されることを確認する。
     */
    @Test
    void givenValidParameters_whenGetRecommendations_thenReturnsRecommendationsSuccessfully() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        maxAudioFeatures = Map.of("danceability", 0.9f);
        minAudioFeatures = Map.of("danceability", 0.1f);
        artists = List.of("artist1", "artist2");
        List<Track> expectedRecommendations = List.of(
                new Track.Builder().setName("Recommended Track 1").build(),
                new Track.Builder().setName("Recommended Track 2").build()
        );
        when(recommendationService.getRecommendations(artists, maxAudioFeatures, minAudioFeatures))
                .thenReturn(expectedRecommendations);

        // Act: テスト対象メソッドの実行
        List<Track> result = trackRecommendationService.getRecommendations(artists, maxAudioFeatures, minAudioFeatures);

        // Assert: 結果の検証
        assertThat(result).isEqualTo(expectedRecommendations);
        verify(recommendationService).getRecommendations(artists, maxAudioFeatures, minAudioFeatures);
    }

    /**
     * 空のアーティストリストが与えられた場合、空の推奨トラックリストが返されることを確認する。
     */
    @Test
    void givenEmptyGenres_whenGetRecommendations_thenReturnsEmptyList() {
        // Arrange: 空のアーティストリスト
        List<String> emptyArtists = Collections.emptyList();

        // Act: テスト対象メソッドの実行
        List<Track> result = trackRecommendationService.getRecommendations(emptyArtists, maxAudioFeatures, minAudioFeatures);

        // Assert: 結果の検証
        assertThat(result).isEmpty();
        verifyNoInteractions(recommendationService);
    }

    /**
     * Spotify API呼び出し中にSpotifyWebApiExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void givenSpotifyWebApiException_whenGetRecommendations_thenThrowsSpotifyApiException() throws Exception {
        // Arrange: SpotifyWebApiExceptionをスローするモックの設定
        maxAudioFeatures = Map.of("danceability", 0.9f);
        minAudioFeatures = Map.of("danceability", 0.1f);
        artists = List.of("artist1", "artist2");
        when(recommendationService.getRecommendations(artists, maxAudioFeatures, minAudioFeatures))
                .thenThrow(new SpotifyWebApiException("API Error"));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> trackRecommendationService.getRecommendations(artists, maxAudioFeatures, minAudioFeatures))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("トラックの推薦中にエラーが発生しました。");
    }

    /**
     * Spotify API呼び出し中にRuntimeExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void givenRuntimeException_whenGetRecommendations_thenThrowsInternalServerException() throws Exception {
        // Arrange: IOExceptionをスローするモックの設定
        maxAudioFeatures = Map.of("danceability", 0.9f);
        minAudioFeatures = Map.of("danceability", 0.1f);
        artists = List.of("artist1", "artist2");
        when(recommendationService.getRecommendations(artists, maxAudioFeatures, minAudioFeatures))
                .thenThrow(new RuntimeException("Runtime Error"));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> trackRecommendationService.getRecommendations(artists, maxAudioFeatures, minAudioFeatures))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("トラックの推薦中にエラーが発生しました。");
    }
}
