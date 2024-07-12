package com.github.oosm032519.playlistviewernext.service.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MinAudioFeaturesCalculatorTest {

    @InjectMocks
    private MinAudioFeaturesCalculator minAudioFeaturesCalculator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldCalculateMinAudioFeaturesCorrectly() {
        // Arrange
        AudioFeatures audioFeatures1 = createMockAudioFeatures(0.5f, 0.6f, 0.7f, 120.0f, 0.1f, 0.0f, 0.2f, 0.3f);
        AudioFeatures audioFeatures2 = createMockAudioFeatures(0.4f, 0.5f, 0.6f, 110.0f, 0.2f, 0.1f, 0.3f, 0.4f);

        List<Map<String, Object>> trackList = List.of(
                createTrackMap(audioFeatures1),
                createTrackMap(audioFeatures2)
        );

        // Act
        Map<String, Float> result = minAudioFeaturesCalculator.calculateMinAudioFeatures(trackList);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).containsEntry("danceability", 0.4f)
                .containsEntry("energy", 0.5f)
                .containsEntry("valence", 0.6f)
                .containsEntry("tempo", 110.0f)
                .containsEntry("acousticness", 0.1f)
                .containsEntry("instrumentalness", 0.0f)
                .containsEntry("liveness", 0.2f)
                .containsEntry("speechiness", 0.3f);
    }

    @Test
    public void shouldHandleNullAudioFeaturesGracefully() {
        // Arrange
        List<Map<String, Object>> trackList = List.of(
                createTrackMap(null),
                createTrackMap(null)
        );

        // Act
        Map<String, Float> result = minAudioFeaturesCalculator.calculateMinAudioFeatures(trackList);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).containsEntry("danceability", Float.MAX_VALUE)
                .containsEntry("energy", Float.MAX_VALUE)
                .containsEntry("valence", Float.MAX_VALUE)
                .containsEntry("tempo", Float.MAX_VALUE)
                .containsEntry("acousticness", Float.MAX_VALUE)
                .containsEntry("instrumentalness", Float.MAX_VALUE)
                .containsEntry("liveness", Float.MAX_VALUE)
                .containsEntry("speechiness", Float.MAX_VALUE);
    }

    private AudioFeatures createMockAudioFeatures(float danceability, float energy, float valence, float tempo, float acousticness, float instrumentalness, float liveness, float speechiness) {
        AudioFeatures audioFeatures = mock(AudioFeatures.class);
        when(audioFeatures.getDanceability()).thenReturn(danceability);
        when(audioFeatures.getEnergy()).thenReturn(energy);
        when(audioFeatures.getValence()).thenReturn(valence);
        when(audioFeatures.getTempo()).thenReturn(tempo);
        when(audioFeatures.getAcousticness()).thenReturn(acousticness);
        when(audioFeatures.getInstrumentalness()).thenReturn(instrumentalness);
        when(audioFeatures.getLiveness()).thenReturn(liveness);
        when(audioFeatures.getSpeechiness()).thenReturn(speechiness);
        return audioFeatures;
    }

    private Map<String, Object> createTrackMap(AudioFeatures audioFeatures) {
        Map<String, Object> trackMap = new HashMap<>();
        trackMap.put("audioFeatures", audioFeatures);
        return trackMap;
    }
}
