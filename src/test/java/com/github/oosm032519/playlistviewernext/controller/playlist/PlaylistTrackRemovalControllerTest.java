package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackRemovalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlaylistTrackRemovalControllerTest {

    @Mock
    private SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private PlaylistTrackRemovalController playlistTrackRemovalController;


    /**
     * トラックがプレイリストから正常に削除された場合、成功メッセージを含むレスポンスが返されることを確認する。
     */
    @Test
    void removeTrackFromPlaylist_success() throws Exception {
        // Arrange: テストデータの準備
        PlaylistTrackRemovalRequest request = new PlaylistTrackRemovalRequest();
        playlistTrackRemovalController = new PlaylistTrackRemovalController(spotifyPlaylistTrackRemovalService);

        // SpotifyPlaylistTrackRemovalServiceのモックを設定
        when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(request, principal))
                .thenReturn(ResponseEntity.ok("success"));

        // Act: removeTrackFromPlaylistメソッドを実行
        ResponseEntity<?> response = playlistTrackRemovalController.removeTrackFromPlaylist(request, principal);

        // Assert: レスポンスを確認
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "トラックが正常に削除されました。"));
    }

    /**
     * 認証されていないユーザーがトラックを削除しようとした場合、AuthenticationExceptionがスローされることを確認する。
     */
    @Test
    void removeTrackFromPlaylist_authenticationError() {
        // Arrange: テストデータの準備
        PlaylistTrackRemovalRequest request = new PlaylistTrackRemovalRequest();
        playlistTrackRemovalController = new PlaylistTrackRemovalController(spotifyPlaylistTrackRemovalService);

        // Act & Assert: principalがnullの場合
        assertThatThrownBy(() -> playlistTrackRemovalController.removeTrackFromPlaylist(request, null))
                .isInstanceOf(AuthenticationException.class)
                .hasMessage("認証されていないユーザーがアクセスしようとしました。");
    }

    /**
     * Spotify APIでトラックの削除中にエラーが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void removeTrackFromPlaylist_internalServerError() throws Exception {
        // Arrange: テストデータの準備
        PlaylistTrackRemovalRequest request = new PlaylistTrackRemovalRequest();
        playlistTrackRemovalController = new PlaylistTrackRemovalController(spotifyPlaylistTrackRemovalService);

        // SpotifyPlaylistTrackRemovalServiceのモックを設定、エラーレスポンスを返す
        when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(request, principal))
                .thenReturn(ResponseEntity.internalServerError().body("Spotify API Error"));

        // Act & Assert: InternalServerExceptionがスローされることを確認
        assertThatThrownBy(() -> playlistTrackRemovalController.removeTrackFromPlaylist(request, principal))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Spotify APIでトラックの削除中にエラーが発生しました。");
    }
}
