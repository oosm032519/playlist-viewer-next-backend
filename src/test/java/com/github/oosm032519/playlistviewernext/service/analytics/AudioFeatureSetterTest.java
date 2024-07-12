package com.github.oosm032519.playlistviewernext.service.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AudioFeatureSetterTest {

    @Mock
    private GetRecommendationsRequest.Builder builder;

    @InjectMocks
    private AudioFeatureSetter audioFeatureSetter;

    private Map<String, Float> audioFeatures;

    @BeforeEach
    void setUp() {
        audioFeatures = new HashMap<>();
    }

    @Test
    void testSetMaxAudioFeatures() {
        // Arrange
        audioFeatures.put("danceability", 0.8f);
        audioFeatures.put("energy", 0.7f);
        audioFeatures.put("valence", 0.6f);
        audioFeatures.put("tempo", 120.0f);
        audioFeatures.put("acousticness", 0.5f);
        audioFeatures.put("instrumentalness", 0.4f);
        audioFeatures.put("liveness", 0.3f);
        audioFeatures.put("speechiness", 0.2f);

        // Act
        audioFeatureSetter.setMaxAudioFeatures(builder, audioFeatures);

        // Assert
        verify(builder).max_danceability(0.8f);
        verify(builder).max_energy(0.7f);
        verify(builder).max_valence(0.6f);
        verify(builder).max_tempo(120.0f);
        verify(builder).max_acousticness(0.5f);
        verify(builder).max_instrumentalness(0.4f);
        verify(builder).max_liveness(0.3f);
        verify(builder).max_speechiness(0.2f);
        verifyNoMoreInteractions(builder);
    }

    @Test
    void testSetMaxAudioFeatures_EmptyMap() {
        // Act
        audioFeatureSetter.setMaxAudioFeatures(builder, audioFeatures);

        // Assert
        verifyNoInteractions(builder);
    }

    @Test
    void testSetMinAudioFeatures() {
        // Arrange
        audioFeatures.put("danceability", 0.2f);
        audioFeatures.put("energy", 0.3f);
        audioFeatures.put("valence", 0.4f);
        audioFeatures.put("tempo", 80.0f);
        audioFeatures.put("acousticness", 0.1f);
        audioFeatures.put("instrumentalness", 0.2f);
        audioFeatures.put("liveness", 0.3f);
        audioFeatures.put("speechiness", 0.4f);

        // Act
        audioFeatureSetter.setMinAudioFeatures(builder, audioFeatures);

        // Assert
        verify(builder).min_danceability(0.2f);
        verify(builder).min_energy(0.3f);
        verify(builder).min_valence(0.4f);
        verify(builder).min_tempo(80.0f);
        verify(builder).min_acousticness(0.1f);
        verify(builder).min_instrumentalness(0.2f);
        verify(builder).min_liveness(0.3f);
        verify(builder).min_speechiness(0.4f);
        verifyNoMoreInteractions(builder);
    }

    @Test
    void testSetMinAudioFeatures_EmptyMap() {
        // Act
        audioFeatureSetter.setMinAudioFeatures(builder, audioFeatures);

        // Assert
        verifyNoInteractions(builder);
    }

    @Test
    void testSetMedianAudioFeatures() {
        // Arrange
        audioFeatures.put("danceability", 0.5f);
        audioFeatures.put("energy", 0.6f);
        audioFeatures.put("valence", 0.7f);
        audioFeatures.put("tempo", 100.0f);
        audioFeatures.put("acousticness", 0.3f);
        audioFeatures.put("instrumentalness", 0.4f);
        audioFeatures.put("liveness", 0.5f);
        audioFeatures.put("speechiness", 0.6f);

        // Act
        audioFeatureSetter.setMedianAudioFeatures(builder, audioFeatures);

        // Assert
        verify(builder).target_danceability(0.5f);
        verify(builder).target_energy(0.6f);
        verify(builder).target_valence(0.7f);
        verify(builder).target_tempo(100.0f);
        verify(builder).target_acousticness(0.3f);
        verify(builder).target_instrumentalness(0.4f);
        verify(builder).target_liveness(0.5f);
        verify(builder).target_speechiness(0.6f);
        verifyNoMoreInteractions(builder);
    }

    @Test
    void testSetMedianAudioFeatures_EmptyMap() {
        // Act
        audioFeatureSetter.setMedianAudioFeatures(builder, audioFeatures);

        // Assert
        verifyNoInteractions(builder);
    }

    @Test
    void testSetModeValues() {
        // Arrange
        Map<String, Object> modeValues = new HashMap<>();
        modeValues.put("key", 5);
        modeValues.put("mode", "MAJOR");
        modeValues.put("time_signature", 4);

        // Act
        audioFeatureSetter.setModeValues(builder, modeValues);

        // Assert
        verify(builder).target_key(5);
        verify(builder).target_mode(1);
        verify(builder).target_time_signature(4);
        verifyNoMoreInteractions(builder);
    }

    @Test
    void testSetModeValues_EmptyMap() {
        // Arrange
        Map<String, Object> modeValues = new HashMap<>();

        // Act
        audioFeatureSetter.setModeValues(builder, modeValues);

        // Assert
        verifyNoInteractions(builder);
    }

    @Test
    void testSetModeValues_MinorMode() {
        // Arrange
        Map<String, Object> modeValues = new HashMap<>();
        modeValues.put("mode", "MINOR");

        // Act
        audioFeatureSetter.setModeValues(builder, modeValues);

        // Assert
        verify(builder).target_mode(0);
        verifyNoMoreInteractions(builder);
    }
}
