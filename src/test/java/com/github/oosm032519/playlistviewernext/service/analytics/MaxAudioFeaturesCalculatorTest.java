package com.github.oosm032519.playlistviewernext.service.analytics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

public class MaxAudioFeaturesCalculatorTest {

    @InjectMocks
    private MaxAudioFeaturesCalculator maxAudioFeaturesCalculator;

    // テストの前にMockitoのモックを初期化するメソッド
    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // 空のトラックリストを使用してcalculateMaxAudioFeaturesメソッドをテストする
    @Test
    public void testCalculateMaxAudioFeatures_withEmptyTrackList() {
        // Arrange
        List<Map<String, Object>> trackList = new ArrayList<>();

        // Act
        Map<String, Float> result = maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);

        // Assert
        assertThat(result).containsEntry("danceability", 0.0f)
                .containsEntry("energy", 0.0f)
                .containsEntry("valence", 0.0f)
                .containsEntry("tempo", 0.0f)
                .containsEntry("acousticness", 0.0f)
                .containsEntry("instrumentalness", 0.0f)
                .containsEntry("liveness", 0.0f)
                .containsEntry("speechiness", 0.0f);
    }

    // 有効なトラックリストを使用してcalculateMaxAudioFeaturesメソッドをテストする
    @Test
    public void testCalculateMaxAudioFeatures_withValidTrackList() {
        // Arrange
        List<Map<String, Object>> trackList = new ArrayList<>();

        // AudioFeaturesオブジェクトを作成
        AudioFeatures audioFeatures1 = new AudioFeatures.Builder()
                .setDanceability(0.5f)
                .setEnergy(0.6f)
                .setValence(0.7f)
                .setTempo(120.0f)
                .setAcousticness(0.1f)
                .setInstrumentalness(0.0f)
                .setLiveness(0.2f)
                .setSpeechiness(0.3f)
                .build();

        AudioFeatures audioFeatures2 = new AudioFeatures.Builder()
                .setDanceability(0.8f)
                .setEnergy(0.9f)
                .setValence(0.4f)
                .setTempo(130.0f)
                .setAcousticness(0.2f)
                .setInstrumentalness(0.1f)
                .setLiveness(0.3f)
                .setSpeechiness(0.4f)
                .build();

        // トラックデータを作成
        Map<String, Object> trackData1 = new HashMap<>();
        trackData1.put("audioFeatures", audioFeatures1);

        Map<String, Object> trackData2 = new HashMap<>();
        trackData2.put("audioFeatures", audioFeatures2);

        // トラックリストに追加
        trackList.add(trackData1);
        trackList.add(trackData2);

        // Act
        Map<String, Float> result = maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);

        // Assert
        assertThat(result).containsEntry("danceability", 0.8f)
                .containsEntry("energy", 0.9f)
                .containsEntry("valence", 0.7f)
                .containsEntry("tempo", 130.0f)
                .containsEntry("acousticness", 0.2f)
                .containsEntry("instrumentalness", 0.1f)
                .containsEntry("liveness", 0.3f)
                .containsEntry("speechiness", 0.4f);
    }

    // nullのオーディオフィーチャーを含むトラックリストを使用してcalculateMaxAudioFeaturesメソッドをテストする
    @Test
    public void testCalculateMaxAudioFeatures_withNullAudioFeatures() {
        // Arrange
        List<Map<String, Object>> trackList = new ArrayList<>();

        // nullのオーディオフィーチャーを含むトラックデータを作成
        Map<String, Object> trackData1 = new HashMap<>();
        trackData1.put("audioFeatures", null);

        // トラックリストに追加
        trackList.add(trackData1);

        // Act
        Map<String, Float> result = maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);

        // Assert
        assertThat(result).containsEntry("danceability", 0.0f)
                .containsEntry("energy", 0.0f)
                .containsEntry("valence", 0.0f)
                .containsEntry("tempo", 0.0f)
                .containsEntry("acousticness", 0.0f)
                .containsEntry("instrumentalness", 0.0f)
                .containsEntry("liveness", 0.0f)
                .containsEntry("speechiness", 0.0f);
    }
}
