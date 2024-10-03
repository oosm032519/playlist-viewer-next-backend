package com.github.oosm032519.playlistviewernext.service.playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackDataRetrieverTest {

    @Mock
    private SpotifyTrackService trackService;

    @InjectMocks
    private TrackDataRetriever trackDataRetriever;

    private PlaylistTrack[] playlistTracks;

    @BeforeEach
    public void setUp() {
        Track mockTrack = mock(Track.class);
        when(mockTrack.getId()).thenReturn("testTrackId");

        PlaylistTrack mockPlaylistTrack = mock(PlaylistTrack.class);
        when(mockPlaylistTrack.getTrack()).thenReturn(mockTrack);

        playlistTracks = new PlaylistTrack[]{mockPlaylistTrack};
    }

    @Test
    public void shouldReturnTrackListData() throws Exception {
        AudioFeatures mockAudioFeatures = mock(AudioFeatures.class);
        when(trackService.getAudioFeaturesForTrack(anyString())).thenReturn(mockAudioFeatures);

        List<Map<String, Object>> result = trackDataRetriever.getTrackListData(playlistTracks);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsKeys("track", "audioFeatures");
        assertThat(result.get(0).get("track")).isNotNull();
        assertThat(result.get(0).get("audioFeatures")).isEqualTo(mockAudioFeatures);

        verify(trackService, times(1)).getAudioFeaturesForTrack("testTrackId");
    }

    @Test
    public void shouldThrowSpotifyApiExceptionWhenFetchingAudioFeatures() {
        when(trackService.getAudioFeaturesForTrack(anyString())).thenThrow(new RuntimeException("Test Exception"));

        assertThatThrownBy(() -> trackDataRetriever.getTrackListData(playlistTracks))
                .isInstanceOf(SpotifyApiException.class)
                .hasMessageContaining("トラックデータの取得中にエラーが発生しました。");

        verify(trackService, times(1)).getAudioFeaturesForTrack("testTrackId");
    }

    @Test
    public void shouldLogAppropriateMessages() throws Exception {
        Logger mockLogger = mock(Logger.class);
        TrackDataRetriever.logger = mockLogger;

        AudioFeatures mockAudioFeatures = mock(AudioFeatures.class);
        when(trackService.getAudioFeaturesForTrack(anyString())).thenReturn(mockAudioFeatures);

        trackDataRetriever.getTrackListData(playlistTracks);

        verify(mockLogger, times(1)).info("getTrackListData: トラック数: {}", 1);
        verify(mockLogger, times(1)).info("getTrackListData: トラックデータリスト作成完了");
    }
}
