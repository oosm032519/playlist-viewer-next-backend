package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyTrackServiceTest {

    @Mock
    SpotifyApi spotifyApi;

    @Mock
    WebClient.Builder webClientBuilder; // WebClient.Builder をモックに変更

    @Mock
    WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private GetAudioFeaturesForSeveralTracksRequest.Builder requestBuilder;

    @Mock
    private GetAudioFeaturesForSeveralTracksRequest request;

    private SpotifyTrackService spotifyTrackService;

    @BeforeEach
    void setUp() {
        // WebClient.Builder のモックの振る舞いを設定
        spotifyTrackService = new SpotifyTrackService(spotifyApi, webClient);
        // モックモードを無効に設定
        ReflectionTestUtils.setField(spotifyTrackService, "mockEnabled", false);
        ReflectionTestUtils.setField(spotifyTrackService, "mockApiUrl", "");
    }

    /**
     * 100曲以下のトラックIDリストに対して、AudioFeaturesが正常に取得できることを確認する。
     */
    @Test
    @DisplayName("100曲以下のトラックIDリストに対して正常にAudioFeaturesを取得できること")
    void getAudioFeaturesForTracksUnder100() throws Exception {
        // Arrange: テストデータの準備
        List<String> trackIds = Arrays.asList("track1", "track2", "track3");
        AudioFeatures audioFeatures1 = mock(AudioFeatures.class);
        AudioFeatures audioFeatures2 = mock(AudioFeatures.class);
        AudioFeatures audioFeatures3 = mock(AudioFeatures.class);
        AudioFeatures[] expectedFeatures = {audioFeatures1, audioFeatures2, audioFeatures3};

        // モックの設定
        when(spotifyApi.getAudioFeaturesForSeveralTracks(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute()).thenReturn(expectedFeatures);

        // Act: テスト対象メソッドの実行
        List<AudioFeatures> result = spotifyTrackService.getAudioFeaturesForTracks(trackIds);

        // Assert: 結果の検証
        assertThat(result).hasSize(3)
                .containsExactlyElementsOf(Arrays.asList(expectedFeatures));
        verify(spotifyApi, times(1)).getAudioFeaturesForSeveralTracks(anyString());
    }

    /**
     * 100曲を超えるトラックIDリストが与えられた場合、リクエストが適切に分割され、すべてのAudioFeaturesが取得されることを確認する。
     */
    @Test
    @DisplayName("100曲を超えるトラックIDリストが正しく分割して処理されること")
    void getAudioFeaturesForTracksOver100() throws Exception {
        // Arrange: 150曲のトラックIDリストを生成
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

        // Act: テスト対象メソッドの実行
        List<AudioFeatures> result = spotifyTrackService.getAudioFeaturesForTracks(trackIds);

        // Assert: 結果の検証
        assertThat(result).hasSize(150);
        verify(spotifyApi, times(2)).getAudioFeaturesForSeveralTracks(anyString());
    }

    /**
     * Spotify API呼び出し中にSpotifyWebApiExceptionが発生した場合、例外が適切にスローされることを確認する。
     */
    @Test
    @DisplayName("SpotifyWebApiExceptionが発生した場合、適切に例外がスローされること")
    void getAudioFeaturesForTracksWithSpotifyApiException() throws Exception {
        // Arrange: テストデータの準備
        List<String> trackIds = Arrays.asList("track1", "track2");

        // モックの設定
        when(spotifyApi.getAudioFeaturesForSeveralTracks(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute()).thenThrow(new SpotifyWebApiException("Spotify API Error"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> spotifyTrackService.getAudioFeaturesForTracks(trackIds))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessageContaining("Spotify API Error");
    }

    /**
     * モックモードが有効な場合、モックAPIからAudioFeaturesが取得されることを確認する。
     */
    @Test
    @DisplayName("モックモードが有効な場合、モックAPIからデータを取得すること")
    void getAudioFeaturesForTracks_MockModeEnabled() throws Exception {
        // Arrange: モックモードを有効に設定
        ReflectionTestUtils.setField(spotifyTrackService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyTrackService, "mockApiUrl", "http://mock-api");

        List<String> trackIds = List.of("track1", "track2");
        AudioFeatures audioFeatures1 = mock(AudioFeatures.class);
        AudioFeatures audioFeatures2 = mock(AudioFeatures.class);
        List<AudioFeatures> expectedFeatures = Arrays.asList(audioFeatures1, audioFeatures2);

        // WebClientのモック設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("http://mock-api/tracks/audio-features"), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<List<AudioFeatures>>>notNull()))
                .thenReturn(Mono.just(expectedFeatures));

        // Act: テスト対象メソッドの実行
        List<AudioFeatures> result = spotifyTrackService.getAudioFeaturesForTracks(trackIds);

        // Assert: 結果の検証
        assertThat(result).hasSize(2).containsExactlyElementsOf(expectedFeatures);
        verify(webClient, times(1)).get();
        verify(spotifyApi, never()).getAudioFeaturesForSeveralTracks(anyString());
    }

    /**
     * モックモードが無効な場合、Spotify APIからAudioFeaturesが取得されることを確認する。
     */
    @Test
    @DisplayName("モックモードが無効な場合、Spotify APIからデータを取得すること")
    void getAudioFeaturesForTracks_MockModeDisabled() throws Exception {
        // Arrange: モックモードを無効に設定
        ReflectionTestUtils.setField(spotifyTrackService, "mockEnabled", false);

        List<String> trackIds = List.of("track1", "track2");
        AudioFeatures audioFeatures1 = mock(AudioFeatures.class);
        AudioFeatures audioFeatures2 = mock(AudioFeatures.class);
        AudioFeatures[] expectedFeatures = {audioFeatures1, audioFeatures2};

        when(spotifyApi.getAudioFeaturesForSeveralTracks(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute()).thenReturn(expectedFeatures);

        // Act: テスト対象メソッドの実行
        List<AudioFeatures> result = spotifyTrackService.getAudioFeaturesForTracks(trackIds);

        // Assert: 結果の検証
        assertThat(result).hasSize(2).containsExactly(expectedFeatures);
        verify(spotifyApi, times(1)).getAudioFeaturesForSeveralTracks(anyString());
        verify(webClient, never()).get();
    }

    /**
     * モックモードが有効だが、モックAPIのURLが設定されていない場合、Spotify APIからAudioFeaturesが取得されることを確認する。
     */
    @Test
    @DisplayName("モックモードが有効だが、モックAPIのURLが設定されていない場合、Spotify APIからデータを取得すること")
    void getAudioFeaturesForTracks_MockModeEnabledButNoMockApiUrl() throws Exception {
        // Arrange: モックモードを有効にし、モックAPI URLをnullに設定
        ReflectionTestUtils.setField(spotifyTrackService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyTrackService, "mockApiUrl", null);

        List<String> trackIds = List.of("track1", "track2");
        AudioFeatures audioFeatures1 = mock(AudioFeatures.class);
        AudioFeatures audioFeatures2 = mock(AudioFeatures.class);
        AudioFeatures[] expectedFeatures = {audioFeatures1, audioFeatures2};

        when(spotifyApi.getAudioFeaturesForSeveralTracks(anyString())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute()).thenReturn(expectedFeatures);

        // Act: テスト対象メソッドの実行
        List<AudioFeatures> result = spotifyTrackService.getAudioFeaturesForTracks(trackIds);

        // Assert: 結果の検証
        assertThat(result).hasSize(2).containsExactly(expectedFeatures);
        verify(spotifyApi, times(1)).getAudioFeaturesForSeveralTracks(anyString());
        verify(webClient, never()).get();
    }

    /**
     * モックモードが有効で、モックAPIのURLが空の場合、Spotify APIからAudioFeaturesが取得されることを確認する。
     */
    @Test
    @DisplayName("モックモードが有効で、モックAPIのURLが空の場合、Spotify APIからデータを取得すること")
    void getAudioFeaturesForTracks_MockModeEnabledAndMockApiUrlIsEmpty() throws Exception {
        // Arrange: モックモードを有効にし、モックAPI URLを空文字列に設定
        ReflectionTestUtils.setField(spotifyTrackService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyTrackService, "mockApiUrl", "");

        List<String> trackIds = List.of("track1", "track2");
        AudioFeatures audioFeatures1 = mock(AudioFeatures.class);
        AudioFeatures audioFeatures2 = mock(AudioFeatures.class);
        AudioFeatures[] expectedFeatures = {audioFeatures1, audioFeatures2};

        Mockito.lenient().when(spotifyApi.getAudioFeaturesForSeveralTracks(ArgumentMatchers.any())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute()).thenReturn(expectedFeatures);

        // Act: テスト対象メソッドの実行
        List<AudioFeatures> result = spotifyTrackService.getAudioFeaturesForTracks(trackIds);

        // Assert: 結果の検証
        assertThat(result).hasSize(2).containsExactly(expectedFeatures);
        verify(spotifyApi, times(1)).getAudioFeaturesForSeveralTracks(anyString());
        verify(webClient, never()).get();
    }

    /**
     * モックモードが有効で、モックAPI呼び出し時にWebClientResponseExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    @DisplayName("モックモードが有効で、モックAPI呼び出し時にWebClientResponseExceptionが発生した場合、InternalServerExceptionがスローされること")
    void getAudioFeaturesForTracks_MockModeEnabled_WebClientResponseException() {
        // Arrange: モックモードを有効にし、モックAPI URLとWebClientを設定
        ReflectionTestUtils.setField(spotifyTrackService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyTrackService, "mockApiUrl", "http://mock-api");
        ReflectionTestUtils.setField(spotifyTrackService, "webClient", webClient);

        List<String> trackIds = List.of("track1", "track2");

        // WebClientのモック設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("http://mock-api/tracks/audio-features"), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<List<AudioFeatures>>>notNull()))
                .thenReturn(Mono.error(new WebClientResponseException("Mock API Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> spotifyTrackService.getAudioFeaturesForTracks(trackIds))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Error calling mock API");
    }

    /**
     * モックモードが有効で、モックAPI呼び出し時にWebClientRequestExceptionが発生した場合、例外がそのままスローされることを確認する。
     */
    @Test
    @DisplayName("モックモードが有効で、モックAPI呼び出し時にWebClientRequestExceptionが発生した場合、例外がそのままスローされること")
    void getAudioFeaturesForTracks_MockModeEnabled_WebClientRequestException() {
        // Arrange: モックモードを有効にし、モックAPI URLとWebClientを設定
        ReflectionTestUtils.setField(spotifyTrackService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyTrackService, "mockApiUrl", "http://mock-api");
        ReflectionTestUtils.setField(spotifyTrackService, "webClient", webClient);

        List<String> trackIds = List.of("track1", "track2");

        // WebClientのモック設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("http://mock-api/tracks/audio-features"), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        HttpHeaders headers = new HttpHeaders();
        when(responseSpec.bodyToMono(ArgumentMatchers.<ParameterizedTypeReference<List<AudioFeatures>>>notNull()))
                .thenReturn(Mono.error(new WebClientRequestException(new RuntimeException("Connection refused"), HttpMethod.GET, null, headers)));

        // Act & Assert: WebClientRequestExceptionがそのままスローされることの確認
        assertThatThrownBy(() -> spotifyTrackService.getAudioFeaturesForTracks(trackIds))
                .isInstanceOf(WebClientRequestException.class);
    }
}
