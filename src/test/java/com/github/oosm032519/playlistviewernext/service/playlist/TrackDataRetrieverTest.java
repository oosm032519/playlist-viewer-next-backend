package com.github.oosm032519.playlistviewernext.service.playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TrackDataRetrieverTest {

    @Mock
    private SpotifyTrackService spotifyTrackService;

    @Mock
    private PlaylistTrack playlistTrack1;

    @Mock
    private PlaylistTrack playlistTrack2;

    @Mock
    private Track track1;

    @Mock
    private Track track2;

    @Mock
    private AudioFeatures audioFeatures1;

    @Mock
    private AudioFeatures audioFeatures2;

    private TrackDataRetriever trackDataRetriever;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        trackDataRetriever = new TrackDataRetriever(spotifyTrackService);
    }

    @Test
    void getTrackListData_shouldReturnCorrectData() throws Exception {
        // Arrange
        PlaylistTrack[] tracks = {playlistTrack1, playlistTrack2};

        when(playlistTrack1.getTrack()).thenReturn(track1);
        when(playlistTrack2.getTrack()).thenReturn(track2);
        when(track1.getId()).thenReturn("track1Id");
        when(track2.getId()).thenReturn("track2Id");
        when(spotifyTrackService.getAudioFeaturesForTrack("track1Id")).thenReturn(audioFeatures1);
        when(spotifyTrackService.getAudioFeaturesForTrack("track2Id")).thenReturn(audioFeatures2);

        // Act
        List<Map<String, Object>> result = trackDataRetriever.getTrackListData(tracks);

        // Assert
        assertThat(result).hasSize(2);

        Map<String, Object> trackData1 = result.get(0);
        assertThat(trackData1).containsKeys("track", "audioFeatures");
        assertThat(trackData1.get("track")).isEqualTo(track1);
        assertThat(trackData1.get("audioFeatures")).isEqualTo(audioFeatures1);

        Map<String, Object> trackData2 = result.get(1);
        assertThat(trackData2).containsKeys("track", "audioFeatures");
        assertThat(trackData2.get("track")).isEqualTo(track2);
        assertThat(trackData2.get("audioFeatures")).isEqualTo(audioFeatures2);

        verify(spotifyTrackService, times(1)).getAudioFeaturesForTrack("track1Id");
        verify(spotifyTrackService, times(1)).getAudioFeaturesForTrack("track2Id");
    }

    @Test
    void getTrackListData_shouldHandleExceptionGracefully() throws Exception {
        // Arrange
        PlaylistTrack[] tracks = {playlistTrack1};

        when(playlistTrack1.getTrack()).thenReturn(track1);
        when(track1.getId()).thenReturn("track1Id");
        when(spotifyTrackService.getAudioFeaturesForTrack("track1Id")).thenThrow(new RuntimeException("API Error"));

        // Act
        List<Map<String, Object>> result = trackDataRetriever.getTrackListData(tracks);

        // Assert
        assertThat(result).hasSize(1);

        Map<String, Object> trackData = result.get(0);
        assertThat(trackData).containsKeys("track", "audioFeatures");
        assertThat(trackData.get("track")).isEqualTo(track1);
        assertThat(trackData.get("audioFeatures")).isNull();

        verify(spotifyTrackService, times(1)).getAudioFeaturesForTrack("track1Id");
    }
}
