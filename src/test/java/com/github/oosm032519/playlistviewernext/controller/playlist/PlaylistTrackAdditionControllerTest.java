// PlaylistTrackAdditionControllerTest.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackAdditionRequest;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackAdditionService;
import org.apache.hc.core5.http.ParseException;
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

import java.io.IOException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistTrackAdditionControllerTest {

    private static final String PLAYLIST_ID = "playlistId123";
    private static final String TRACK_ID = "trackId456";
    private static final String ACCESS_TOKEN = "validAccessToken";
    private static final String SNAPSHOT_ID = "snapshotId789";

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private SpotifyPlaylistTrackAdditionService spotifyService;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private PlaylistTrackAdditionController playlistTrackAdditionController;

    // 正常にトラックがプレイリストに追加される場合のテスト
    @Test
    void addTrackToPlaylist_成功時() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId(PLAYLIST_ID);
        request.setTrackId(TRACK_ID);

        // ユーザーのアクセストークンを取得するモック設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(ACCESS_TOKEN);

        // SpotifyのAPI呼び出しの結果をモック設定
        SnapshotResult snapshotResult = mock(SnapshotResult.class);
        when(snapshotResult.getSnapshotId()).thenReturn(SNAPSHOT_ID);
        when(spotifyService.addTrackToPlaylist(ACCESS_TOKEN, PLAYLIST_ID, TRACK_ID)).thenReturn(snapshotResult);

        // Act
        ResponseEntity<Map<String, String>> response = playlistTrackAdditionController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(Map.of("message", "トラックが正常に追加されました。", "snapshot_id", SNAPSHOT_ID));

        // メソッド呼び出しの検証
        verify(userAuthenticationService).getAccessToken(principal);
        verify(spotifyService).addTrackToPlaylist(ACCESS_TOKEN, PLAYLIST_ID, TRACK_ID);
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
        ResponseEntity<Map<String, String>> response = playlistTrackAdditionController.addTrackToPlaylist(request, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "認証が必要です。"));

        // メソッド呼び出しの検証
        verify(userAuthenticationService).getAccessToken(null);
        verifyNoInteractions(spotifyService);
    }

    // アクセストークンがない場合のテスト
    @Test
    void addTrackToPlaylist_アクセストークンがない場合() {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId(PLAYLIST_ID);
        request.setTrackId(TRACK_ID);

        // アクセストークンがない場合のモック設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(null);

        // Act
        ResponseEntity<Map<String, String>> response = playlistTrackAdditionController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "認証が必要です。"));

        // メソッド呼び出しの検証
        verify(userAuthenticationService).getAccessToken(principal);
        verifyNoInteractions(spotifyService);
    }

    // Spotify APIでエラーが発生した場合のテスト
    @Test
    void addTrackToPlaylist_SpotifyAPIエラーの場合() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId(PLAYLIST_ID);
        request.setTrackId(TRACK_ID);

        // ユーザーのアクセストークンを取得するモック設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(ACCESS_TOKEN);

        // SpotifyのAPI呼び出しでエラーが発生するモック設定
        when(spotifyService.addTrackToPlaylist(ACCESS_TOKEN, PLAYLIST_ID, TRACK_ID)).thenThrow(new SpotifyWebApiException("Spotify API error"));

        // Act
        ResponseEntity<Map<String, String>> response = playlistTrackAdditionController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo(Map.of("error", "エラー: Spotify API error"));

        // メソッド呼び出しの検証
        verify(userAuthenticationService).getAccessToken(principal);
        verify(spotifyService).addTrackToPlaylist(ACCESS_TOKEN, PLAYLIST_ID, TRACK_ID);
    }
}
