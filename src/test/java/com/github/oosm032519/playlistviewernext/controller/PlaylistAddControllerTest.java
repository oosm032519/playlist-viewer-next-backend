package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.model.AddTrackRequest;
import com.github.oosm032519.playlistviewernext.security.AuthService;
import com.github.oosm032519.playlistviewernext.service.PlaylistAddSearvice;
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
class PlaylistAddControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private PlaylistAddSearvice spotifyService;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private PlaylistAddController playlistAddController;

    @Test
    void addTrackToPlaylist_成功時() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "playlistId123";
        String trackId = "trackId456";
        String accessToken = "validAccessToken";
        String snapshotId = "snapshotId789";

        AddTrackRequest request = new AddTrackRequest();
        request.setPlaylistId(playlistId);
        request.setTrackId(trackId);

        when(authService.getAccessToken(principal)).thenReturn(accessToken);

        SnapshotResult snapshotResult = mock(SnapshotResult.class);
        when(snapshotResult.getSnapshotId()).thenReturn(snapshotId);
        when(spotifyService.addTrackToPlaylist(accessToken, playlistId, trackId)).thenReturn(snapshotResult);

        // Act
        ResponseEntity<String> response = playlistAddController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("トラックが正常に追加されました。Snapshot ID: " + snapshotId);

        verify(authService).getAccessToken(principal);
        verify(spotifyService).addTrackToPlaylist(accessToken, playlistId, trackId);
    }

    @Test
    void addTrackToPlaylist_認証されていない場合() {
        // Arrange
        AddTrackRequest request = new AddTrackRequest();

        when(authService.getAccessToken(null)).thenReturn(null);

        // Act
        ResponseEntity<String> response = playlistAddController.addTrackToPlaylist(request, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("認証が必要です。");

        verify(authService).getAccessToken(null);
        verifyNoInteractions(spotifyService);
    }

    @Test
    void addTrackToPlaylist_アクセストークンがない場合() {
        // Arrange
        AddTrackRequest request = new AddTrackRequest();
        request.setPlaylistId("playlistId123");
        request.setTrackId("trackId456");

        when(authService.getAccessToken(principal)).thenReturn(null);

        // Act
        ResponseEntity<String> response = playlistAddController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("認証が必要です。");

        verify(authService).getAccessToken(principal);
        verifyNoInteractions(spotifyService);
    }

    @Test
    void addTrackToPlaylist_SpotifyAPIエラーの場合() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "playlistId123";
        String trackId = "trackId456";
        String accessToken = "validAccessToken";

        AddTrackRequest request = new AddTrackRequest();
        request.setPlaylistId(playlistId);
        request.setTrackId(trackId);

        when(authService.getAccessToken(principal)).thenReturn(accessToken);
        when(spotifyService.addTrackToPlaylist(accessToken, playlistId, trackId)).thenThrow(new SpotifyWebApiException("Spotify API error"));

        // Act
        ResponseEntity<String> response = playlistAddController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("エラー: Spotify API error");

        verify(authService).getAccessToken(principal);
        verify(spotifyService).addTrackToPlaylist(accessToken, playlistId, trackId);
    }
}
