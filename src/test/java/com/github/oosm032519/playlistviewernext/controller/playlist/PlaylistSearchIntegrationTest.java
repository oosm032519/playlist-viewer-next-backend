package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * PlaylistSearchController の統合テストクラス
 * 実際の Spotify API との通信を含めて、コントローラーの動作を検証する
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PlaylistSearchIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SpotifyPlaylistSearchService playlistSearchService;

    /**
     * 有効な検索クエリを使用してプレイリストを検索し、
     * Spotify API から結果が正常に返されることを検証するテストケース
     *
     * @throws Exception テスト中に例外が発生した場合
     */
    @Test
    void givenValidQuery_whenSearchPlaylists_thenReturnsPlaylistsSuccessfully() throws Exception {
        // Arrange: 検索クエリとモックの検索結果を設定
        String query = "classical music";
        PlaylistSimplified[] mockPlaylists = {mock(PlaylistSimplified.class)};
        when(playlistSearchService.searchPlaylists(query, 0, 20)).thenReturn(Arrays.asList(mockPlaylists));

        // Act: プレイリスト検索リクエストを実行
        mockMvc.perform(get("/api/playlists/search")
                        .param("query", query)
                        .contentType(MediaType.APPLICATION_JSON))
                // Assert: ステータスコードが200 OKであることを検証
                .andExpect(status().isOk())
                // Assert: レスポンスボディが空の配列ではないことを検証
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isNotEmpty());

        // Verify that playlistSearchService.searchPlaylists was called
        verify(playlistSearchService, times(1)).searchPlaylists(query, 0, 20);
    }

    /**
     * 無効な検索クエリ（空文字列）を使用してプレイリストを検索し、
     * Spotify API からエラーが返されることを検証するテストケース
     *
     * @throws Exception テスト中に例外が発生した場合
     */
    @Test
    void givenInvalidQuery_whenSearchPlaylists_thenReturnsError() throws Exception {
        // Arrange: 無効な検索クエリを設定
        String query = "";

        // Act: プレイリスト検索リクエストを実行
        mockMvc.perform(get("/api/playlists/search")
                        .param("query", query)
                        .contentType(MediaType.APPLICATION_JSON))
                // Assert: ステータスコードが400 Bad Requestであることを検証
                .andExpect(status().isBadRequest())
                // Assert: エラーレスポンスの検証
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.name()))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("入力値が不正です。"));
    }

    /**
     * Spotify API のレート制限を超過した場合に、
     * エラーメッセージが適切に返されることを検証するテストケース
     *
     * @throws Exception テスト中に例外が発生した場合
     */
    @Test
    void givenRateLimitExceeded_whenSearchPlaylists_thenReturnsRateLimitError() throws Exception {
        // Arrange: 検索クエリを設定
        String query = "popular music";

        // Spotify API のモックを設定して、レート制限を超過したエラーを返すようにする
        SpotifyApiException rateLimitException = new SpotifyApiException(
                HttpStatus.TOO_MANY_REQUESTS,
                "SPOTIFY_API_RATE_LIMIT_EXCEEDED", // エラーコードを設定
                "Rate limit exceeded",
                "Error details"
        );
        // 4回例外をスローするように設定
        when(playlistSearchService.searchPlaylists(anyString(), anyInt(), anyInt()))
                .thenThrow(rateLimitException)
                .thenThrow(rateLimitException)
                .thenThrow(rateLimitException)
                .thenThrow(rateLimitException);

        // Act: プレイリスト検索リクエストを実行
        mockMvc.perform(get("/api/playlists/search")
                        .param("query", query)
                        .contentType(MediaType.APPLICATION_JSON))
                // Assert: ステータスコードが429 Too Many Requestsであることを検証
                .andExpect(status().isTooManyRequests())
                // Assert: エラーレスポンスの検証
                .andExpect(jsonPath("$").isMap())
                .andExpect(jsonPath("$.status").value(HttpStatus.TOO_MANY_REQUESTS.name()))
                .andExpect(jsonPath("$.errorCode").value("SPOTIFY_API_RATE_LIMIT_EXCEEDED"))
                .andExpect(jsonPath("$.message").value("Spotify API のレート制限を超過しました。しばらく時間をおいてから再度お試しください。"));

        // Verify that playlistSearchService.searchPlaylists was called 4 times
        verify(playlistSearchService, times(4)).searchPlaylists(anyString(), anyInt(), anyInt());
    }
}
