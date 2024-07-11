// PlaylistSearchControllerTest.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistSearchControllerTest {

    @Mock
    private SpotifyPlaylistSearchService playlistSearchService;

    @Mock
    private SpotifyClientCredentialsAuthentication authController;

    @InjectMocks
    private PlaylistSearchController searchController;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void searchPlaylists_ReturnsPlaylistsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // テスト用のクエリ、オフセット、リミットを設定
        String query = "test query";
        int offset = 0;
        int limit = 20;

        // 期待されるプレイリストのリストを作成
        List<PlaylistSimplified> expectedPlaylists = Arrays.asList(
                new PlaylistSimplified.Builder().setName("Playlist 1").build(),
                new PlaylistSimplified.Builder().setName("Playlist 2").build()
        );

        // モックサービスが期待されるプレイリストを返すように設定
        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenReturn(expectedPlaylists);

        // コントローラーのメソッドを呼び出し、レスポンスを取得
        ResponseEntity<List<PlaylistSimplified>> response = searchController.searchPlaylists(query, offset, limit);

        // レスポンスのステータスコードとボディを検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);

        // モックサービスのメソッドが呼び出されたことを検証
        verify(playlistSearchService).searchPlaylists(query, offset, limit);
    }

    @Test
    void searchPlaylists_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // テスト用のクエリ、オフセット、リミットを設定
        String query = "test query";
        int offset = 0;
        int limit = 20;

        // モックサービスが例外をスローするように設定
        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenThrow(new RuntimeException("API error"));

        // コントローラーのメソッドを呼び出し、レスポンスを取得
        ResponseEntity<List<PlaylistSimplified>> response = searchController.searchPlaylists(query, offset, limit);

        // レスポンスのステータスコードとボディを検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();

        // モックサービスのメソッドが呼び出されたことを検証
        verify(playlistSearchService).searchPlaylists(query, offset, limit);
    }
}
