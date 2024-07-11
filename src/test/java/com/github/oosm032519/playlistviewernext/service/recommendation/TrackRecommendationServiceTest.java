// TrackRecommendationServiceTest.java

package com.github.oosm032519.playlistviewernext.service.recommendation;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackRecommendationServiceTest {

    @Mock
    private SpotifyRecommendationService recommendationService;

    @InjectMocks
    private TrackRecommendationService trackRecommendationServiceWrapper;

    @Mock
    private Logger logger;

    private Map<String, Float> maxAudioFeatures;
    private Map<String, Float> minAudioFeatures;
    private Map<String, Float> medianAudioFeatures;
    private Map<String, Object> modeValues;

    @BeforeEach
    void setUp() {
        // LoggerのモックをTrackRecommendationServiceに設定
        TrackRecommendationService.setLogger(logger);

        // テスト用のダミーデータを初期化
        maxAudioFeatures = Map.of("danceability", 0.9f);
        minAudioFeatures = Map.of("danceability", 0.1f);
        medianAudioFeatures = Map.of("danceability", 0.5f);
        modeValues = Map.of("key", 1);
    }

    @Test
    void getRecommendations_ReturnsRecommendationsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given: テスト用のジャンルと推奨トラックのリストを設定
        List<String> top5Genres = List.of("pop", "rock");
        List<Track> recommendations = List.of(
                new Track.Builder().setName("Recommended Track 1").build(),
                new Track.Builder().setName("Recommended Track 2").build()
        );

        // recommendationServiceのgetRecommendationsメソッドが呼ばれたときに、recommendationsを返すように設定
        when(recommendationService.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues)).thenReturn(recommendations);

        // When: trackRecommendationServiceWrapperのgetRecommendationsメソッドを呼び出す
        List<Track> result = trackRecommendationServiceWrapper.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        // Then: 結果が期待通りであることを確認し、recommendationServiceのgetRecommendationsメソッドが呼ばれたことを検証
        assertThat(result).isEqualTo(recommendations);
        verify(recommendationService).getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
    }

    @Test
    void getRecommendations_ReturnsEmptyListWhenGenresAreEmpty() throws IOException, ParseException, SpotifyWebApiException {
        // Given: 空のジャンルリストを設定
        List<String> top5Genres = Collections.emptyList();

        // When: trackRecommendationServiceWrapperのgetRecommendationsメソッドを呼び出す
        List<Track> result = trackRecommendationServiceWrapper.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        // Then: 結果が空のリストであることを確認し、recommendationServiceのgetRecommendationsメソッドが呼ばれないことを検証
        assertThat(result).isEmpty();
        verify(recommendationService, never()).getRecommendations(anyList(), anyMap(), anyMap(), anyMap(), anyMap());
    }

    @Test
    void getRecommendations_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given: テスト用のジャンルリストを設定し、recommendationServiceのgetRecommendationsメソッドがSpotifyWebApiExceptionをスローするように設定
        List<String> top5Genres = List.of("pop", "rock");

        when(recommendationService.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues)).thenThrow(new SpotifyWebApiException("API Error"));

        // When: trackRecommendationServiceWrapperのgetRecommendationsメソッドを呼び出す
        List<Track> result = trackRecommendationServiceWrapper.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        // Then: 結果が空のリストであることを確認し、recommendationServiceのgetRecommendationsメソッドが呼ばれたことと、エラーログが記録されたことを検証
        assertThat(result).isEmpty();
        verify(recommendationService).getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
        verify(logger).error(eq("TrackRecommendationService: Spotify APIの呼び出し中にエラーが発生しました。"), any(SpotifyWebApiException.class));
    }

    @Test
    void getRecommendations_HandlesIOExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given: テスト用のジャンルリストを設定し、recommendationServiceのgetRecommendationsメソッドがIOExceptionをスローするように設定
        List<String> top5Genres = List.of("pop", "rock");

        when(recommendationService.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues)).thenThrow(new IOException("IO Error"));

        // When: trackRecommendationServiceWrapperのgetRecommendationsメソッドを呼び出す
        List<Track> result = trackRecommendationServiceWrapper.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        // Then: 結果が空のリストであることを確認し、recommendationServiceのgetRecommendationsメソッドが呼ばれたことと、エラーログが記録されたことを検証
        assertThat(result).isEmpty();
        verify(recommendationService).getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
        verify(logger).error(eq("TrackRecommendationService: Spotify APIの呼び出し中にエラーが発生しました。"), any(IOException.class));
    }

    @Test
    void getRecommendations_HandlesParseExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given: テスト用のジャンルリストを設定し、recommendationServiceのgetRecommendationsメソッドがParseExceptionをスローするように設定
        List<String> top5Genres = List.of("pop", "rock");

        when(recommendationService.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues)).thenThrow(new ParseException("Parse Error"));

        // When: trackRecommendationServiceWrapperのgetRecommendationsメソッドを呼び出す
        List<Track> result = trackRecommendationServiceWrapper.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        // Then: 結果が空のリストであることを確認し、recommendationServiceのgetRecommendationsメソッドが呼ばれたことと、エラーログが記録されたことを検証
        assertThat(result).isEmpty();
        verify(recommendationService).getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
        verify(logger).error(eq("TrackRecommendationService: Spotify APIの呼び出し中にエラーが発生しました。"), any(ParseException.class));
    }
}
