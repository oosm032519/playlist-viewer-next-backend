// MedianAudioFeaturesCalculatorTest.java

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

/**
 * MedianAudioFeaturesCalculatorのテストクラス
 */
class MedianAudioFeaturesCalculatorTest {

    /**
     * テスト対象のMedianAudioFeaturesCalculatorインスタンス
     */
    @InjectMocks
    private MedianAudioFeaturesCalculator medianAudioFeaturesCalculator;

    /**
     * 各テストの前にモックを初期化する
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * calculateMedianAudioFeaturesメソッドのテスト
     * 複数のAudioFeaturesオブジェクトから中央値を計算する
     */
    @Test
    void testCalculateMedianAudioFeatures() {
        // Arrange: モックのAudioFeaturesオブジェクトを作成し、各プロパティの値を設定する
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
        when(audioFeatures2.getDanceability()).thenReturn(0.6f);
        when(audioFeatures2.getEnergy()).thenReturn(0.7f);
        when(audioFeatures2.getValence()).thenReturn(0.8f);
        when(audioFeatures2.getTempo()).thenReturn(130.0f);
        when(audioFeatures2.getAcousticness()).thenReturn(0.2f);
        when(audioFeatures2.getInstrumentalness()).thenReturn(0.1f);
        when(audioFeatures2.getLiveness()).thenReturn(0.3f);
        when(audioFeatures2.getSpeechiness()).thenReturn(0.4f);

        List<Map<String, Object>> trackList = new ArrayList<>();
        Map<String, Object> trackData1 = new HashMap<>();
        trackData1.put("audioFeatures", audioFeatures1);
        trackList.add(trackData1);

        Map<String, Object> trackData2 = new HashMap<>();
        trackData2.put("audioFeatures", audioFeatures2);
        trackList.add(trackData2);

        // Act: メソッドを実行し、結果を取得する
        Map<String, Float> result = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);

        // Assert: 結果が期待通りであることを検証する
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

    /**
     * calculateMedianAudioFeaturesメソッドのテスト
     * 空のリストを入力した場合の動作を確認する
     */
    @Test
    void testCalculateMedianAudioFeaturesWithEmptyList() {
        // Arrange: 空のリストを作成する
        List<Map<String, Object>> trackList = new ArrayList<>();

        // Act: メソッドを実行し、結果を取得する
        Map<String, Float> result = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);

        // Assert: 結果が空であることを検証する
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    /**
     * calculateMedianAudioFeaturesメソッドのテスト
     * AudioFeaturesがnullの場合の動作を確認する
     */
    @Test
    void testCalculateMedianAudioFeaturesWithNullAudioFeatures() {
        // Arrange: AudioFeaturesがnullのデータを含むリストを作成する
        List<Map<String, Object>> trackList = new ArrayList<>();
        Map<String, Object> trackData = new HashMap<>();
        trackData.put("audioFeatures", null);
        trackList.add(trackData);

        // Act: メソッドを実行し、結果を取得する
        Map<String, Float> result = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);

        // Assert: 結果が空であることを検証する
        assertThat(result).isNotNull();
        assertThat(result).isEmpty();
    }

    /**
     * calculateMedianAudioFeaturesメソッドのテスト
     * リストに1つのAudioFeaturesオブジェクトのみが含まれる場合の動作を確認する
     */
    @Test
    void testCalculateMedianAudioFeaturesWithSingleElement() {
        // Arrange: モックのAudioFeaturesオブジェクトを作成し、各プロパティの値を設定する
        AudioFeatures audioFeatures = mock(AudioFeatures.class);
        when(audioFeatures.getDanceability()).thenReturn(0.5f);
        when(audioFeatures.getEnergy()).thenReturn(0.6f);
        when(audioFeatures.getValence()).thenReturn(0.7f);
        when(audioFeatures.getTempo()).thenReturn(120.0f);
        when(audioFeatures.getAcousticness()).thenReturn(0.1f);
        when(audioFeatures.getInstrumentalness()).thenReturn(0.0f);
        when(audioFeatures.getLiveness()).thenReturn(0.2f);
        when(audioFeatures.getSpeechiness()).thenReturn(0.3f);

        List<Map<String, Object>> trackList = new ArrayList<>();
        Map<String, Object> trackData = new HashMap<>();
        trackData.put("audioFeatures", audioFeatures);
        trackList.add(trackData);

        // Act: メソッドを実行し、結果を取得する
        Map<String, Float> result = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);

        // Assert: 結果が期待通りであることを検証する
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
