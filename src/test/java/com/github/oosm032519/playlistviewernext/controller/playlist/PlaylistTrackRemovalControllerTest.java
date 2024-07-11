// PlaylistTrackRemovalControllerTest.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * PlaylistTrackRemovalControllerTest クラスは、PlaylistTrackRemovalController クラスのユニットテストを行います。
 */
@ExtendWith(MockitoExtension.class)
class PlaylistTrackRemovalControllerTest {

    /**
     * SpotifyPlaylistTrackRemovalService のモックオブジェクト。
     */
    @Mock
    private SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;

    /**
     * テスト対象の PlaylistTrackRemovalController のインスタンス。
     */
    @InjectMocks
    private PlaylistTrackRemovalController playlistTrackRemovalController;

    /**
     * テストで使用する PlaylistTrackRemovalRequest のインスタンス。
     */
    private PlaylistTrackRemovalRequest playlistTrackRemovalRequest;

    /**
     * 各テストの前に実行されるセットアップメソッド。
     * PlaylistTrackRemovalRequest の初期化を行います。
     */
    @BeforeEach
    void setUp() {
        playlistTrackRemovalRequest = new PlaylistTrackRemovalRequest();
        playlistTrackRemovalRequest.setPlaylistId("playlistId");
        playlistTrackRemovalRequest.setTrackId("trackId");
    }

    /**
     * removeTrackFromPlaylist メソッドのテストを行うネストクラス。
     */
    @Nested
    @DisplayName("removeTrackFromPlaylist method tests")
    class RemoveTrackFromPlaylistTests {

        /**
         * principal が null の場合に UNAUTHORIZED ステータスを返すことを確認するテスト。
         */
        @Test
        @DisplayName("Should return unauthorized when principal is null")
        void shouldReturnUnauthorizedWhenPrincipalIsNull() {
            // テスト対象メソッドを呼び出し、レスポンスを取得
            ResponseEntity<String> response = playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, null);

            // レスポンスのステータスコードとボディを検証
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isEqualTo("{\"error\": \"認証が必要です。\"}");
        }

        /**
         * SpotifyPlaylistTrackRemovalService に処理を委譲することを確認するテスト。
         */
        @Test
        @DisplayName("Should delegate to SpotifyPlaylistTrackRemovalService")
        void shouldDelegateToPlaylistService() {
            // OAuth2User のモックを作成
            OAuth2User principal = mock(OAuth2User.class);
            // 期待されるレスポンスを設定
            ResponseEntity<String> expectedResponse = ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body("{\"message\": \"トラックが正常に削除されました。\"}");
            // モックの振る舞いを設定
            when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal)).thenReturn(expectedResponse);

            // テスト対象メソッドを呼び出し、レスポンスを取得
            ResponseEntity<String> response = playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            // レスポンスとモックの呼び出しを検証
            assertThat(response).isEqualTo(expectedResponse);
            verify(spotifyPlaylistTrackRemovalService).removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);
        }
    }
}
