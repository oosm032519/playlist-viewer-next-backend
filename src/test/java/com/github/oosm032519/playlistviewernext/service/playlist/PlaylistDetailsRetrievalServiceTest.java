package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.service.analytics.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

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
        // Given
        String playlistId = "testPlaylistId";
        PlaylistTrack[] tracks = new PlaylistTrack[]{
                new PlaylistTrack.Builder().setTrack(new Track.Builder().setId("track1").build()).build(),
                new PlaylistTrack.Builder().setTrack(new Track.Builder().setId("track2").build()).build()
        };
        String playlistName = "Test Playlist";
        User owner = new User.Builder().setId("ownerId").setDisplayName("Owner Name").build();
        List<Map<String, Object>> trackList = new ArrayList<>();
        trackList.add(Map.of("id", "track1"));
        trackList.add(Map.of("id", "track2"));

        Map<String, Float> maxAudioFeatures = Map.of("feature1", 1.0f);
        Map<String, Float> minAudioFeatures = Map.of("feature1", 0.1f);
        Map<String, Float> medianAudioFeatures = Map.of("feature1", 0.5f);
        Map<String, Float> averageAudioFeatures = Map.of("feature1", 0.3f);
        Map<String, Object> modeValues = Map.of("feature1", 0.3f);

        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(tracks);
        when(playlistDetailsService.getPlaylistName(playlistId)).thenReturn(playlistName);
        when(playlistDetailsService.getPlaylistOwner(playlistId)).thenReturn(owner);
        when(trackDataRetriever.getTrackListData(tracks)).thenReturn(trackList);
        when(maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList)).thenReturn(maxAudioFeatures);
        when(minAudioFeaturesCalculator.calculateMinAudioFeatures(trackList)).thenReturn(minAudioFeatures);
        when(medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList)).thenReturn(medianAudioFeatures);
        when(averageAudioFeaturesCalculator.calculateAverageAudioFeatures(trackList)).thenReturn(averageAudioFeatures);
        when(modeValuesCalculator.calculateModeValues(trackList)).thenReturn(modeValues);

        // When
        Map<String, Object> response = playlistDetailsRetrievalService.getPlaylistDetails(playlistId);

        // Then
        assertThat(response.get("tracks")).isInstanceOf(Map.class);
        assertThat(response.get("playlistName")).isEqualTo(playlistName);
        assertThat(response.get("ownerId")).isEqualTo(owner.getId());
        assertThat(response.get("ownerName")).isEqualTo(owner.getDisplayName());
        assertThat(response.get("maxAudioFeatures")).isEqualTo(maxAudioFeatures);
        assertThat(response.get("minAudioFeatures")).isEqualTo(minAudioFeatures);
        assertThat(response.get("medianAudioFeatures")).isEqualTo(medianAudioFeatures);
        assertThat(response.get("averageAudioFeatures")).isEqualTo(averageAudioFeatures);
        assertThat(response.get("modeValues")).isEqualTo(modeValues);

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
