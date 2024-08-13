package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.enums.Modality;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyTrackServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @InjectMocks
    private SpotifyTrackService trackService;

    @BeforeEach
    void setUp() {
        // 初期化が必要な場合はここに記述
    }

    @Test
    void testGetAudioFeaturesForTrack_ShouldReturnAudioFeatures() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String trackId = "test-track-id";
        GetAudioFeaturesForTrackRequest.Builder builder = mock(GetAudioFeaturesForTrackRequest.Builder.class);
        GetAudioFeaturesForTrackRequest getAudioFeaturesRequest = mock(GetAudioFeaturesForTrackRequest.class);
        AudioFeatures audioFeatures = mock(AudioFeatures.class);

        when(spotifyApi.getAudioFeaturesForTrack(trackId)).thenReturn(builder);
        when(builder.build()).thenReturn(getAudioFeaturesRequest);
        when(getAudioFeaturesRequest.execute()).thenReturn(audioFeatures);

        // Act
        AudioFeatures result = trackService.getAudioFeaturesForTrack(trackId);

        // Assert
        assertThat(result).isEqualTo(audioFeatures);
    }

    @Test
    void testGetAudioFeaturesForTrack_ShouldThrowExceptionWhenTrackNotFound() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String trackId = "non-existent-track-id";
        GetAudioFeaturesForTrackRequest.Builder builder = mock(GetAudioFeaturesForTrackRequest.Builder.class);
        GetAudioFeaturesForTrackRequest getAudioFeaturesRequest = mock(GetAudioFeaturesForTrackRequest.class);

        when(spotifyApi.getAudioFeaturesForTrack(trackId)).thenReturn(builder);
        when(builder.build()).thenReturn(getAudioFeaturesRequest);
        when(getAudioFeaturesRequest.execute()).thenThrow(new SpotifyWebApiException("Track not found"));

        // Act & Assert
        assertThatThrownBy(() -> trackService.getAudioFeaturesForTrack(trackId))
                .isInstanceOf(PlaylistViewerNextException.class)
                .hasMessageContaining("オーディオ特徴の取得中にエラーが発生しました。");
    }

    @Test
    void testGetAudioFeaturesForTrack_ShouldReturnDetailedAudioFeatures() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String trackId = "test-track-id";
        GetAudioFeaturesForTrackRequest.Builder builder = mock(GetAudioFeaturesForTrackRequest.Builder.class);
        GetAudioFeaturesForTrackRequest getAudioFeaturesRequest = mock(GetAudioFeaturesForTrackRequest.class);
        AudioFeatures audioFeatures = mock(AudioFeatures.class);

        when(spotifyApi.getAudioFeaturesForTrack(trackId)).thenReturn(builder);
        when(builder.build()).thenReturn(getAudioFeaturesRequest);
        when(getAudioFeaturesRequest.execute()).thenReturn(audioFeatures);
        when(audioFeatures.getDanceability()).thenReturn(0.8f);
        when(audioFeatures.getEnergy()).thenReturn(0.9f);
        when(audioFeatures.getKey()).thenReturn(5);
        when(audioFeatures.getLoudness()).thenReturn(-4.3f);
        when(audioFeatures.getMode()).thenReturn(Modality.MINOR);
        when(audioFeatures.getTimeSignature()).thenReturn(4);

        // Act
        AudioFeatures result = trackService.getAudioFeaturesForTrack(trackId);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getDanceability()).isEqualTo(0.8f);
        assertThat(result.getEnergy()).isEqualTo(0.9f);
        assertThat(result.getKey()).isEqualTo(5);
        assertThat(result.getLoudness()).isEqualTo(-4.3f);
        assertThat(result.getMode()).isEqualTo(Modality.MINOR);
        assertThat(result.getTimeSignature()).isEqualTo(4);
    }
}
