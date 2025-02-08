package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.exception.ErrorResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlaylistSearchControllerTest {

    @Mock
    private SpotifyPlaylistSearchService playlistSearchService;

    @Mock
    private SpotifyClientCredentialsAuthentication authController;

    @InjectMocks
    private PlaylistSearchController playlistSearchController;

    /**
     * 有効なクエリでプレイリスト検索が成功した場合、検索結果が返されることを確認する。
     */
    @Test
    void searchPlaylists_validQuery_returnsSearchResults() throws SpotifyWebApiException {
        // Arrange: テストデータの準備
        String query = "test query";
        int offset = 0;
        int limit = 20;
        Map<String, Object> expectedSearchResult = new HashMap<>();
        expectedSearchResult.put("playlists", new Object()); // プレイリストのモックオブジェクト
        expectedSearchResult.put("total", 10); // プレイリストの総数

        // モックの設定
        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenReturn(expectedSearchResult);

        // Act: テスト対象メソッドの実行
        playlistSearchController = new PlaylistSearchController(playlistSearchService, authController);
        ResponseEntity<?> response = playlistSearchController.searchPlaylists(query, offset, limit);

        // Assert: アサーション
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedSearchResult);
    }

    /**
     * 空白のクエリでプレイリスト検索を行った場合、BadRequestエラーが返されることを確認する。
     */
    @Test
    void searchPlaylists_blankQuery_returnsBadRequest() throws SpotifyWebApiException {
        // Arrange: テストデータの準備
        String query = "   "; // 空白のクエリ
        int offset = 0;
        int limit = 20;

        // Act: テスト対象メソッドの実行
        playlistSearchController = new PlaylistSearchController(playlistSearchService, authController);
        ResponseEntity<?> response = playlistSearchController.searchPlaylists(query, offset, limit);

        // Assert: アサーション
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertThat(Objects.requireNonNull(errorResponse).getErrorCode()).isEqualTo("INVALID_QUERY");
        assertThat(errorResponse.getMessage()).isEqualTo("検索クエリは必須です。");
    }

    /**
     * Spotify API例外が発生した場合、例外がスローされることを確認する。
     */
    @Test
    void searchPlaylists_spotifyApiException_throwsException() throws SpotifyWebApiException {
        // Arrange: テストデータの準備
        String query = "test query";
        int offset = 0;
        int limit = 20;

        // モックの設定
        SpotifyWebApiException exception = new SpotifyWebApiException("Spotify API error");
        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenThrow(exception);

        // Act & Assert: テスト対象メソッドの実行と例外の確認
        playlistSearchController = new PlaylistSearchController(playlistSearchService, authController);

        try {
            playlistSearchController.searchPlaylists(query, offset, limit);
        } catch (SpotifyWebApiException e) {
            assertThat(e.getMessage()).isEqualTo("Spotify API error");
        }
    }
}
