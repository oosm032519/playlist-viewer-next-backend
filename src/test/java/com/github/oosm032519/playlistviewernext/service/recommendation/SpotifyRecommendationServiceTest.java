package com.github.oosm032519.playlistviewernext.service.recommendation;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.service.analytics.AudioFeatureSetter;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyRecommendationServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private AudioFeatureSetter audioFeatureSetter;

    @InjectMocks
    private SpotifyRecommendationService spotifyRecommendationService;

    @Test
    void getRecommendations_正常系_推奨トラックが取得できる場合() throws SpotifyWebApiException, IOException, ParseException {
        // テストデータの準備
        List<String> seedArtists = List.of("artistId1", "artistId2");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        Map<String, Float> minAudioFeatures = new HashMap<>();
        Track[] tracks = new Track[2];
        Recommendations recommendations = new Recommendations.Builder().setTracks(tracks).build();

        // モックの設定
        GetRecommendationsRequest.Builder builderMock = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest requestMock = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(builderMock);
        when(builderMock.seed_artists(anyString())).thenReturn(builderMock);
        when(builderMock.limit(anyInt())).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(requestMock);
        when(requestMock.execute()).thenReturn(recommendations);
        doNothing().when(audioFeatureSetter).setMaxAudioFeatures(any(), any());
        doNothing().when(audioFeatureSetter).setMinAudioFeatures(any(), any());


        // テスト実行
        List<Track> result = spotifyRecommendationService.getRecommendations(seedArtists, maxAudioFeatures, minAudioFeatures);

        // 結果検証
        assertThat(result).hasSize(2);
    }

    @Test
    void getRecommendations_正常系_推奨トラックがない場合() throws SpotifyWebApiException, IOException, ParseException {
        // テストデータの準備
        List<String> seedArtists = List.of("artistId1", "artistId2");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        Map<String, Float> minAudioFeatures = new HashMap<>();

        // モックの設定
        GetRecommendationsRequest.Builder builderMock = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest requestMock = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(builderMock);
        when(builderMock.seed_artists(anyString())).thenReturn(builderMock);
        when(builderMock.limit(anyInt())).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(requestMock);
        when(requestMock.execute()).thenReturn(null);
        doNothing().when(audioFeatureSetter).setMaxAudioFeatures(any(), any());
        doNothing().when(audioFeatureSetter).setMinAudioFeatures(any(), any());

        // テスト実行
        List<Track> result = spotifyRecommendationService.getRecommendations(seedArtists, maxAudioFeatures, minAudioFeatures);

        // 結果検証
        assertThat(result).isEmpty();
    }

    @Test
    void getRecommendations_異常系_seedArtistsがnullの場合() throws SpotifyWebApiException {
        // テスト実行
        List<Track> result = spotifyRecommendationService.getRecommendations(null, new HashMap<>(), new HashMap<>());

        // 結果検証
        assertThat(result).isEmpty();
    }

    @Test
    void getRecommendations_異常系_seedArtistsが空の場合() throws SpotifyWebApiException {
        // テスト実行
        List<Track> result = spotifyRecommendationService.getRecommendations(Collections.emptyList(), new HashMap<>(), new HashMap<>());

        // 結果検証
        assertThat(result).isEmpty();
    }

    @Test
    void getRecommendations_異常系_SpotifyWebApiExceptionが発生した場合() throws IOException, ParseException, SpotifyWebApiException {
        // テストデータの準備
        List<String> seedArtists = List.of("artistId1", "artistId2");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        Map<String, Float> minAudioFeatures = new HashMap<>();

        // モックの設定
        GetRecommendationsRequest.Builder builderMock = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest requestMock = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(builderMock);
        when(builderMock.seed_artists(anyString())).thenReturn(builderMock);
        when(builderMock.limit(anyInt())).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(requestMock);
        when(requestMock.execute()).thenThrow(new SpotifyWebApiException("Spotify API Error"));
        doNothing().when(audioFeatureSetter).setMaxAudioFeatures(any(), any());
        doNothing().when(audioFeatureSetter).setMinAudioFeatures(any(), any());


        // テスト実行と検証
        assertThatThrownBy(() -> spotifyRecommendationService.getRecommendations(seedArtists, maxAudioFeatures, minAudioFeatures))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessageContaining("Spotify API Error");
    }

    @Test
    void getRecommendations_異常系_その他の例外が発生した場合() throws SpotifyWebApiException, ParseException, IOException {
        // テストデータの準備
        List<String> seedArtists = List.of("artistId1", "artistId2");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        Map<String, Float> minAudioFeatures = new HashMap<>();

        // モックの設定
        GetRecommendationsRequest.Builder builderMock = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest requestMock = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(builderMock);
        when(builderMock.seed_artists(anyString())).thenReturn(builderMock);
        when(builderMock.limit(anyInt())).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(requestMock);
        when(requestMock.execute()).thenThrow(new IOException("IO Error"));
        doNothing().when(audioFeatureSetter).setMaxAudioFeatures(any(), any());
        doNothing().when(audioFeatureSetter).setMinAudioFeatures(any(), any());

        // テスト実行と検証
        assertThatThrownBy(() -> spotifyRecommendationService.getRecommendations(seedArtists, maxAudioFeatures, minAudioFeatures))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("推奨トラックの取得中にエラーが発生しました。")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR); // ここを修正
    }
}
