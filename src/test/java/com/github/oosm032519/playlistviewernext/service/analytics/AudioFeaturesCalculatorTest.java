package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AudioFeaturesCalculatorTest {

    @Mock
    private AudioFeatures audioFeatures1;

    @Mock
    private AudioFeatures audioFeatures2;

    @Nested
    @DisplayName("calculateAverageAudioFeatures()のテスト")
    class CalculateAverageAudioFeaturesTest {

        /**
         * 複数のトラックのAudioFeaturesから、各特徴量の平均値が正しく計算されることを確認する。
         */
        @Test
        @DisplayName("正常系：平均値が正しく計算される")
        void calculateAverageSuccess() {
            // Arrange: モックの設定
            when(audioFeatures1.getDanceability()).thenReturn(0.8f);
            when(audioFeatures1.getEnergy()).thenReturn(0.6f);
            when(audioFeatures2.getDanceability()).thenReturn(0.4f);
            when(audioFeatures2.getEnergy()).thenReturn(0.2f);

            // テストデータの準備
            List<Map<String, Object>> trackList = new ArrayList<>();
            Map<String, Object> track1 = new HashMap<>();
            Map<String, Object> track2 = new HashMap<>();
            track1.put("audioFeatures", audioFeatures1);
            track2.put("audioFeatures", audioFeatures2);
            trackList.add(track1);
            trackList.add(track2);

            // Act: テスト対象メソッドの実行
            Map<String, Float> result = AudioFeaturesCalculator.calculateAverageAudioFeatures(trackList);

            // Assert: 検証
            assertThat(result)
                    .containsEntry("danceability", 0.6f)
                    .containsEntry("energy", 0.4f);
        }

        /**
         * 空のトラックリストが与えられた場合、InvalidRequestExceptionがスローされることを確認する。
         */
        @Test
        @DisplayName("異常系：空のトラックリストでInvalidRequestExceptionが発生")
        void throwExceptionWhenTrackListEmpty() {
            // Arrange: 空のトラックリスト
            List<Map<String, Object>> emptyTrackList = new ArrayList<>();

            // Act & Assert: 例外がスローされることを確認
            assertThatThrownBy(() ->
                    AudioFeaturesCalculator.calculateAverageAudioFeatures(emptyTrackList))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("トラックリストが空です");
        }

        /**
         * トラックリストにAudioFeaturesがnullのトラックが含まれている場合、InvalidRequestExceptionがスローされることを確認する。
         */
        @Test
        @DisplayName("異常系：AudioFeaturesがnullでInvalidRequestExceptionが発生")
        void throwExceptionWhenAudioFeaturesNull() {
            // Arrange: AudioFeaturesがnullのトラックを含むリスト
            List<Map<String, Object>> trackList = new ArrayList<>();
            Map<String, Object> track = new HashMap<>();
            track.put("audioFeatures", null);
            trackList.add(track);

            // Act & Assert: 例外がスローされることを確認
            assertThatThrownBy(() ->
                    AudioFeaturesCalculator.calculateAverageAudioFeatures(trackList))
                    .isInstanceOf(InvalidRequestException.class)
                    .hasMessageContaining("AudioFeaturesがnullです");
        }
    }

    @Nested
    @DisplayName("calculateMaxAudioFeatures()のテスト")
    class CalculateMaxAudioFeaturesTest {

        /**
         * 複数のトラックのAudioFeaturesから、各特徴量の最大値が正しく計算されることを確認する。
         */
        @Test
        @DisplayName("正常系：最大値が正しく計算される")
        void calculateMaxSuccess() {
            // Arrange: モックの設定
            when(audioFeatures1.getDanceability()).thenReturn(0.8f);
            when(audioFeatures1.getEnergy()).thenReturn(0.6f);
            when(audioFeatures2.getDanceability()).thenReturn(0.4f);
            when(audioFeatures2.getEnergy()).thenReturn(0.2f);

            // テストデータの準備
            List<Map<String, Object>> trackList = new ArrayList<>();
            Map<String, Object> track1 = new HashMap<>();
            Map<String, Object> track2 = new HashMap<>();
            track1.put("audioFeatures", audioFeatures1);
            track2.put("audioFeatures", audioFeatures2);
            trackList.add(track1);
            trackList.add(track2);

            // Act: テスト対象メソッドの実行
            Map<String, Float> result = AudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);

            // Assert: 検証
            assertThat(result)
                    .containsEntry("danceability", 0.8f)
                    .containsEntry("energy", 0.6f);
        }
    }
}
