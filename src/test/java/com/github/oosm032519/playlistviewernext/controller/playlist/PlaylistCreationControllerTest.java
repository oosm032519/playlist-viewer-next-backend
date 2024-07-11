// PlaylistCreationControllerTest.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistCreationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PlaylistCreationControllerTest {

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private PlaylistCreationController playlistCreationController;

    /**
     * 各テストメソッドの前に実行されるセットアップメソッド。
     * Mockitoのモックオブジェクトを初期化します。
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 認証されていないユーザーがプレイリストを作成しようとした場合のテスト。
     * 期待される結果はHTTPステータスコード401（UNAUTHORIZED）です。
     */
    @Test
    void createPlaylist_Unauthorized() {
        // Arrange: 認証トークンがnullであることをモック
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(null);

        // Act: プレイリスト作成メソッドを呼び出し
        ResponseEntity<String> response = playlistCreationController.createPlaylist(List.of("track1", "track2"), principal);

        // Assert: ステータスコードがUNAUTHORIZEDであることを確認
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("認証が必要です。");
    }

    /**
     * 認証されたユーザーが正常にプレイリストを作成できる場合のテスト。
     * 期待される結果はHTTPステータスコード200（OK）とプレイリストIDです。
     */
    @Test
    void createPlaylist_Success() throws Exception {
        // Arrange: 必要なモックデータを設定
        String accessToken = "validAccessToken";
        String userId = "userId";
        String userName = "userName";
        String playlistId = "newPlaylistId";
        List<String> trackIds = List.of("track1", "track2");

        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttributes()).thenReturn(Map.of("display_name", userName));
        when(spotifyUserPlaylistCreationService.createPlaylist(eq(accessToken), eq(userId), any(String.class), eq(trackIds))).thenReturn(playlistId);

        // Act: プレイリスト作成メソッドを呼び出し
        ResponseEntity<String> response = playlistCreationController.createPlaylist(trackIds, principal);

        // Assert: ステータスコードがOKであり、レスポンスボディにプレイリストIDが含まれていることを確認
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(playlistId);
    }

    /**
     * プレイリスト作成中に内部サーバーエラーが発生した場合のテスト。
     * 期待される結果はHTTPステータスコード500（INTERNAL_SERVER_ERROR）です。
     */
    @Test
    void createPlaylist_InternalServerError() throws Exception {
        // Arrange: 必要なモックデータを設定し、例外をスローするように設定
        String accessToken = "validAccessToken";
        String userId = "userId";
        String userName = "userName";
        List<String> trackIds = List.of("track1", "track2");

        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttributes()).thenReturn(Map.of("display_name", userName));
        when(spotifyUserPlaylistCreationService.createPlaylist(eq(accessToken), eq(userId), any(String.class), eq(trackIds))).thenThrow(new RuntimeException("Some error"));

        // Act: プレイリスト作成メソッドを呼び出し
        ResponseEntity<String> response = playlistCreationController.createPlaylist(trackIds, principal);

        // Assert: ステータスコードがINTERNAL_SERVER_ERRORであり、レスポンスボディにエラーメッセージが含まれていることを確認
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("エラー: Some error");
    }
}
