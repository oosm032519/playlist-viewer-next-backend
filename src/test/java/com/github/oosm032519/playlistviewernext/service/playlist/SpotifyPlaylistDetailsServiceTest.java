package com.github.oosm032519.playlistviewernext.service.playlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
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
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // MockitoExtension を追加
class SpotifyPlaylistDetailsServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private WebClient.Builder webClientBuilder; // WebClient.Builder をモック

    @Mock
    private WebClient webClient; // WebClient をモック

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private GetPlaylistRequest.Builder getPlaylistRequestBuilder;

    @Mock
    private GetPlaylistRequest getPlaylistRequest;

    @Mock
    private GetPlaylistsItemsRequest.Builder getPlaylistsItemsRequestBuilder;

    @Mock
    private GetPlaylistsItemsRequest getPlaylistsItemsRequest;

    private SpotifyPlaylistDetailsService spotifyPlaylistDetailsService;

    @BeforeEach
    void setUp() {
        // WebClient.Builder のモックの振る舞いを設定
        when(webClientBuilder.build()).thenReturn(webClient);
        spotifyPlaylistDetailsService = new SpotifyPlaylistDetailsService(spotifyApi, webClientBuilder, objectMapper);
        // モックモードを無効に設定
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockEnabled", false);
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockApiUrl", "");
    }

    /**
     * モックモードが有効で、モックAPIのURLがnullの場合に、実APIが呼び出されることを確認する。
     */
    @Test
    void getPlaylistTracks_mockModeEnabledAndMockApiUrlNull_shouldCallRealApi() throws Exception {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";

        // モックモードを有効にするが、mockApiUrlはnullに設定
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockApiUrl", null);

        // getPlaylist のモック設定 (fields パラメータを指定)
        when(spotifyApi.getPlaylist(playlistId)).thenReturn(getPlaylistRequestBuilder);
        when(getPlaylistRequestBuilder.build()).thenReturn(getPlaylistRequest);

        // Paging<PlaylistTrack> のモックを Builder を使って作成
        Paging<PlaylistTrack> playlistTracksPaging = new Paging.Builder<PlaylistTrack>()
                .setItems(new PlaylistTrack[0]) // 空のトラックリスト
                .setTotal(0) // トラック数 0
                .build();

        // Playlist のモックを作成し、tracks に Paging<PlaylistTrack> のモックを設定
        Playlist playlist = new Playlist.Builder().setTracks(playlistTracksPaging).build();
        when(getPlaylistRequest.execute()).thenReturn(playlist);

        // Act: テスト対象メソッドの実行
        PlaylistTrack[] actualTracks = spotifyPlaylistDetailsService.getPlaylistTracks(playlistId);

        // Assert: 結果の検証
        assertThat(actualTracks).isEmpty(); // 期待される結果は空の配列

        // getPlaylist メソッドが呼び出されたことを確認
        verify(spotifyApi).getPlaylist(playlistId);
    }

    /**
     * モックモードが有効な場合、モックAPIからトラック情報を取得することを確認する。
     */
    @Test
    void getPlaylistTracks_mockMode_returnsMockedTracks() throws Exception {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";
        List<Map<String, Object>> mockTracks = Collections.singletonList(Map.of("id", "track1"));

        // モックモードを有効にする (コンストラクタ呼び出しより前に設定)
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockApiUrl", "http://localhost:8081");

        // WebClient のモック設定 (bodyToMono の戻り値を設定)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(mockTracks));

        // ObjectMapper のモック設定 (convertValue の戻り値を設定)
        Track mockTrack = new Track.Builder().setId("track1").build();
        when(objectMapper.convertValue(any(Map.class), eq(Track.class))).thenReturn(mockTrack);

        // Act: テスト対象メソッドの実行
        PlaylistTrack[] result = spotifyPlaylistDetailsService.getPlaylistTracks(playlistId);

        // Assert: 結果の検証
        assertThat(result).hasSize(1);
        assertThat(result[0].getTrack().getId()).isEqualTo("track1");

        // WebClient のメソッドが正しく呼び出されたことを確認
        verify(webClient, times(1)).get(); // WebClient の get() が呼ばれる
    }

    /**
     * モックモードが有効な場合、WebClientResponseExceptionが発生したときにInternalServerExceptionがスローされることを確認する。
     */
    @Test
    void getPlaylistTracks_mockMode_WebClientResponseException() {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";

        // モックモードを有効にする (コンストラクタ呼び出しより前に設定)
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockApiUrl", "http://localhost:8081");

        // WebClient のモック設定 (WebClientResponseException をスロー)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new WebClientResponseException("Mock API Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)));

        // Act & Assert: InternalServerException がスローされることの確認
        assertThatThrownBy(() -> spotifyPlaylistDetailsService.getPlaylistTracks(playlistId))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Error calling mock API");

        // WebClient のメソッドが正しく呼び出されたことを確認
        verify(webClient, times(1)).get();
    }

    /**
     * モックモードが有効な場合、WebClientRequestExceptionが発生したときに、その例外がそのままスローされることを確認する。
     */
    @Test
    void getPlaylistTracks_mockMode_WebClientRequestException() {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";

        // モックモードを有効にする (コンストラクタ呼び出しより前に設定)
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockApiUrl", "http://localhost:8081");

        // WebClient のモック設定 (WebClientRequestException をスロー)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new WebClientRequestException(new RuntimeException("Connection refused"), HttpMethod.GET, null, new HttpHeaders())));

        // Act & Assert: WebClientRequestException がそのままスローされることの確認
        assertThatThrownBy(() -> spotifyPlaylistDetailsService.getPlaylistTracks(playlistId))
                .isInstanceOf(WebClientRequestException.class);

        // WebClient のメソッドが正しく呼び出されたことを確認
        verify(webClient, times(1)).get();
    }

    /**
     * モックモードが有効な場合、getPlaylistでWebClientResponseExceptionが発生したときにInternalServerExceptionがスローされることを確認する。
     */
    @Test
    void getPlaylist_mockMode_WebClientResponseException() {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";

        // モックモードを有効にする
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockApiUrl", "http://localhost:8081");

        // WebClient のモック設定 (WebClientResponseException をスロー)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new WebClientResponseException("Mock API Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)));

        // Act & Assert: InternalServerException がスローされることの確認
        assertThatThrownBy(() -> spotifyPlaylistDetailsService.getPlaylist(playlistId))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Error calling mock API");

        // WebClient のメソッドが正しく呼び出されたことを確認
        verify(webClient, times(1)).get();
    }

    /**
     * モックモードが有効な場合、getPlaylistでWebClientRequestExceptionが発生したときに、その例外がそのままスローされることを確認する。
     */
    @Test
    void getPlaylist_mockMode_WebClientRequestException() {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";

        // モックモードを有効にする
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockApiUrl", "http://localhost:8081");

        // WebClient のモック設定 (WebClientRequestException をスロー)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class))
                .thenReturn(Mono.error(new WebClientRequestException(new RuntimeException("Connection refused"), HttpMethod.GET, null, new HttpHeaders())));

        // Act & Assert: WebClientRequestException がそのままスローされることの確認
        assertThatThrownBy(() -> spotifyPlaylistDetailsService.getPlaylist(playlistId))
                .isInstanceOf(WebClientRequestException.class);

        // WebClient のメソッドが正しく呼び出されたことを確認
        verify(webClient, times(1)).get();
    }

    /**
     * 実APIモードで、トラック情報取得時にIOExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void getPlaylistTracksReal_shouldThrowInternalServerException_whenOtherExceptionOccurs() {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";

        // Spotify API のモック設定 (IOException を RuntimeException でラップしてスロー)
        when(spotifyApi.getPlaylist(playlistId)).thenThrow(new RuntimeException(new IOException("IO error")));

        // Act & Assert: InternalServerException がスローされることの確認
        assertThatThrownBy(() -> spotifyPlaylistDetailsService.getPlaylistTracksReal(playlistId))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("トラック情報の取得中にエラーが発生しました。")
                .hasRootCauseInstanceOf(IOException.class); // hasRootCauseInstanceOf を使用

        verify(spotifyApi).getPlaylist(playlistId);
    }

    /**
     * モックモードで、getPlaylistMockがnullの所有者情報を持つプレイリストを返すことを確認する。
     */
    @Test
    void getPlaylistMock_shouldReturnPlaylist_whenOwnerIsNull() {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";
        // モックのレスポンスデータ (owner が null)
        Map<String, Object> mockApiResponse = new HashMap<>();
        mockApiResponse.put("id", playlistId);
        mockApiResponse.put("playlistName", "Test Playlist");
        mockApiResponse.put("owner", null); // owner を null に設定

        // WebClient のモック設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockApiResponse));

        // モックモードを有効にする
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockApiUrl", "http://localhost:8081");

        // Act: テスト対象メソッドの実行
        Playlist result = spotifyPlaylistDetailsService.getPlaylistMock(playlistId);

        // Assert: 結果の検証
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(playlistId);
        assertThat(result.getName()).isEqualTo("Test Playlist");
        assertThat(result.getOwner()).isNull(); // owner が null であることを確認

        // WebClient のメソッドが正しく呼び出されたことを確認
        verify(webClient, times(1)).get();
    }

    /**
     * モックモードで、getPlaylistTracksがnullを返すことを確認する。
     */
    @Test
    void getPlaylistTracks_mockMode_returnsNull() throws Exception {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";

        // モックモードを有効にする (コンストラクタ呼び出しより前に設定)
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockApiUrl", "http://localhost:8081");

        // WebClient のモック設定 (bodyToMono の戻り値を null に設定)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.empty()); // null の代わりに empty()

        // Act: テスト対象メソッドの実行
        PlaylistTrack[] result = spotifyPlaylistDetailsService.getPlaylistTracks(playlistId);

        // Assert: 結果の検証
        assertThat(result).isNotNull();
        assertThat(result.length).isEqualTo(0);

        // WebClient のメソッドが正しく呼び出されたことを確認
        verify(webClient, times(1)).get(); // WebClient の get() が呼ばれる
    }

    /**
     * モックモードで、getPlaylistMockがnullでない所有者情報を持つプレイリストを返すことを確認する。
     */
    @Test
    void getPlaylistMock_shouldReturnPlaylist_whenOwnerIsNotNull() {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";
        // モックのレスポンスデータ (owner が null でない)
        Map<String, Object> mockApiResponse = new HashMap<>();
        mockApiResponse.put("id", playlistId);
        mockApiResponse.put("playlistName", "Test Playlist");
        Map<String, Object> ownerMap = new HashMap<>();
        ownerMap.put("id", "ownerId");
        ownerMap.put("displayName", "Owner Name");
        mockApiResponse.put("owner", ownerMap);

        // WebClient のモック設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString(), anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(Map.class)).thenReturn(Mono.just(mockApiResponse));

        // モックモードを有効にする
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyPlaylistDetailsService, "mockApiUrl", "http://localhost:8081");

        // Act: テスト対象メソッドの実行
        Playlist result = spotifyPlaylistDetailsService.getPlaylistMock(playlistId);

        // Assert: 結果の検証
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(playlistId);
        assertThat(result.getName()).isEqualTo("Test Playlist");
        assertThat(result.getOwner()).isNotNull();
        assertThat(result.getOwner().getId()).isEqualTo("ownerId");
        assertThat(result.getOwner().getDisplayName()).isEqualTo("Owner Name");

        // WebClient のメソッドが正しく呼び出されたことを確認
        verify(webClient, times(1)).get();
    }

    /**
     * 実APIモードで、プレイリストのトラック数が100を超える場合に、すべてのトラックが取得されることを確認する。
     */
    @Test
    void getPlaylistTracksReal_shouldReturnAllTracks_whenPlaylistHasMoreThan100Tracks() throws Exception {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";

        // 最初の 100 件のトラックのモック
        PlaylistTrack[] firstPageTracks = new PlaylistTrack[100];
        for (int i = 0; i < 100; i++) {
            Track track = new Track.Builder().setId("track" + i).setDurationMs(200000).build();
            firstPageTracks[i] = new PlaylistTrack.Builder().setTrack(track).build();
        }
        Paging<PlaylistTrack> firstPage = new Paging.Builder<PlaylistTrack>().setItems(firstPageTracks).setTotal(200).setOffset(0).setLimit(100).build();

        // 次の 50 件のトラックのモック
        PlaylistTrack[] secondPageTracks = new PlaylistTrack[50];
        for (int i = 0; i < 50; i++) {
            Track track = new Track.Builder().setId("track" + (100 + i)).setDurationMs(200000).build();
            secondPageTracks[i] = new PlaylistTrack.Builder().setTrack(track).build();
        }
        Paging<PlaylistTrack> secondPage = new Paging.Builder<PlaylistTrack>().setItems(secondPageTracks).setTotal(200).setOffset(100).setLimit(100).build();

        // Playlist のモック (getTracks() メソッドが firstPage を返すように設定)
        Playlist playlist = mock(Playlist.class);
        when(playlist.getTracks()).thenReturn(firstPage);

        // getPlaylist のモック設定
        when(spotifyApi.getPlaylist(playlistId)).thenReturn(getPlaylistRequestBuilder);
        when(getPlaylistRequestBuilder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenReturn(playlist);

        // getPlaylistsItems のモック設定 (2 ページ分のデータを返す)
        when(spotifyApi.getPlaylistsItems(playlistId)).thenReturn(getPlaylistsItemsRequestBuilder);
        when(getPlaylistsItemsRequestBuilder.limit(anyInt())).thenReturn(getPlaylistsItemsRequestBuilder);
        when(getPlaylistsItemsRequestBuilder.offset(anyInt())).thenReturn(getPlaylistsItemsRequestBuilder);
        when(getPlaylistsItemsRequestBuilder.build()).thenReturn(getPlaylistsItemsRequest);
        when(getPlaylistsItemsRequest.execute()).thenReturn(firstPage, secondPage); // 2 回呼び出されるように設定

        // Act: テスト対象メソッドの実行
        PlaylistTrack[] result = spotifyPlaylistDetailsService.getPlaylistTracksReal(playlistId);

        // Assert: 結果の検証
        assertThat(result).hasSize(200); // 合計 200 曲取得できるはず
        verify(spotifyApi, times(1)).getPlaylist(playlistId); // getPlaylist は 1 回だけ呼ばれる
        verify(spotifyApi, times(1)).getPlaylistsItems(playlistId); // getPlaylistsItems は 1 回呼ばれる
        verify(getPlaylistsItemsRequest, times(1)).execute(); // execute は 1 回呼ばれる (2 ページ分)
    }

    /**
     * 実APIモードで、Spotify APIからResourceNotFoundExceptionが発生した場合、
     * それがResourceNotFoundExceptionとして再スローされることを確認する。
     */
    @Test
    void getPlaylistTracksReal_shouldThrowResourceNotFoundException_whenPlaylistNotFound() throws Exception {
        // Arrange:
        String playlistId = "nonExistentPlaylistId";

        // Spotify API のモック設定 (SpotifyWebApiException をスロー)
        when(spotifyApi.getPlaylist(playlistId)).thenReturn(getPlaylistRequestBuilder);
        when(getPlaylistRequestBuilder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Not Found", new Throwable("Not Found")));

        // Act & Assert:
        assertThatThrownBy(() -> spotifyPlaylistDetailsService.getPlaylistTracksReal(playlistId))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessageContaining("Not Found");

        verify(spotifyApi).getPlaylist(playlistId);
    }
}
