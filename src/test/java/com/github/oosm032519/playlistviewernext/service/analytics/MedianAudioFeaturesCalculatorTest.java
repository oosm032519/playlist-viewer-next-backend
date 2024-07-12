package com.github.oosm032519.playlistviewernext.service.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.Mockito.*;

class MedianAudioFeaturesCalculatorTest {

    @InjectMocks
    private MedianAudioFeaturesCalculator medianAudioFeaturesCalculator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private AudioFeatures createMockAudioFeatures(float danceability, float energy, float valence, float tempo,
                                                  float acousticness, float instrumentalness, float liveness, float speechiness) {
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

    @Test
    void testCalculateMedianAudioFeatures() {
        // Arrange
        AudioFeatures audioFeatures1 = createMockAudioFeatures(0.5f, 0.6f, 0.7f, 120.0f, 0.1f, 0.0f, 0.2f, 0.3f);
        AudioFeatures audioFeatures2 = createMockAudioFeatures(0.6f, 0.7f, 0.8f, 130.0f, 0.2f, 0.1f, 0.3f, 0.4f);

        List<Map<String, Object>> trackList = new ArrayList<>();
        trackList.add(Collections.singletonMap("audioFeatures", audioFeatures1));
        trackList.add(Collections.singletonMap("audioFeatures", audioFeatures2));

        // Act
        Map<String, Float> result = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("danceability")).isEqualTo(0.55f);
        assertThat(result.get("energy")).isEqualTo(0.65f);
        assertThat(result.get("valence")).isEqualTo(0.75f);
        assertThat(result.get("tempo")).isEqualTo(125.0f);
        assertThat(result.get("acousticness")).isEqualTo(0.15f);
        assertThat(result.get("instrumentalness")).isEqualTo(0.05f);
        assertThat(result.get("liveness")).isEqualTo(0.25f);
        assertThat(result.get("speechiness")).isCloseTo(0.35f, within(0.0001f));
    }

    @Test
    void testCalculateMedianAudioFeaturesWithEmptyList() {
        // Arrange
        List<Map<String, Object>> trackList = new ArrayList<>();

        // Act
        Map<String, Float> result = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testCalculateMedianAudioFeaturesWithNullAudioFeatures() {
        // Arrange
        List<Map<String, Object>> trackList = new ArrayList<>();
        trackList.add(Collections.singletonMap("audioFeatures", null));

        // Act
        Map<String, Float> result = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    @Test
    void testCalculateMedianAudioFeaturesWithSingleElement() {
        // Arrange
        AudioFeatures audioFeatures = createMockAudioFeatures(0.5f, 0.6f, 0.7f, 120.0f, 0.1f, 0.0f, 0.2f, 0.3f);

        List<Map<String, Object>> trackList = new ArrayList<>();
        trackList.add(Collections.singletonMap("audioFeatures", audioFeatures));

        // Act
        Map<String, Float> result = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("danceability")).isEqualTo(0.5f);
        assertThat(result.get("energy")).isEqualTo(0.6f);
        assertThat(result.get("valence")).isEqualTo(0.7f);
        assertThat(result.get("tempo")).isEqualTo(120.0f);
        assertThat(result.get("acousticness")).isEqualTo(0.1f);
        assertThat(result.get("instrumentalness")).isEqualTo(0.0f);
        assertThat(result.get("liveness")).isEqualTo(0.2f);
        assertThat(result.get("speechiness")).isEqualTo(0.3f);
    }
}
