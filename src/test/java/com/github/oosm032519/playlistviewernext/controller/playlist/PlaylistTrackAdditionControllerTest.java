// PlaylistTrackAdditionControllerTest.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackAdditionRequest;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackAdditionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistTrackAdditionControllerTest {

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private SpotifyPlaylistTrackAdditionService spotifyService;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private PlaylistTrackAdditionController playlistTrackAdditionController;

    /**
     * 正常にトラックがプレイリストに追加される場合のテスト
     */
    @Test
    void addTrackToPlaylist_成功時() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "playlistId123";
        String trackId = "trackId456";
        String accessToken = "validAccessToken";
        String snapshotId = "snapshotId789";

        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId(playlistId);
        request.setTrackId(trackId);

        // ユーザーのアクセストークンを取得するモック設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);

        // SpotifyのAPI呼び出しの結果をモック設定
        SnapshotResult snapshotResult = mock(SnapshotResult.class);
        when(snapshotResult.getSnapshotId()).thenReturn(snapshotId);
        when(spotifyService.addTrackToPlaylist(accessToken, playlistId, trackId)).thenReturn(snapshotResult);

        // Act
        ResponseEntity<String> response = playlistTrackAdditionController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("トラックが正常に追加されました。Snapshot ID: " + snapshotId);

        // メソッド呼び出しの検証
        verify(userAuthenticationService).getAccessToken(principal);
        verify(spotifyService).addTrackToPlaylist(accessToken, playlistId, trackId);
    }

    /**
     * 認証されていない場合のテスト
     */
    @Test
    void addTrackToPlaylist_認証されていない場合() {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();

        // 認証されていない場合のモック設定
        when(userAuthenticationService.getAccessToken(null)).thenReturn(null);

        // Act
        ResponseEntity<String> response = playlistTrackAdditionController.addTrackToPlaylist(request, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("認証が必要です。");

        // メソッド呼び出しの検証
        verify(userAuthenticationService).getAccessToken(null);
        verifyNoInteractions(spotifyService);
    }

    /**
     * アクセストークンがない場合のテスト
     */
    @Test
    void addTrackToPlaylist_アクセストークンがない場合() {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId("playlistId123");
        request.setTrackId("trackId456");

        // アクセストークンがない場合のモック設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(null);

        // Act
        ResponseEntity<String> response = playlistTrackAdditionController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("認証が必要です。");

        // メソッド呼び出しの検証
        verify(userAuthenticationService).getAccessToken(principal);
        verifyNoInteractions(spotifyService);
    }

    /**
     * Spotify APIでエラーが発生した場合のテスト
     */
    @Test
    void addTrackToPlaylist_SpotifyAPIエラーの場合() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "playlistId123";
        String trackId = "trackId456";
        String accessToken = "validAccessToken";

        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId(playlistId);
        request.setTrackId(trackId);

        // ユーザーのアクセストークンを取得するモック設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);

        // SpotifyのAPI呼び出しでエラーが発生するモック設定
        when(spotifyService.addTrackToPlaylist(accessToken, playlistId, trackId)).thenThrow(new SpotifyWebApiException("Spotify API error"));

        // Act
        ResponseEntity<String> response = playlistTrackAdditionController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("エラー: Spotify API error");

        // メソッド呼び出しの検証
        verify(userAuthenticationService).getAccessToken(principal);
        verify(spotifyService).addTrackToPlaylist(accessToken, playlistId, trackId);
    }
}
