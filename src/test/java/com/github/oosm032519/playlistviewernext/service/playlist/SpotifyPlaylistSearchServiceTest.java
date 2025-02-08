package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.miscellaneous.PlaylistTracksInformation;
import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistSearchServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private WebClient.Builder webClientBuilder;

    @Mock
    private WebClient webClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private SearchPlaylistsRequest.Builder searchPlaylistsRequestBuilder;

    @Mock
    private SearchPlaylistsRequest searchPlaylistsRequest;

    @Mock
    private Paging<PlaylistSimplified> playlistSimplifiedPaging;

    @InjectMocks
    private SpotifyPlaylistSearchService spotifyPlaylistSearchService;

    /**
     * モックモードが有効で、モックAPIのURLが設定されている場合、モックAPIからプレイリストを検索する。
     */
    @Test
    @DisplayName("モックモードが有効で、モックAPIのURLが設定されている場合、モックAPIからプレイリストを検索する")
    void searchPlaylists_mockModeEnabledAndMockApiUrlSet_shouldSearchPlaylistsUsingMockApi() throws SpotifyWebApiException {
        // Arrange: 事前条件
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockApiUrl", "http://localhost:8081");

        // WebClientのモック設定
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "webClient", webClient);

        // モックAPIのレスポンスを設定
        Map<String, Object> mockApiResponse = new HashMap<>();
        Map<String, Object> playlist = new HashMap<>();
        playlist.put("id", "playlistId");
        playlist.put("name", "playlistName");
        mockApiResponse.put("playlists", List.of(playlist));
        mockApiResponse.put("total", 1);

        // searchPlaylistsMock メソッド内で使用される WebClient のモックの振る舞いを設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockApiResponse));

        // Act: テスト対象メソッドの実行
        Map<String, Object> result = spotifyPlaylistSearchService.searchPlaylists("query", 0, 20);

        // Assert: 結果の検証
        assertThat(result).isEqualTo(mockApiResponse);
        verify(webClient, times(1)).get();
    }

    /**
     * モックモードが無効の場合、実APIからプレイリストを検索する。
     */
    @Test
    @DisplayName("モックモードが無効の場合、実APIからプレイリストを検索する")
    void searchPlaylists_mockModeDisabled_shouldSearchPlaylistsUsingRealApi() throws Exception {
        // Arrange: 事前条件
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockEnabled", false);

        // Spotify APIのモック設定
        when(spotifyApi.searchPlaylists(anyString())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.limit(anyInt())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.offset(anyInt())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenReturn(playlistSimplifiedPaging);
        when(playlistSimplifiedPaging.getItems()).thenReturn(new PlaylistSimplified[]{});
        when(playlistSimplifiedPaging.getTotal()).thenReturn(0);

        // RetryUtilのモック設定
        try (MockedStatic<RetryUtil> retryUtilMockedStatic = mockStatic(RetryUtil.class)) {
            RetryUtil.RetryableOperation<Map<String, Object>> retryableOperation = mock(RetryUtil.RetryableOperation.class);
            retryUtilMockedStatic.when(() -> RetryUtil.executeWithRetry(any(), anyInt(), anyLong()))
                    .thenAnswer(invocation -> {
                        RetryUtil.RetryableOperation<Map<String, Object>> operation = invocation.getArgument(0);
                        return operation.execute();
                    });

            // Act: テスト対象メソッドの実行
            Map<String, Object> result = spotifyPlaylistSearchService.searchPlaylists("query", 0, 20);

            // Assert: 結果の検証
            assertThat(result).isNotNull();
            assertThat(result.get("playlists")).isNotNull();
            assertThat(result.get("total")).isNotNull();
            verify(spotifyApi, times(1)).searchPlaylists(anyString());
            retryUtilMockedStatic.verify(() -> RetryUtil.executeWithRetry(any(), anyInt(), anyLong()), times(1));
        }
    }

    /**
     * モックモードで、モックAPI呼び出し時にWebClientResponseExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    @DisplayName("モックモードで、モックAPI呼び出し時にWebClientResponseExceptionが発生した場合、InternalServerExceptionがスローされる")
    void searchPlaylists_mockModeAndWebClientResponseException_shouldThrowInternalServerException() {
        // Arrange: 事前条件
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockApiUrl", "http://localhost:8081");

        // WebClientのモック設定
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "webClient", webClient);

        // WebClientResponseExceptionをスローするようモックを設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), any(Object[].class))).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.error(new WebClientResponseException("Mock Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> spotifyPlaylistSearchService.searchPlaylists("query", 0, 20))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("Error calling mock API");
    }

    /**
     * 実APIモードで、Spotify API呼び出し時にSpotifyWebApiExceptionが発生した場合、そのままスローされることを確認する。
     */
    @Test
    @DisplayName("実APIモードで、Spotify API呼び出し時にSpotifyWebApiExceptionが発生した場合、そのままスローされる")
    void searchPlaylists_realModeAndSpotifyWebApiException_shouldThrowSpotifyWebApiException() throws Exception {
        // Arrange: 事前条件
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockEnabled", false);

        // SpotifyWebApiExceptionをスローするようモックを設定
        when(spotifyApi.searchPlaylists(anyString())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.limit(anyInt())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.offset(anyInt())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API Error"));

        // RetryUtilのモック設定
        try (MockedStatic<RetryUtil> retryUtilMockedStatic = mockStatic(RetryUtil.class)) {
            RetryUtil.RetryableOperation<Map<String, Object>> retryableOperation = mock(RetryUtil.RetryableOperation.class);
            retryUtilMockedStatic.when(() -> RetryUtil.executeWithRetry(any(), anyInt(), anyLong()))
                    .thenAnswer(invocation -> {
                        RetryUtil.RetryableOperation<Map<String, Object>> operation = invocation.getArgument(0);
                        return operation.execute();
                    });

            // Act & Assert: SpotifyWebApiExceptionがスローされることの確認
            assertThatThrownBy(() -> spotifyPlaylistSearchService.searchPlaylists("query", 0, 20))
                    .isInstanceOf(SpotifyWebApiException.class)
                    .hasMessage("Spotify API Error");
            retryUtilMockedStatic.verify(() -> RetryUtil.executeWithRetry(any(), anyInt(), anyLong()), times(1));
        }
    }

    /**
     * 実APIモードで、その他の例外が発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    @DisplayName("実APIモードで、その他の例外が発生した場合、InternalServerExceptionがスローされる")
    void searchPlaylists_realModeAndOtherException_shouldThrowInternalServerException() throws Exception {
        // Arrange: 事前条件
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockEnabled", false);

        // Spotify APIのモック設定
        when(spotifyApi.searchPlaylists(anyString())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.limit(anyInt())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.offset(anyInt())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenThrow(new RuntimeException("Unexpected Error"));

        // RetryUtilのモック設定
        try (MockedStatic<RetryUtil> retryUtilMockedStatic = mockStatic(RetryUtil.class)) {
            RetryUtil.RetryableOperation<Map<String, Object>> retryableOperation = mock(RetryUtil.RetryableOperation.class);
            retryUtilMockedStatic.when(() -> RetryUtil.executeWithRetry(any(), anyInt(), anyLong()))
                    .thenAnswer(invocation -> {
                        RetryUtil.RetryableOperation<Map<String, Object>> operation = invocation.getArgument(0);
                        return operation.execute();
                    });

            // Act & Assert: InternalServerExceptionがスローされることの確認
            assertThatThrownBy(() -> spotifyPlaylistSearchService.searchPlaylists("query", 0, 20))
                    .isInstanceOf(InternalServerException.class)
                    .hasMessage("Spotifyプレイリストの検索中にエラーが発生しました。");
        }
    }

    /**
     * モックモードが有効だが、モックAPIのURLがnullの場合、実APIからプレイリストを検索することを確認する。
     */
    @Test
    @DisplayName("モックモードが有効だが、モックAPIのURLがnullの場合、実APIからプレイリストを検索する")
    void searchPlaylists_mockModeEnabledAndMockApiUrlNull_shouldSearchPlaylistsUsingRealApi() throws Exception {
        // Arrange: 事前条件
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockApiUrl", null);

        // Spotify APIのモック設定 (searchPlaylists_mockModeDisabled_shouldSearchPlaylistsUsingRealApi と同じ)
        when(spotifyApi.searchPlaylists(anyString())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.limit(anyInt())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.offset(anyInt())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenReturn(playlistSimplifiedPaging);
        when(playlistSimplifiedPaging.getItems()).thenReturn(new PlaylistSimplified[]{});
        when(playlistSimplifiedPaging.getTotal()).thenReturn(0);

        // RetryUtilのモック設定 (searchPlaylists_mockModeDisabled_shouldSearchPlaylistsUsingRealApi と同じ)
        try (MockedStatic<RetryUtil> retryUtilMockedStatic = mockStatic(RetryUtil.class)) {
            RetryUtil.RetryableOperation<Map<String, Object>> retryableOperation = mock(RetryUtil.RetryableOperation.class);
            retryUtilMockedStatic.when(() -> RetryUtil.executeWithRetry(any(), anyInt(), anyLong()))
                    .thenAnswer(invocation -> {
                        RetryUtil.RetryableOperation<Map<String, Object>> operation = invocation.getArgument(0);
                        return operation.execute();
                    });

            // Act: テスト対象メソッドの実行
            Map<String, Object> result = spotifyPlaylistSearchService.searchPlaylists("query", 0, 20);

            // Assert: 結果の検証
            assertThat(result).isNotNull();
            assertThat(result.get("playlists")).isNotNull();
            assertThat(result.get("total")).isNotNull();
            verify(spotifyApi, times(1)).searchPlaylists(anyString());
            retryUtilMockedStatic.verify(() -> RetryUtil.executeWithRetry(any(), anyInt(), anyLong()), times(1));
        }
    }

    /**
     * PlaylistSimplifiedのexternalUrlsがnullの場合、空のexternalUrlsマップを返すことを確認する。
     */
    @Test
    @DisplayName("PlaylistSimplifiedのexternalUrlsがnullの場合、空のexternalUrlsマップを返す")
    void convertToMap_externalUrlsNull_returnsEmptyExternalUrlsMap() {
        // Arrange: テストデータの準備
        PlaylistSimplified playlist = mock(PlaylistSimplified.class);
        when(playlist.getId()).thenReturn("testId");
        when(playlist.getName()).thenReturn("testName");

        // PlaylistTracksInformation のモックを作成
        PlaylistTracksInformation tracksInfo = mock(PlaylistTracksInformation.class);
        when(tracksInfo.getTotal()).thenReturn(10);
        when(playlist.getTracks()).thenReturn(tracksInfo);

        // Imageのモックを作成 (配列で返す)
        Image[] images = new Image[0]; // 空の配列
        when(playlist.getImages()).thenReturn(images);

        when(playlist.getExternalUrls()).thenReturn(null); // externalUrlsをnullに設定
        User owner = new User.Builder().setDisplayName("testOwner").build();
        when(playlist.getOwner()).thenReturn(owner);

        // Act: テスト対象メソッドの実行
        Map<String, Object> result = spotifyPlaylistSearchService.convertToMap(playlist);

        // Assert: 結果の検証
        assertThat(result).isNotNull();
        assertThat(result.get("externalUrls")).isEqualTo(Collections.emptyMap());
    }

    /**
     * モックモードが有効だが、モックAPIのURLが空文字列の場合、実APIからプレイリストを検索することを確認する。
     */
    @Test
    @DisplayName("モックモードが有効だが、モックAPIのURLが空文字列の場合、実APIからプレイリストを検索する")
    void searchPlaylists_mockModeEnabledAndMockApiUrlEmpty_shouldSearchPlaylistsUsingRealApi() throws Exception {
        // Arrange: 事前条件
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistSearchService, "mockApiUrl", ""); // 空文字列に設定

        // Spotify APIのモック設定 (searchPlaylists_mockModeDisabled_shouldSearchPlaylistsUsingRealApi と同じ)
        when(spotifyApi.searchPlaylists(anyString())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.limit(anyInt())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.offset(anyInt())).thenReturn(searchPlaylistsRequestBuilder);
        when(searchPlaylistsRequestBuilder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenReturn(playlistSimplifiedPaging);
        when(playlistSimplifiedPaging.getItems()).thenReturn(new PlaylistSimplified[]{});
        when(playlistSimplifiedPaging.getTotal()).thenReturn(0);

        // RetryUtilのモック設定 (searchPlaylists_mockModeDisabled_shouldSearchPlaylistsUsingRealApi と同じ)
        try (MockedStatic<RetryUtil> retryUtilMockedStatic = mockStatic(RetryUtil.class)) {
            RetryUtil.RetryableOperation<Map<String, Object>> retryableOperation = mock(RetryUtil.RetryableOperation.class);
            retryUtilMockedStatic.when(() -> RetryUtil.executeWithRetry(any(), anyInt(), anyLong()))
                    .thenAnswer(invocation -> {
                        RetryUtil.RetryableOperation<Map<String, Object>> operation = invocation.getArgument(0);
                        return operation.execute();
                    });

            // Act: テスト対象メソッドの実行
            Map<String, Object> result = spotifyPlaylistSearchService.searchPlaylists("query", 0, 20);

            // Assert: 結果の検証
            assertThat(result).isNotNull();
            assertThat(result.get("playlists")).isNotNull();
            assertThat(result.get("total")).isNotNull();
            verify(spotifyApi, times(1)).searchPlaylists(anyString());
            retryUtilMockedStatic.verify(() -> RetryUtil.executeWithRetry(any(), anyInt(), anyLong()), times(1));
        }
    }
}
