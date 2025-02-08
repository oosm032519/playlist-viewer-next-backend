package com.github.oosm032519.playlistviewernext.service.recommendation;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.service.analytics.AudioFeatureSetter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyRecommendationServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private AudioFeatureSetter audioFeatureSetter;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private SpotifyRecommendationService spotifyRecommendationService;

    @BeforeEach
    void setUp() {
        // WebClient.Builder のモックの振る舞いを設定
        when(webClientBuilder.build()).thenReturn(webClient);
        spotifyRecommendationService = new SpotifyRecommendationService(spotifyApi, audioFeatureSetter, webClientBuilder);
        // モックモードを無効に設定
        ReflectionTestUtils.setField(spotifyRecommendationService, "mockEnabled", false);
        ReflectionTestUtils.setField(spotifyRecommendationService, "mockApiUrl", "");
    }

    /**
     * 有効なパラメータを使用して推奨トラックを取得し、結果が期待通りであることを確認する。
     */
    @Test
    void getRecommendations_正常系_推奨トラックが取得できる場合() throws SpotifyWebApiException, IOException, org.apache.hc.core5.http.ParseException {
        // Arrange: テストデータの準備
        List<String> seedArtists = List.of("artistId1", "artistId2");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        Map<String, Float> minAudioFeatures = new HashMap<>();
        Track[] tracks = new Track[2];
        Recommendations recommendations = new Recommendations.Builder().setTracks(tracks).build();

        // モックの設定
        GetRecommendationsRequest.Builder builderMock = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest requestMock = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(builderMock);
        when(builderMock.seed_artists(anyString())).thenReturn(builderMock);
        when(builderMock.limit(anyInt())).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(requestMock);
        when(requestMock.execute()).thenReturn(recommendations);

        // Act: テスト対象メソッドの実行
        List<Track> result = spotifyRecommendationService.getRecommendations(seedArtists, maxAudioFeatures, minAudioFeatures);

        // Assert: 結果の検証
        assertThat(result).hasSize(2);

        // AudioFeatureSetterのメソッドが呼び出されたことを確認
        verify(audioFeatureSetter).setMaxAudioFeatures(builderMock, maxAudioFeatures);
        verify(audioFeatureSetter).setMinAudioFeatures(builderMock, minAudioFeatures);
    }

    /**
     * 推奨トラックが空の場合に、空のリストが返されることを確認する。
     */
    @Test
    void getRecommendations_正常系_推奨トラックがない場合() throws SpotifyWebApiException, IOException, org.apache.hc.core5.http.ParseException {
        // Arrange: テストデータの準備
        List<String> seedArtists = List.of("artistId1", "artistId2");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        Map<String, Float> minAudioFeatures = new HashMap<>();

        // モックの設定
        GetRecommendationsRequest.Builder builderMock = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest requestMock = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(builderMock);
        when(builderMock.seed_artists(anyString())).thenReturn(builderMock);
        when(builderMock.limit(anyInt())).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(requestMock);
        when(requestMock.execute()).thenReturn(new Recommendations.Builder().build()); // 空の Recommendations オブジェクトを返す

        // Act: テスト対象メソッドの実行
        List<Track> result = spotifyRecommendationService.getRecommendations(seedArtists, maxAudioFeatures, minAudioFeatures);

        // Assert: 結果の検証
        assertThat(result).isEmpty();

        // AudioFeatureSetterのメソッドが呼び出されたことを確認
        verify(audioFeatureSetter).setMaxAudioFeatures(builderMock, maxAudioFeatures);
        verify(audioFeatureSetter).setMinAudioFeatures(builderMock, minAudioFeatures);
    }

    /**
     * シードアーティストがnullの場合に、空のリストが返されることを確認する。
     */
    @Test
    void getRecommendations_異常系_seedArtistsがnullの場合() throws SpotifyWebApiException {
        // Act: テスト対象メソッドの実行
        List<Track> result = spotifyRecommendationService.getRecommendations(null, new HashMap<>(), new HashMap<>());

        // Assert: 結果の検証
        assertThat(result).isEmpty();
        verifyNoInteractions(spotifyApi);
    }

    /**
     * シードアーティストが空の場合に、空のリストが返されることを確認する。
     */
    @Test
    void getRecommendations_異常系_seedArtistsが空の場合() throws SpotifyWebApiException {
        // Act: テスト対象メソッドの実行
        List<Track> result = spotifyRecommendationService.getRecommendations(Collections.emptyList(), new HashMap<>(), new HashMap<>());

        // Assert: 結果の検証
        assertThat(result).isEmpty();
        verifyNoInteractions(spotifyApi);
    }

    /**
     * SpotifyWebApiExceptionが発生した場合に、例外がそのままスローされることを確認する。
     */
    @Test
    void getRecommendations_異常系_SpotifyWebApiExceptionが発生した場合() throws IOException, org.apache.hc.core5.http.ParseException, SpotifyWebApiException {
        // Arrange: テストデータの準備
        List<String> seedArtists = List.of("artistId1", "artistId2");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        Map<String, Float> minAudioFeatures = new HashMap<>();

        // モックの設定
        GetRecommendationsRequest.Builder builderMock = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest requestMock = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(builderMock);
        // lenient() を使用する場合は、when() の前に記述する
        lenient().when(builderMock.seed_artists(anyString())).thenReturn(builderMock);
        lenient().when(builderMock.limit(anyInt())).thenReturn(builderMock);
        lenient().when(builderMock.build()).thenReturn(requestMock);
        when(requestMock.execute()).thenThrow(new SpotifyWebApiException("Spotify API Error"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> spotifyRecommendationService.getRecommendations(seedArtists, maxAudioFeatures, minAudioFeatures))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessageContaining("Spotify API Error");

        // AudioFeatureSetterのメソッドが呼び出されたことを確認
        verify(audioFeatureSetter).setMaxAudioFeatures(builderMock, maxAudioFeatures);
        verify(audioFeatureSetter).setMinAudioFeatures(builderMock, minAudioFeatures);
    }

    /**
     * その他の例外が発生した場合に、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void getRecommendations_異常系_その他の例外が発生した場合() throws SpotifyWebApiException, org.apache.hc.core5.http.ParseException, IOException {
        // Arrange: テストデータの準備
        List<String> seedArtists = List.of("artistId1", "artistId2");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        Map<String, Float> minAudioFeatures = new HashMap<>();

        // モックの設定
        GetRecommendationsRequest.Builder builderMock = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest requestMock = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(builderMock);
        // lenient() を使用する場合は、when() の前に記述する
        lenient().when(builderMock.seed_artists(anyString())).thenReturn(builderMock);
        lenient().when(builderMock.limit(anyInt())).thenReturn(builderMock);
        lenient().when(builderMock.build()).thenReturn(requestMock);
        when(requestMock.execute()).thenThrow(new IOException("IO Error"));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> spotifyRecommendationService.getRecommendations(seedArtists, maxAudioFeatures, minAudioFeatures))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("推奨トラックの取得中にエラーが発生しました。")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR);

        // AudioFeatureSetterのメソッドが呼び出されたことを確認
        verify(audioFeatureSetter).setMaxAudioFeatures(builderMock, maxAudioFeatures);
        verify(audioFeatureSetter).setMinAudioFeatures(builderMock, minAudioFeatures);
    }

    /**
     * モックモードが有効で、モックAPIのURLがnullまたは空の場合に、実APIが呼び出されることを確認する。
     */
    @Test
    void getRecommendations_mockModeEnabledAndMockApiUrlNullOrEmpty_shouldCallRealApi() throws SpotifyWebApiException, IOException, org.apache.hc.core5.http.ParseException {
        // Arrange: モックモードを有効にし、モックAPI URLを空またはnullに設定
        ReflectionTestUtils.setField(spotifyRecommendationService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyRecommendationService, "mockApiUrl", ""); // or null

        List<String> seedArtists = List.of("artistId1", "artistId2");
        Map<String, Float> maxAudioFeatures = new HashMap<>();
        Map<String, Float> minAudioFeatures = new HashMap<>();
        Track[] tracks = new Track[2];
        Recommendations recommendations = new Recommendations.Builder().setTracks(tracks).build();

        GetRecommendationsRequest.Builder builderMock = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest requestMock = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(builderMock);
        when(builderMock.seed_artists(anyString())).thenReturn(builderMock);
        when(builderMock.limit(anyInt())).thenReturn(builderMock);
        when(builderMock.build()).thenReturn(requestMock);
        when(requestMock.execute()).thenReturn(recommendations);

        // Act: テスト対象メソッドの実行
        List<Track> result = spotifyRecommendationService.getRecommendations(seedArtists, maxAudioFeatures, minAudioFeatures);

        // Assert: 結果の検証
        assertThat(result).hasSize(2);

        // 実APIが呼び出されたことを確認 (audioFeatureSetter メソッドが呼び出される)
        verify(audioFeatureSetter).setMaxAudioFeatures(builderMock, maxAudioFeatures);
        verify(audioFeatureSetter).setMinAudioFeatures(builderMock, minAudioFeatures);
    }

    /**
     * モックモードが有効で、モックAPI呼び出し時にWebClientResponseExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    @DisplayName("モックモードが有効で、モックAPI呼び出し時にWebClientResponseExceptionが発生した場合、InternalServerExceptionがスローされること")
    void getRecommendationsMock_shouldThrowInternalServerException_whenWebClientResponseExceptionOccurs() {
        // Arrange: モックモードを有効にし、モックAPI URLとWebClientを設定
        ReflectionTestUtils.setField(spotifyRecommendationService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyRecommendationService, "mockApiUrl", "http://mock-api-url");
        ReflectionTestUtils.setField(spotifyRecommendationService, "webClient", webClient);

        // WebClientのモック設定 (WebClientResponseExceptionをスロー)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("http://mock-api-url/recommendations"))).thenReturn(requestHeadersSpec); // 正しいURIをeq()で指定
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new WebClientResponseException("Mock API Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> spotifyRecommendationService.getRecommendationsMock())
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Error calling mock API");

        // Verify: WebClientが呼び出されたことの確認
        verify(webClient, times(1)).get();
    }

    /**
     * モックモードが有効で、モックAPIのURLが有効な場合、モックAPIが呼び出されることを確認する。
     */
    @Test
    void getRecommendations_mockModeEnabledAndMockApiUrlValid_shouldCallMockApi() throws SpotifyWebApiException {
        // Arrange: モックモードを有効にし、モックAPI URLとWebClientを設定
        ReflectionTestUtils.setField(spotifyRecommendationService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyRecommendationService, "mockApiUrl", "http://mock-api-url");
        ReflectionTestUtils.setField(spotifyRecommendationService, "webClient", webClient);

        List<String> seedArtists = List.of("artist1");
        Map<String, Float> maxAudioFeatures = Collections.emptyMap();
        Map<String, Float> minAudioFeatures = Collections.emptyMap();

        Track track1 = new Track.Builder().setName("Mock Track 1").build();
        Track track2 = new Track.Builder().setName("Mock Track 2").build();
        List<Track> mockTracks = List.of(track1, track2);

        // WebClientのモック設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq("http://mock-api-url/recommendations"))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(mockTracks));

        // Act: テスト対象メソッドの実行
        List<Track> result = spotifyRecommendationService.getRecommendations(seedArtists, maxAudioFeatures, minAudioFeatures);

        // Assert: 結果の検証
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Mock Track 1");
        assertThat(result.get(1).getName()).isEqualTo("Mock Track 2");

        verify(webClient, times(1)).get(); // WebClientが呼び出されたことを確認
        verify(spotifyApi, never()).getRecommendations(); // 実APIが呼び出されていないことを確認
        verifyNoInteractions(audioFeatureSetter); // AudioFeatureSetterが呼び出されていないことを確認
    }
}
