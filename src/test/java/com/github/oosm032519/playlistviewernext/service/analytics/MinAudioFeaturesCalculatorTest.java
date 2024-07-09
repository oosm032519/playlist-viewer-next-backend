package com.github.oosm032519.playlistviewernext.service.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(MinAudioFeaturesCalculatorTest.class);

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCalculateMinAudioFeatures() {
        // Arrange
        AudioFeatures audioFeatures1 = mock(AudioFeatures.class);
        when(audioFeatures1.getDanceability()).thenReturn(0.5f);
        when(audioFeatures1.getEnergy()).thenReturn(0.6f);
        when(audioFeatures1.getValence()).thenReturn(0.7f);
        when(audioFeatures1.getTempo()).thenReturn(120.0f);
        when(audioFeatures1.getAcousticness()).thenReturn(0.1f);
        when(audioFeatures1.getInstrumentalness()).thenReturn(0.0f);
        when(audioFeatures1.getLiveness()).thenReturn(0.2f);
        when(audioFeatures1.getSpeechiness()).thenReturn(0.3f);

        AudioFeatures audioFeatures2 = mock(AudioFeatures.class);
        when(audioFeatures2.getDanceability()).thenReturn(0.4f);
        when(audioFeatures2.getEnergy()).thenReturn(0.5f);
        when(audioFeatures2.getValence()).thenReturn(0.6f);
        when(audioFeatures2.getTempo()).thenReturn(110.0f);
        when(audioFeatures2.getAcousticness()).thenReturn(0.2f);
        when(audioFeatures2.getInstrumentalness()).thenReturn(0.1f);
        when(audioFeatures2.getLiveness()).thenReturn(0.3f);
        when(audioFeatures2.getSpeechiness()).thenReturn(0.4f);

        Map<String, Object> track1 = new HashMap<>();
        track1.put("audioFeatures", audioFeatures1);

        Map<String, Object> track2 = new HashMap<>();
        track2.put("audioFeatures", audioFeatures2);

        List<Map<String, Object>> trackList = List.of(track1, track2);

        // Act
        Map<String, Float> result = minAudioFeaturesCalculator.calculateMinAudioFeatures(trackList);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("danceability")).isEqualTo(0.4f);
        assertThat(result.get("energy")).isEqualTo(0.5f);
        assertThat(result.get("valence")).isEqualTo(0.6f);
        assertThat(result.get("tempo")).isEqualTo(110.0f);
        assertThat(result.get("acousticness")).isEqualTo(0.1f);
        assertThat(result.get("instrumentalness")).isEqualTo(0.0f);
        assertThat(result.get("liveness")).isEqualTo(0.2f);
        assertThat(result.get("speechiness")).isEqualTo(0.3f);
    }

    @Test
    public void testCalculateMinAudioFeaturesWithNullAudioFeatures() {
        // Arrange
        Map<String, Object> track1 = new HashMap<>();
        track1.put("audioFeatures", null);

        Map<String, Object> track2 = new HashMap<>();
        track2.put("audioFeatures", null);

        List<Map<String, Object>> trackList = List.of(track1, track2);

        // Act
        Map<String, Float> result = minAudioFeaturesCalculator.calculateMinAudioFeatures(trackList);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("danceability")).isEqualTo(Float.MAX_VALUE);
        assertThat(result.get("energy")).isEqualTo(Float.MAX_VALUE);
        assertThat(result.get("valence")).isEqualTo(Float.MAX_VALUE);
        assertThat(result.get("tempo")).isEqualTo(Float.MAX_VALUE);
        assertThat(result.get("acousticness")).isEqualTo(Float.MAX_VALUE);
        assertThat(result.get("instrumentalness")).isEqualTo(Float.MAX_VALUE);
        assertThat(result.get("liveness")).isEqualTo(Float.MAX_VALUE);
        assertThat(result.get("speechiness")).isEqualTo(Float.MAX_VALUE);
    }
}
