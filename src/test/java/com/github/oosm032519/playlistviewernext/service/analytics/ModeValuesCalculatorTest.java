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
    public void testCalculateModeValues() {
        // テストデータの準備
        List<Map<String, Object>> trackList = new ArrayList<>();

        Map<String, Object> trackData1 = new HashMap<>();
        AudioFeatures audioFeatures1 = createAudioFeatures(1, 4, "MAJOR");
        trackData1.put("audioFeatures", audioFeatures1);
        trackList.add(trackData1);

        Map<String, Object> trackData2 = new HashMap<>();
        AudioFeatures audioFeatures2 = createAudioFeatures(1, 4, "MAJOR");
        trackData2.put("audioFeatures", audioFeatures2);
        trackList.add(trackData2);

        Map<String, Object> trackData3 = new HashMap<>();
        AudioFeatures audioFeatures3 = createAudioFeatures(2, 3, "MINOR");
        trackData3.put("audioFeatures", audioFeatures3);
        trackList.add(trackData3);

        // メソッドの実行
        Map<String, Object> result = modeValuesCalculator.calculateModeValues(trackList);

        // 結果の検証
        assertThat(result).isNotNull();
        assertThat(result.get("key")).isEqualTo(1);
        assertThat(result.get("time_signature")).isEqualTo(4);
        assertThat(result.get("mode")).isEqualTo("MAJOR");
    }

    private AudioFeatures createAudioFeatures(int key, int timeSignature, String mode) {
        return new AudioFeatures.Builder()
                .setKey(key)
                .setTimeSignature(timeSignature)
                .setMode(Modality.keyOf(mode.equals("MAJOR") ? 1 : 0))
                .build();
    }
}
