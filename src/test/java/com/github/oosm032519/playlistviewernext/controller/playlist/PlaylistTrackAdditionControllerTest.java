package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.model.PlaylistTrackAdditionRequest;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackAdditionService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private HttpServletRequest request;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private PlaylistTrackAdditionController playlistTrackAdditionController;

    @Test
    void addTrackToPlaylist_成功時() throws Exception {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId(PLAYLIST_ID);
        request.setTrackId(TRACK_ID);

        when(userAuthenticationService.getAccessToken(principal)).thenReturn(ACCESS_TOKEN);

        SnapshotResult snapshotResult = mock(SnapshotResult.class);
        when(snapshotResult.getSnapshotId()).thenReturn(SNAPSHOT_ID);
        when(spotifyService.addTrackToPlaylist(ACCESS_TOKEN, PLAYLIST_ID, TRACK_ID)).thenReturn(snapshotResult);

        // Act
        ResponseEntity<Map<String, String>> response = playlistTrackAdditionController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("message", "トラックが正常に追加されました。");
        assertThat(response.getBody()).containsEntry("snapshot_id", SNAPSHOT_ID);

        verify(userAuthenticationService).getAccessToken(principal);
        verify(spotifyService).addTrackToPlaylist(ACCESS_TOKEN, PLAYLIST_ID, TRACK_ID);
    }

    @Test
    void addTrackToPlaylist_認証されていない場合() {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId(PLAYLIST_ID);
        request.setTrackId(TRACK_ID);

        when(userAuthenticationService.getAccessToken(principal)).thenReturn(null);

        // Act & Assert
        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> playlistTrackAdditionController.addTrackToPlaylist(request, principal));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getErrorCode()).isEqualTo("AUTHENTICATION_ERROR");
        assertThat(exception.getMessage()).isEqualTo("ユーザーが認証されていないか、アクセストークンが見つかりません。");

        verify(userAuthenticationService).getAccessToken(principal);
        verifyNoInteractions(spotifyService);
    }

    @Test
    void addTrackToPlaylist_SpotifyAPIエラーの場合() throws Exception {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId(PLAYLIST_ID);
        request.setTrackId(TRACK_ID);

        when(userAuthenticationService.getAccessToken(principal)).thenReturn(ACCESS_TOKEN);
        when(spotifyService.addTrackToPlaylist(ACCESS_TOKEN, PLAYLIST_ID, TRACK_ID))
                .thenThrow(new RuntimeException("Spotify API error"));

        // Act & Assert
        SpotifyApiException exception = assertThrows(SpotifyApiException.class,
                () -> playlistTrackAdditionController.addTrackToPlaylist(request, principal));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getErrorCode()).isEqualTo("TRACK_ADDITION_ERROR");
        assertThat(exception.getMessage()).isEqualTo("Spotify APIでトラックの追加中にエラーが発生しました。しばらく時間をおいてから再度お試しください。");

        verify(userAuthenticationService).getAccessToken(principal);
        verify(spotifyService).addTrackToPlaylist(ACCESS_TOKEN, PLAYLIST_ID, TRACK_ID);
    }
}
