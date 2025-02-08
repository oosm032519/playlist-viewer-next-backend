package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.artists.GetSeveralArtistsRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyArtistServiceTest {

    @Mock
    SpotifyApi spotifyApi;

    @Mock
    WebClient.Builder webClientBuilder;

    @Mock
    WebClient webClient;

    @Mock
    WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    WebClient.ResponseSpec responseSpec;

    SpotifyArtistService artistService;


    GetSeveralArtistsRequest getSeveralArtistsRequest;
    Artist artist;

    /**
     * アーティストIDのリストが与えられた場合、各アーティストのジャンルリストが正しく取得されることを確認する。
     */
    @Test
    void getArtistGenres_shouldReturnGenres_whenArtistHasGenres() throws SpotifyWebApiException, IOException, ParseException {
        // Arrange: モックの設定
        when(webClientBuilder.build()).thenReturn(webClient);
        artistService = new SpotifyArtistService(spotifyApi, webClientBuilder);
        ReflectionTestUtils.setField(artistService, "mockEnabled", false);

        String artistId = "test-artist-id";
        String[] genres = {"pop", "rock"};
        artist = mock(Artist.class);
        getSeveralArtistsRequest = mock(GetSeveralArtistsRequest.class);

        GetSeveralArtistsRequest.Builder builder = mock(GetSeveralArtistsRequest.Builder.class);
        when(spotifyApi.getSeveralArtists(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(getSeveralArtistsRequest);
        when(getSeveralArtistsRequest.execute()).thenReturn(new Artist[]{artist});

        when(artist.getGenres()).thenReturn(genres);
        when(artist.getId()).thenReturn(artistId);

        // Act: テスト対象メソッドの実行
        Map<String, List<String>> result = artistService.getArtistGenres(Collections.singletonList(artistId));

        // Assert: 結果の検証
        assertThat(result.get(artistId)).containsExactly("pop", "rock");
    }

    /**
     * 存在しないアーティストIDが与えられた場合、SpotifyWebApiExceptionがスローされることを確認する。
     */
    @Test
    void getArtistGenres_shouldThrowException_whenArtistNotFound() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange: モックの設定
        when(webClientBuilder.build()).thenReturn(webClient);
        artistService = new SpotifyArtistService(spotifyApi, webClientBuilder);
        ReflectionTestUtils.setField(artistService, "mockEnabled", false);

        String artistId = "non-existent-artist-id";
        getSeveralArtistsRequest = mock(GetSeveralArtistsRequest.class);

        GetSeveralArtistsRequest.Builder builder = mock(GetSeveralArtistsRequest.Builder.class);
        when(spotifyApi.getSeveralArtists(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(getSeveralArtistsRequest);
        when(getSeveralArtistsRequest.execute()).thenThrow(new SpotifyWebApiException("Artist not found"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> artistService.getArtistGenres(Collections.singletonList(artistId)))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessageContaining("Artist not found");
    }

    /**
     * アーティストが複数のジャンルを持つ場合、すべてのジャンルが返されることを確認する。
     */
    @Test
    void getArtistGenres_shouldReturnAllGenres_whenArtistHasMultipleGenres() throws SpotifyWebApiException, IOException, ParseException {
        // Arrange: モックの設定
        when(webClientBuilder.build()).thenReturn(webClient);
        artistService = new SpotifyArtistService(spotifyApi, webClientBuilder);
        ReflectionTestUtils.setField(artistService, "mockEnabled", false);

        String artistId = "test-artist-id";
        String[] genres = {"pop", "rock", "indie", "alternative"};
        artist = mock(Artist.class);
        getSeveralArtistsRequest = mock(GetSeveralArtistsRequest.class);

        GetSeveralArtistsRequest.Builder builder = mock(GetSeveralArtistsRequest.Builder.class);
        when(spotifyApi.getSeveralArtists(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(getSeveralArtistsRequest);
        when(getSeveralArtistsRequest.execute()).thenReturn(new Artist[]{artist});

        when(artist.getGenres()).thenReturn(genres);
        when(artist.getId()).thenReturn(artistId);

        // Act: テスト対象メソッドの実行
        Map<String, List<String>> result = artistService.getArtistGenres(Collections.singletonList(artistId));

        // Assert: 結果の検証
        assertThat(result.get(artistId)).containsExactly("pop", "rock", "indie", "alternative");
    }

    /**
     * モックモードが有効で、モックAPIのURLが設定されている場合、モックAPIからアーティストのジャンルが取得されることを確認する。
     */
    @Test
    void getArtistGenres_shouldCallMockApi_whenMockEnabledAndMockApiUrlSet() throws SpotifyWebApiException {
        // Arrange: モックの設定
        when(webClientBuilder.build()).thenReturn(webClient);
        artistService = new SpotifyArtistService(spotifyApi, webClientBuilder);
        ReflectionTestUtils.setField(artistService, "mockEnabled", true);
        ReflectionTestUtils.setField(artistService, "mockApiUrl", "http://mock-api-url");

        String mockApiUrl = "http://mock-api-url"; // mockApiUrl をローカル変数として定義

        String artistId = "test-artist-id";
        List<String> artistIds = Collections.singletonList(artistId);

        Map<String, List<String>> expectedGenres = Collections.singletonMap(artistId, List.of("mock-genre"));

        // WebClient のモック設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(mockApiUrl + "/artists/genres"), any(Function.class))).thenReturn(requestHeadersSpec);
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedGenres));

        // Act: テスト対象メソッドの実行
        Map<String, List<String>> actualGenres = artistService.getArtistGenres(artistIds);

        // Assert: 結果の検証
        assertThat(actualGenres).isEqualTo(expectedGenres);
    }

    /**
     * モックAPIがジャンルを返す場合、モックAPIからアーティストのジャンルが取得されることを確認する。
     */
    @Test
    void getArtistGenresMock_shouldReturnGenres_whenMockApiReturnsGenres() {
        // Arrange: モックの設定
        when(webClientBuilder.build()).thenReturn(webClient);
        artistService = new SpotifyArtistService(spotifyApi, webClientBuilder);
        ReflectionTestUtils.setField(artistService, "mockApiUrl", "http://mock-api-url");

        String mockApiUrl = "http://mock-api-url"; // mockApiUrl をローカル変数として定義

        String artistId = "test-artist-id";
        List<String> artistIds = Collections.singletonList(artistId);

        Map<String, List<String>> expectedGenres = Collections.singletonMap(artistId, List.of("mock-genre"));

        // WebClient のモック設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(mockApiUrl + "/artists/genres"), any(Function.class))).thenReturn(requestHeadersSpec); // 修正点: eq() と any(Function.class)
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class))).thenReturn(Mono.just(expectedGenres));

        // Act: テスト対象メソッドの実行
        Map<String, List<String>> actualGenres = artistService.getArtistGenresMock(artistIds);

        // Assert: 結果の検証
        assertThat(actualGenres).isEqualTo(expectedGenres);
    }

    /**
     * モックAPI呼び出しが失敗した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void getArtistGenresMock_shouldThrowException_whenMockApiCallFails() {
        // Arrange: モックの設定
        when(webClientBuilder.build()).thenReturn(webClient);
        artistService = new SpotifyArtistService(spotifyApi, webClientBuilder);
        ReflectionTestUtils.setField(artistService, "mockApiUrl", "http://mock-api-url");

        String mockApiUrl = "http://mock-api-url"; // mockApiUrl をローカル変数として定義

        String artistId = "test-artist-id";
        List<String> artistIds = Collections.singletonList(artistId);

        // WebClient のモック設定
        when(webClient.get()).thenReturn(requestHeadersUriSpec);
        when(requestHeadersUriSpec.uri(eq(mockApiUrl + "/artists/genres"), any(Function.class))).thenReturn(requestHeadersSpec); // 修正点: eq() と any(Function.class)
        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(any(ParameterizedTypeReference.class)))
                .thenReturn(Mono.error(new WebClientResponseException("Mock API Error", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", null, null, null)));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> artistService.getArtistGenresMock(artistIds))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Error calling mock API");
    }

    /**
     * 実際のSpotify APIがジャンルを返す場合、アーティストのジャンルが正しく取得されることを確認する。
     */
    @Test
    void getArtistGenresReal_shouldReturnGenres_whenArtistHasGenres() throws Exception {
        // Arrange: モックの設定
        when(webClientBuilder.build()).thenReturn(webClient);
        artistService = new SpotifyArtistService(spotifyApi, webClientBuilder);

        String artistId = "test-artist-id";
        String[] genres = {"pop", "rock"};
        artist = mock(Artist.class);
        getSeveralArtistsRequest = mock(GetSeveralArtistsRequest.class);

        GetSeveralArtistsRequest.Builder builder = mock(GetSeveralArtistsRequest.Builder.class);
        when(spotifyApi.getSeveralArtists(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(getSeveralArtistsRequest);
        when(getSeveralArtistsRequest.execute()).thenReturn(new Artist[]{artist});

        when(artist.getGenres()).thenReturn(genres);
        when(artist.getId()).thenReturn(artistId);

        // Act: テスト対象メソッドの実行
        Map<String, List<String>> result = artistService.getArtistGenresReal(Collections.singletonList(artistId));

        // Assert: 結果の検証
        assertThat(result.get(artistId)).containsExactly("pop", "rock");
    }

    /**
     * 実際のSpotify API呼び出しが失敗した場合、SpotifyWebApiExceptionがスローされることを確認する。
     */
    @Test
    void getArtistGenresReal_shouldThrowException_whenSpotifyApiCallFails() throws Exception {
        // Arrange: モックの設定
        when(webClientBuilder.build()).thenReturn(webClient);
        artistService = new SpotifyArtistService(spotifyApi, webClientBuilder);
        String artistId = "non-existent-artist-id";
        getSeveralArtistsRequest = mock(GetSeveralArtistsRequest.class);

        GetSeveralArtistsRequest.Builder builder = mock(GetSeveralArtistsRequest.Builder.class);
        when(spotifyApi.getSeveralArtists(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(getSeveralArtistsRequest);
        when(getSeveralArtistsRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API Error"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> artistService.getArtistGenresReal(Collections.singletonList(artistId)))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessageContaining("Spotify API Error");
    }

    /**
     * 有効なアーティストIDが提供された場合、アーティスト情報が返されることを確認する。
     */
    @Test
    void getArtists_shouldReturnArtists_whenValidArtistIdsProvided() throws Exception {
        // Arrange: モックの設定
        when(webClientBuilder.build()).thenReturn(webClient);
        artistService = new SpotifyArtistService(spotifyApi, webClientBuilder);
        String artistId = "test-artist-id";
        Artist expectedArtist = new Artist.Builder().setId(artistId).setName("Test Artist").build();
        getSeveralArtistsRequest = mock(GetSeveralArtistsRequest.class);

        GetSeveralArtistsRequest.Builder builder = mock(GetSeveralArtistsRequest.Builder.class);
        when(spotifyApi.getSeveralArtists(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(getSeveralArtistsRequest);
        when(getSeveralArtistsRequest.execute()).thenReturn(new Artist[]{expectedArtist});

        // Act: テスト対象メソッドの実行
        Artist[] actualArtists = artistService.getArtists(Collections.singletonList(artistId));

        // Assert: 結果の検証
        assertThat(actualArtists).containsExactly(expectedArtist);
    }

    /**
     * Spotify API呼び出しが失敗した場合、例外がスローされることを確認する。
     */
    @Test
    void getArtists_shouldThrowException_whenSpotifyApiCallFails() throws Exception {
        // Arrange: モックの設定
        when(webClientBuilder.build()).thenReturn(webClient);
        artistService = new SpotifyArtistService(spotifyApi, webClientBuilder);
        String artistId = "invalid-artist-id";
        getSeveralArtistsRequest = mock(GetSeveralArtistsRequest.class);

        GetSeveralArtistsRequest.Builder builder = mock(GetSeveralArtistsRequest.Builder.class);
        when(spotifyApi.getSeveralArtists(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(getSeveralArtistsRequest);
        when(getSeveralArtistsRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API Error"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> artistService.getArtists(Collections.singletonList(artistId)))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessageContaining("Spotify API Error");
    }
}
