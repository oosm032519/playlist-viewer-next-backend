// PlaylistDetailsRetrievalServiceTest.java

package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.service.analytics.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PlaylistDetailsRetrievalServiceのテストクラス
 */
@ExtendWith(MockitoExtension.class)
class PlaylistDetailsRetrievalServiceTest {

    @Mock
    private SpotifyPlaylistDetailsService playlistDetailsService;

    @Mock
    private SpotifyTrackService trackService;

    @Mock
    private SpotifyClientCredentialsAuthentication authController;

    @Mock
    private MaxAudioFeaturesCalculator maxAudioFeaturesCalculator;

    @Mock
    private MinAudioFeaturesCalculator minAudioFeaturesCalculator;

    @Mock
    private MedianAudioFeaturesCalculator medianAudioFeaturesCalculator;

    @Mock
    private AverageAudioFeaturesCalculator averageAudioFeaturesCalculator;

    @Mock
    private TrackDataRetriever trackDataRetriever;

    @Mock
    private ModeValuesCalculator modeValuesCalculator;

    @InjectMocks
    private PlaylistDetailsRetrievalService playlistDetailsRetrievalService;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void getPlaylistDetails_ReturnsDetailsSuccessfully() throws Exception {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";
        PlaylistTrack[] tracks = new PlaylistTrack[]{
                new PlaylistTrack.Builder().setTrack(new Track.Builder().setId("track1").setDurationMs(180000).build()).build(),
                new PlaylistTrack.Builder().setTrack(new Track.Builder().setId("track2").setDurationMs(240000).build()).build()
        };
        String playlistName = "Test Playlist";
        User owner = new User.Builder().setId("ownerId").setDisplayName("Owner Name").build();
        List<Map<String, Object>> trackList = List.of(
                Map.of("id", "track1"),
                Map.of("id", "track2")
        );

        Map<String, Float> maxAudioFeatures = Map.of("feature1", 1.0f);
        Map<String, Float> minAudioFeatures = Map.of("feature1", 0.1f);
        Map<String, Float> medianAudioFeatures = Map.of("feature1", 0.5f);
        Map<String, Float> averageAudioFeatures = Map.of("feature1", 0.3f);
        Map<String, Object> modeValues = Map.of("feature1", 0.3f);
        long expectedTotalDuration = 420000; // 180000 + 240000

        // モックの設定
        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(tracks);
        when(playlistDetailsService.getPlaylistName(playlistId)).thenReturn(playlistName);
        when(playlistDetailsService.getPlaylistOwner(playlistId)).thenReturn(owner);
        when(trackDataRetriever.getTrackListData(tracks)).thenReturn(trackList);
        when(maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList)).thenReturn(maxAudioFeatures);
        when(minAudioFeaturesCalculator.calculateMinAudioFeatures(trackList)).thenReturn(minAudioFeatures);
        when(medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList)).thenReturn(medianAudioFeatures);
        when(averageAudioFeaturesCalculator.calculateAverageAudioFeatures(trackList)).thenReturn(averageAudioFeatures);
        when(modeValuesCalculator.calculateModeValues(trackList)).thenReturn(modeValues);

        // Act: テスト対象メソッドの実行
        Map<String, Object> response = playlistDetailsRetrievalService.getPlaylistDetails(playlistId);

        // Assert: 結果の検証
        assertThat(response)
                .containsEntry("tracks", Map.of("items", trackList))
                .containsEntry("playlistName", playlistName)
                .containsEntry("ownerId", owner.getId())
                .containsEntry("ownerName", owner.getDisplayName())
                .containsEntry("maxAudioFeatures", maxAudioFeatures)
                .containsEntry("minAudioFeatures", minAudioFeatures)
                .containsEntry("medianAudioFeatures", medianAudioFeatures)
                .containsEntry("averageAudioFeatures", averageAudioFeatures)
                .containsEntry("modeValues", modeValues)
                .containsEntry("totalDuration", expectedTotalDuration);

        // モックの呼び出し検証
        verify(playlistDetailsService).getPlaylistTracks(playlistId);
        verify(playlistDetailsService).getPlaylistName(playlistId);
        verify(playlistDetailsService).getPlaylistOwner(playlistId);
        verify(trackDataRetriever).getTrackListData(tracks);
        verify(maxAudioFeaturesCalculator).calculateMaxAudioFeatures(trackList);
        verify(minAudioFeaturesCalculator).calculateMinAudioFeatures(trackList);
        verify(medianAudioFeaturesCalculator).calculateMedianAudioFeatures(trackList);
        verify(averageAudioFeaturesCalculator).calculateAverageAudioFeatures(trackList);
        verify(modeValuesCalculator).calculateModeValues(trackList);
    }
}
