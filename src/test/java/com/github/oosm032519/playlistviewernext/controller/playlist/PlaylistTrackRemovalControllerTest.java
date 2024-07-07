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

@ExtendWith(MockitoExtension.class)
class PlaylistTrackRemovalControllerTest {

    @Mock
    private SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;

    @InjectMocks
    private PlaylistTrackRemovalController playlistTrackRemovalController;

    private PlaylistTrackRemovalRequest playlistTrackRemovalRequest;

    @BeforeEach
    void setUp() {
        playlistTrackRemovalRequest = new PlaylistTrackRemovalRequest();
        playlistTrackRemovalRequest.setPlaylistId("playlistId");
        playlistTrackRemovalRequest.setTrackId("trackId");
    }

    @Nested
    @DisplayName("removeTrackFromPlaylist method tests")
    class RemoveTrackFromPlaylistTests {

        @Test
        @DisplayName("Should return unauthorized when principal is null")
        void shouldReturnUnauthorizedWhenPrincipalIsNull() {
            ResponseEntity<String> response = playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isEqualTo("認証が必要です。");
        }

        @Test
        @DisplayName("Should delegate to SpotifyPlaylistTrackRemovalService")
        void shouldDelegateToPlaylistService() {
            OAuth2User principal = mock(OAuth2User.class);
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("トラックが正常に削除されました。");
            when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal)).thenReturn(expectedResponse);

            ResponseEntity<String> response = playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            assertThat(response).isEqualTo(expectedResponse);
            verify(spotifyPlaylistTrackRemovalService).removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);
        }
    }
}
