package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class SpotifyUserPlaylistsServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private OAuth2User oauth2User;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private GetListOfCurrentUsersPlaylistsRequest.Builder requestBuilder;

    @Mock
    private GetListOfCurrentUsersPlaylistsRequest request;

    @Mock
    private WebClient.Builder webClientBuilder; // WebClient.Builder をモック

    @Mock
    private WebClient webClient; // WebClient をモック

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private SpotifyUserPlaylistsService spotifyUserPlaylistsService;

    @BeforeEach
    public void setup() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        SecurityContextHolder.setContext(securityContext);

        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(requestBuilder);
        when(requestBuilder.limit(anyInt())).thenReturn(requestBuilder); // limit()のモックを追加
        when(requestBuilder.build()).thenReturn(request);

        // WebClient関連のモック設定
        when(webClientBuilder.build()).thenReturn(webClient); // WebClient.Builder の build() メソッドをモック化
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);

        // モックモードを有効にする
        ReflectionTestUtils.setField(spotifyUserPlaylistsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyUserPlaylistsService, "mockApiUrl", "http://localhost:8081");

        // SpotifyUserPlaylistsService のインスタンスを再生成 (WebClient.Builder のモックを注入)
        spotifyUserPlaylistsService = new SpotifyUserPlaylistsService(spotifyApi, webClientBuilder);
        ReflectionTestUtils.setField(spotifyUserPlaylistsService, "mockEnabled", true);
        ReflectionTestUtils.setField(spotifyUserPlaylistsService, "mockApiUrl", "http://localhost:8081");
    }

    /**
     * 現在のユーザーのプレイリスト一覧が正常に取得できることを確認する。
     */
    @Test
    public void getCurrentUsersPlaylists_success() throws Exception {
        // Arrange: モックのプレイリストデータを作成
        PlaylistSimplified playlistSimplified = new PlaylistSimplified.Builder().setId("1").build();
        List<PlaylistSimplified> mockPlaylists = List.of(playlistSimplified);

        // getCurrentUsersPlaylistsMock メソッドをモック化し、モックのプレイリストデータを返すように設定
        SpotifyUserPlaylistsService spyService = Mockito.spy(spotifyUserPlaylistsService);
        doReturn(mockPlaylists).when(spyService).getCurrentUsersPlaylistsMock();

        // Act: テスト対象メソッドを呼び出し
        List<PlaylistSimplified> playlists = spyService.getCurrentUsersPlaylists();

        // Assert: 結果を検証
        assertThat(playlists).isNotNull();
        assertThat(playlists).hasSize(1);
        assertThat(playlists.getFirst().getId()).isEqualTo("1");

        // getCurrentUsersPlaylistsMock メソッドが呼び出されたことを確認
        verify(spyService, times(1)).getCurrentUsersPlaylistsMock();

        // 実API関連のメソッドが呼び出されていないことを確認
        verify(spotifyApi, never()).setAccessToken(anyString());
        verify(requestBuilder, never()).limit(anyInt());
        verify(request, never()).execute();
    }

    /**
     * Spotify API呼び出し中にSpotifyWebApiExceptionが発生した場合、例外がそのままスローされることを確認する。
     */
    @Test
    public void getCurrentUsersPlaylists_spotifyApiException() throws Exception {
        // Arrange: モックモードを無効にして実APIを呼び出すように設定
        ReflectionTestUtils.setField(spotifyUserPlaylistsService, "mockEnabled", false);

        String accessToken = "mockAccessToken";
        when(oauth2User.getAttribute("spotify_access_token")).thenReturn(accessToken);
        when(request.execute()).thenThrow(new SpotifyWebApiException("API error"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> spotifyUserPlaylistsService.getCurrentUsersPlaylists())
                .isInstanceOf(SpotifyWebApiException.class);
        verify(spotifyApi).setAccessToken(accessToken);
    }

    /**
     * Spotify API呼び出し中にIOExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    public void getCurrentUsersPlaylists_ioException() throws Exception {
        // Arrange: モックモードを無効にして実APIを呼び出すように設定
        ReflectionTestUtils.setField(spotifyUserPlaylistsService, "mockEnabled", false);

        String accessToken = "mockAccessToken";
        when(oauth2User.getAttribute("spotify_access_token")).thenReturn(accessToken);
        when(request.execute()).thenThrow(new IOException("IO error"));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> spotifyUserPlaylistsService.getCurrentUsersPlaylists())
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Error occurred while retrieving playlists");
        verify(spotifyApi).setAccessToken(accessToken);
    }

    /**
     * 実APIモードで、アクセストークンがない場合にAuthenticationExceptionがスローされることを確認する。
     */
    @Test
    void getCurrentUsersPlaylistsReal_NoAccessToken_ThrowsAuthenticationException() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange:
        // SecurityContextから認証情報を取得するが、OAuth2Userは存在するがアクセストークンがない場合をシミュレート
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("spotify_access_token")).thenReturn(null);

        // Act & Assert: AuthenticationExceptionがスローされることの確認
        assertThatThrownBy(() -> spotifyUserPlaylistsService.getCurrentUsersPlaylistsReal())
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("アクセストークンが見つかりません。");

        // Verify:
        verify(spotifyApi, never()).setAccessToken(anyString());
        verify(requestBuilder, never()).limit(anyInt());
        verify(request, never()).execute();
    }

    /**
     * 実APIモードで、SpotifyWebApiExceptionが発生した場合に、例外がそのままスローされることを確認する。
     */
    @Test
    void getCurrentUsersPlaylistsReal_SpotifyWebApiException_ThrowsSpotifyWebApiException() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange:
        // SecurityContextから認証情報を取得し、OAuth2Userとアクセストークンをモック
        String accessToken = "mockAccessToken";
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("spotify_access_token")).thenReturn(accessToken);

        // Spotify APIのモック設定 (SpotifyWebApiExceptionをスロー)
        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(requestBuilder);
        when(requestBuilder.limit(anyInt())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute()).thenThrow(new SpotifyWebApiException("Spotify API Error"));

        // Act & Assert: SpotifyWebApiExceptionがスローされることの確認
        assertThatThrownBy(() -> spotifyUserPlaylistsService.getCurrentUsersPlaylistsReal())
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessageContaining("Spotify API Error");

        // Verify:
        verify(spotifyApi).setAccessToken(accessToken); // アクセストークンがセットされることを確認
    }

    /**
     * モックAPIがプレイリストを返す場合、それらが正常に返されることを確認する。
     */
    @Test
    void getCurrentUsersPlaylistsMock_shouldReturnPlaylists_whenMockApiReturnsPlaylists() {
        // Arrange:
        // WebClientのモック設定
        PlaylistSimplified playlist1 = new PlaylistSimplified.Builder().setId("1").build();
        PlaylistSimplified playlist2 = new PlaylistSimplified.Builder().setId("2").build();
        List<PlaylistSimplified> expectedPlaylists = Arrays.asList(playlist1, playlist2);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec); // URIをanyString()でモック
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedPlaylists));

        // Act: テスト対象メソッドの実行
        List<PlaylistSimplified> actualPlaylists = spotifyUserPlaylistsService.getCurrentUsersPlaylistsMock();

        // Assert: 結果の検証
        assertThat(actualPlaylists).isEqualTo(expectedPlaylists);
    }

    /**
     * モックAPI呼び出し時にWebClientResponseExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void getCurrentUsersPlaylistsMock_shouldThrowException_whenWebClientThrowsException() {
        // Arrange:
        // WebClientのモック設定 (WebClientResponseExceptionをスロー)
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec); // URIをanyString()でモック
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new WebClientResponseException("Mock API Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> spotifyUserPlaylistsService.getCurrentUsersPlaylistsMock())
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Error calling mock API");
    }

    /**
     * モックモードが無効の場合に、実APIが呼び出され、プレイリスト一覧が取得できることを確認する。
     */
    @Test
    void getCurrentUsersPlaylists_shouldCallRealApi_whenMockDisabled() throws Exception {
        // Arrange:
        ReflectionTestUtils.setField(spotifyUserPlaylistsService, "mockEnabled", false); // モックモードを無効にする

        String accessToken = "mockAccessToken";
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("spotify_access_token")).thenReturn(accessToken);

        PlaylistSimplified playlist1 = new PlaylistSimplified.Builder().setId("1").build();
        PlaylistSimplified playlist2 = new PlaylistSimplified.Builder().setId("2").build();
        List<PlaylistSimplified> expectedPlaylists = Arrays.asList(playlist1, playlist2);

        // Spotify APIのモック設定
        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(requestBuilder);
        when(requestBuilder.limit(anyInt())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        se.michaelthelin.spotify.model_objects.specification.Paging<PlaylistSimplified> paging = new Paging.Builder<PlaylistSimplified>().setItems(expectedPlaylists.toArray(new PlaylistSimplified[0])).build();
        when(request.execute()).thenReturn(paging);

        // Act: テスト対象メソッドの実行
        List<PlaylistSimplified> actualPlaylists = spotifyUserPlaylistsService.getCurrentUsersPlaylists();

        // Assert: 結果の検証
        assertThat(actualPlaylists).hasSize(2);
        assertThat(actualPlaylists.get(0).getId()).isEqualTo("1");
        assertThat(actualPlaylists.get(1).getId()).isEqualTo("2");
        verify(spotifyApi).setAccessToken(accessToken); // アクセストークンがセットされることを確認
    }

    /**
     * 実APIモードで、playlistsPaging.getItems()がnullを返す場合に、空のリストが返されることを確認する。
     */
    @Test
    void getPlaylists_shouldReturnEmptyList_whenPlaylistsPagingItemsIsNull() throws Exception {
        // Arrange:
        ReflectionTestUtils.setField(spotifyUserPlaylistsService, "mockEnabled", false); // モックモードを無効にする
        String accessToken = "mockAccessToken";
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("spotify_access_token")).thenReturn(accessToken);

        // Spotify APIのモック設定 (playlistsPaging.getItems() が null を返すように設定)
        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(requestBuilder);
        when(requestBuilder.limit(anyInt())).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute()).thenReturn(new Paging.Builder<PlaylistSimplified>().build()); // items が null の Paging オブジェクトを返す

        // Act: テスト対象メソッドの実行
        List<PlaylistSimplified> result = spotifyUserPlaylistsService.getCurrentUsersPlaylists(); // getCurrentUsersPlaylists を呼び出す

        // Assert: 結果の検証
        assertThat(result).isEmpty();
    }

    /**
     * モックモードが有効で、モックAPIのURLが設定されている場合、モックAPIが呼び出されることを確認する。
     */
    @Test
    void getCurrentUsersPlaylists_shouldCallMockApi_whenMockEnabledAndMockApiUrlSet() throws Exception {
        // Arrange:
        ReflectionTestUtils.setField(spotifyUserPlaylistsService, "mockEnabled", true); // モックモードを有効にする
        ReflectionTestUtils.setField(spotifyUserPlaylistsService, "mockApiUrl", "http://mock-api-url"); // モックAPIのURLを設定

        // WebClientのモック設定 (モックAPIからのレスポンスをシミュレート)
        PlaylistSimplified playlist1 = new PlaylistSimplified.Builder().setId("1").build();
        PlaylistSimplified playlist2 = new PlaylistSimplified.Builder().setId("2").build();
        List<PlaylistSimplified> expectedPlaylists = Arrays.asList(playlist1, playlist2);

        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(anyString())).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedPlaylists));

        // Act: テスト対象メソッドの実行
        List<PlaylistSimplified> actualPlaylists = spotifyUserPlaylistsService.getCurrentUsersPlaylists();

        // Assert: 結果の検証
        assertThat(actualPlaylists).isEqualTo(expectedPlaylists);
        verify(webClient, times(1)).get(); // WebClientが呼び出されたことを確認
        verify(spotifyApi, never()).setAccessToken(anyString()); // 実APIが呼び出されないことを確認
        verify(requestBuilder, never()).limit(anyInt());
        verify(request, never()).execute();
    }
}
