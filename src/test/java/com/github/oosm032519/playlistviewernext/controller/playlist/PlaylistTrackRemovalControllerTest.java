package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PlaylistTrackRemovalControllerTest {

    @Mock
    private SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;

    @InjectMocks
    private PlaylistTrackRemovalController controller;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new MockHttpSession();
    }

    @Test
    void removeTrackFromPlaylist_WithValidAccessToken_ReturnsOk() {
        // Arrange
        session.setAttribute("accessToken", "validToken");
        PlaylistTrackRemovalRequest request = new PlaylistTrackRemovalRequest();
        request.setPlaylistId("playlistId");
        request.setTrackId("trackId");

        when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(any(), eq("validToken")))
                .thenReturn(ResponseEntity.ok("Success"));

        // Act
        ResponseEntity<String> response = controller.removeTrackFromPlaylist(request, session);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("トラックが正常に削除されました");
    }

    @Test
    void removeTrackFromPlaylist_WithoutAccessToken_ReturnsUnauthorized() {
        // Arrange
        PlaylistTrackRemovalRequest request = new PlaylistTrackRemovalRequest();

        // Act
        ResponseEntity<String> response = controller.removeTrackFromPlaylist(request, session);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("認証が必要です");
    }

    @Test
    void removeTrackFromPlaylist_ServiceFails_ReturnsInternalServerError() {
        // Arrange
        session.setAttribute("accessToken", "validToken");
        PlaylistTrackRemovalRequest request = new PlaylistTrackRemovalRequest();

        when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(any(), eq("validToken")))
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        // Act
        ResponseEntity<String> response = controller.removeTrackFromPlaylist(request, session);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("トラックの削除に失敗しました");
    }
}
