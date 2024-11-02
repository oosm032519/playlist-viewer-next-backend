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
}
