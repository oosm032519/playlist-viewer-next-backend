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
import org.springframework.mock.web.MockHttpServletRequest;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.util.HashMap;
import java.util.Map;

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

    @Test
    void searchPlaylists_validQuery_returnsSearchResults() throws SpotifyWebApiException {
        // テストデータの準備
        String query = "test query";
        int offset = 0;
        int limit = 20;
        Map<String, Object> expectedSearchResult = new HashMap<>();
        expectedSearchResult.put("playlists", new Object()); // プレイリストのモックオブジェクト
        expectedSearchResult.put("total", 10); // プレイリストの総数

        // モックの設定
        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenReturn(expectedSearchResult);

        // テスト対象メソッドの実行
        MockHttpServletRequest request = new MockHttpServletRequest();
        playlistSearchController = new PlaylistSearchController(playlistSearchService, authController, request);
        ResponseEntity<?> response = playlistSearchController.searchPlaylists(query, offset, limit);

        // アサーション
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedSearchResult);
    }

    @Test
    void searchPlaylists_blankQuery_returnsBadRequest() throws SpotifyWebApiException {
        // テストデータの準備
        String query = "   ";
        int offset = 0;
        int limit = 20;

        // テスト対象メソッドの実行
        MockHttpServletRequest request = new MockHttpServletRequest();
        playlistSearchController = new PlaylistSearchController(playlistSearchService, authController, request);
        ResponseEntity<?> response = playlistSearchController.searchPlaylists(query, offset, limit);


        // アサーション
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertThat(errorResponse.getErrorCode()).isEqualTo("INVALID_QUERY");
        assertThat(errorResponse.getMessage()).isEqualTo("検索クエリは必須です。");
    }

    @Test
    void searchPlaylists_spotifyApiException_throwsException() throws SpotifyWebApiException {
        // テストデータの準備
        String query = "test query";
        int offset = 0;
        int limit = 20;

        // モックの設定
        SpotifyWebApiException exception = new SpotifyWebApiException("Spotify API error");
        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenThrow(exception);

        // テスト対象メソッドの実行
        MockHttpServletRequest request = new MockHttpServletRequest();
        playlistSearchController = new PlaylistSearchController(playlistSearchService, authController, request);

        try {
            playlistSearchController.searchPlaylists(query, offset, limit);
        } catch (SpotifyWebApiException e) {
            assertThat(e.getMessage()).isEqualTo("Spotify API error");
        }
    }
}
