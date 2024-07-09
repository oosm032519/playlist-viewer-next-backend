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
        audioFeatures.put("danceability", 0.8f);
        audioFeatures.put("energy", 0.7f);
        audioFeatures.put("valence", 0.6f);

        audioFeatureSetter.setMaxAudioFeatures(builder, audioFeatures);

        verify(builder).max_danceability(0.8f);
        verify(builder).max_energy(0.7f);
        verify(builder).max_valence(0.6f);
        verifyNoMoreInteractions(builder);
    }

    @Test
    void testSetMinAudioFeatures() {
        audioFeatures.put("danceability", 0.2f);
        audioFeatures.put("energy", 0.3f);
        audioFeatures.put("valence", 0.4f);

        audioFeatureSetter.setMinAudioFeatures(builder, audioFeatures);

        verify(builder).min_danceability(0.2f);
        verify(builder).min_energy(0.3f);
        verify(builder).min_valence(0.4f);
        verifyNoMoreInteractions(builder);
    }

    @Test
    void testSetMedianAudioFeatures() {
        audioFeatures.put("danceability", 0.5f);
        audioFeatures.put("energy", 0.6f);
        audioFeatures.put("valence", 0.7f);

        audioFeatureSetter.setMedianAudioFeatures(builder, audioFeatures);

        verify(builder).target_danceability(0.5f);
        verify(builder).target_energy(0.6f);
        verify(builder).target_valence(0.7f);
        verifyNoMoreInteractions(builder);
    }

    @Test
    void testSetModeValues() {
        Map<String, Object> modeValues = new HashMap<>();
        modeValues.put("key", 5);
        modeValues.put("mode", "MAJOR");
        modeValues.put("time_signature", 4);

        audioFeatureSetter.setModeValues(builder, modeValues);

        verify(builder).target_key(5);
        verify(builder).target_mode(1);
        verify(builder).target_time_signature(4);
        verifyNoMoreInteractions(builder);
    }
}
