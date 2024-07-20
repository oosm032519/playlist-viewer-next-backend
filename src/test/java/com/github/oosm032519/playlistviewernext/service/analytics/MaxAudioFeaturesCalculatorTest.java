package com.github.oosm032519.playlistviewernext.service.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MaxAudioFeaturesCalculatorTest {

    @InjectMocks
    private MaxAudioFeaturesCalculator maxAudioFeaturesCalculator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCalculateMaxAudioFeatures_withEmptyTrackList() {
        // Arrange
        List<Map<String, Object>> trackList = new ArrayList<>();

        // Act
        Map<String, Float> result = maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void testCalculateMaxAudioFeatures_withValidTrackList() {
        // Arrange
        List<Map<String, Object>> trackList = new ArrayList<>();

        AudioFeatures audioFeatures1 = createMockAudioFeatures(0.5f, 0.6f, 0.7f, 120.0f, 0.1f, 0.0f, 0.2f, 0.3f);
        AudioFeatures audioFeatures2 = createMockAudioFeatures(0.8f, 0.9f, 0.4f, 130.0f, 0.2f, 0.1f, 0.3f, 0.4f);
        AudioFeatures audioFeatures3 = createMockAudioFeatures(0.7f, 0.8f, 0.6f, 125.0f, 0.15f, 0.05f, 0.25f, 0.35f);

        trackList.add(createTrackMap(audioFeatures1));
        trackList.add(createTrackMap(audioFeatures2));
        trackList.add(createTrackMap(audioFeatures3));

        // Act
        Map<String, Float> result = maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);

        // Assert
        assertThat(result).isNotNull();
        // 上限値の計算: Q3 + 1.5 * IQR（ただし、tempo以外は1以下に制限）
        assertThat(result.get("danceability")).isGreaterThan(0.8f).isLessThanOrEqualTo(1f);
        assertThat(result.get("energy")).isGreaterThan(0.9f).isLessThanOrEqualTo(1f);
        assertThat(result.get("valence")).isGreaterThan(0.7f).isLessThanOrEqualTo(1f);
        assertThat(result.get("tempo")).isGreaterThan(130.0f);
        assertThat(result.get("acousticness")).isGreaterThan(0.2f).isLessThanOrEqualTo(1f);
        assertThat(result.get("instrumentalness")).isGreaterThan(0.1f).isLessThanOrEqualTo(1f);
        assertThat(result.get("liveness")).isGreaterThan(0.3f).isLessThanOrEqualTo(1f);
        assertThat(result.get("speechiness")).isGreaterThan(0.4f).isLessThanOrEqualTo(1f);
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

    @Test
    public void testCalculateMaxAudioFeatures_withNullAudioFeatures() {
        // Arrange
        List<Map<String, Object>> trackList = new ArrayList<>();
        trackList.add(createTrackMap(null));

        // Act
        Map<String, Float> result = maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);

        // Assert
        assertThat(result).isEmpty();
    }
}
