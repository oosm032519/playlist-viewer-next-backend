package com.github.oosm032519.playlistviewernext.service.playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyTrackServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private GetAudioFeaturesForSeveralTracksRequest.Builder requestBuilder;

    @Mock
    private GetAudioFeaturesForSeveralTracksRequest request;

    private SpotifyTrackService spotifyTrackService;

    @BeforeEach
    void setUp() {
        spotifyTrackService = new SpotifyTrackService(spotifyApi);
    }

    @Test
    @DisplayName("100曲以下のトラックIDリストに対して正常にAudioFeaturesを取得できること")
    void getAudioFeaturesForTracksUnder100() throws Exception {
        // テストデータの準備
        List<String> trackIds = Arrays.asList("track1", "track2", "track3");
        AudioFeatures[] expectedFeatures = {
                mock(AudioFeatures.class),
                mock(AudioFeatures.class),
                mock(AudioFeatures.class)
        };

        // モックの設定
        when(spotifyApi.getAudioFeaturesForSeveralTracks(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute()).thenReturn(expectedFeatures);

        // テスト実行
        List<AudioFeatures> result = spotifyTrackService.getAudioFeaturesForTracks(trackIds);

        // 検証
        assertThat(result).hasSize(3)
                .containsExactlyElementsOf(Arrays.asList(expectedFeatures));
        verify(spotifyApi, times(1)).getAudioFeaturesForSeveralTracks(anyString());
    }

    @Test
    @DisplayName("100曲を超えるトラックIDリストが正しく分割して処理されること")
    void getAudioFeaturesForTracksOver100() throws Exception {
        // 150曲のトラックIDリストを生成
        List<String> trackIds = IntStream.range(0, 150)
                .mapToObj(i -> "track" + i)
                .collect(Collectors.toList());

        AudioFeatures[] firstBatch = new AudioFeatures[100];
        AudioFeatures[] secondBatch = new AudioFeatures[50];
        Arrays.fill(firstBatch, mock(AudioFeatures.class));
        Arrays.fill(secondBatch, mock(AudioFeatures.class));

        // モックの設定
        when(spotifyApi.getAudioFeaturesForSeveralTracks(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute())
                .thenReturn(firstBatch)
                .thenReturn(secondBatch);

        // テスト実行
        List<AudioFeatures> result = spotifyTrackService.getAudioFeaturesForTracks(trackIds);

        // 検証
        assertThat(result).hasSize(150);
        verify(spotifyApi, times(2)).getAudioFeaturesForSeveralTracks(anyString());
    }

    @Test
    @DisplayName("SpotifyWebApiExceptionが発生した場合、適切に例外がスローされること")
    void getAudioFeaturesForTracksWithSpotifyApiException() throws Exception {
        // テストデータの準備
        List<String> trackIds = Arrays.asList("track1", "track2");
        SpotifyWebApiException apiException = new SpotifyWebApiException();

        // モックの設定
        when(spotifyApi.getAudioFeaturesForSeveralTracks(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute()).thenThrow(apiException);

        // テスト実行と検証
        assertThatThrownBy(() -> spotifyTrackService.getAudioFeaturesForTracks(trackIds))
                .isInstanceOf(SpotifyWebApiException.class);
    }
}
