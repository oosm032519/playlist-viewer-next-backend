package com.github.oosm032519.playlistviewernext.service.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import se.michaelthelin.spotify.enums.Modality;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class ModeValuesCalculatorTest {

    @InjectMocks
    private ModeValuesCalculator modeValuesCalculator;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void shouldCalculateModeValuesCorrectly() {
        // Arrange
        List<Map<String, Object>> trackList = Arrays.asList(
                createTrackData(1, 4, "MAJOR"),
                createTrackData(1, 4, "MAJOR"),
                createTrackData(2, 3, "MINOR")
        );

        // Act
        Map<String, Object> result = modeValuesCalculator.calculateModeValues(trackList);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("key")).isEqualTo(1);
        assertThat(result.get("time_signature")).isEqualTo(4);
        assertThat(result.get("mode")).isEqualTo("MAJOR");
    }

    @Test
    public void shouldHandleEmptyTrackList() {
        // Arrange
        List<Map<String, Object>> emptyTrackList = new ArrayList<>();

        // Act
        Map<String, Object> result = modeValuesCalculator.calculateModeValues(emptyTrackList);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    public void shouldHandleNullAudioFeatures() {
        // Arrange
        List<Map<String, Object>> trackList = Arrays.asList(
                createTrackDataWithNullAudioFeatures(),
                createTrackData(1, 4, "MAJOR")
        );

        // Act
        Map<String, Object> result = modeValuesCalculator.calculateModeValues(trackList);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.get("key")).isEqualTo(1);
        assertThat(result.get("time_signature")).isEqualTo(4);
        assertThat(result.get("mode")).isEqualTo("MAJOR");
    }

    private Map<String, Object> createTrackDataWithNullAudioFeatures() {
        Map<String, Object> trackData = new HashMap<>();
        trackData.put("audioFeatures", null);
        return trackData;
    }

    private Map<String, Object> createTrackData(int key, int timeSignature, String mode) {
        Map<String, Object> trackData = new HashMap<>();
        trackData.put("audioFeatures", createAudioFeatures(key, timeSignature, mode));
        return trackData;
    }

    @Test
    public void shouldHandleAllNullAudioFeatures() {
        // Arrange
        List<Map<String, Object>> trackList = Arrays.asList(
                createTrackDataWithNullAudioFeatures(),
                createTrackDataWithNullAudioFeatures()
        );

        // Act
        Map<String, Object> result = modeValuesCalculator.calculateModeValues(trackList);

        // Assert
        assertThat(result).isEmpty();
    }

    private AudioFeatures createAudioFeatures(int key, int timeSignature, String mode) {
        return new AudioFeatures.Builder()
                .setKey(key)
                .setTimeSignature(timeSignature)
                .setMode(Modality.keyOf(mode.equals("MAJOR") ? 1 : 0))
                .build();
    }
}
