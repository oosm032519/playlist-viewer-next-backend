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
            // Arrange
            OAuth2User principal = null;

            // Act
            ResponseEntity<String> response = playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isEqualTo("{\"error\": \"認証が必要です。\"}");
        }

        @Test
        @DisplayName("Should return OK when track removal is successful")
        void shouldReturnOkWhenTrackRemovalIsSuccessful() {
            // Arrange
            OAuth2User principal = mock(OAuth2User.class);
            ResponseEntity<String> serviceResponse = ResponseEntity.ok("Some content");  // 非空のボディを持つレスポンス
            when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal)).thenReturn(serviceResponse);

            // Act
            ResponseEntity<String> response = playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("{\"message\": \"トラックが正常に削除されました。\"}");
            verify(spotifyPlaylistTrackRemovalService).removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);
        }


        @Test
        @DisplayName("Should return INTERNAL_SERVER_ERROR when track removal fails")
        void shouldReturnInternalServerErrorWhenTrackRemovalFails() {
            // Arrange
            OAuth2User principal = mock(OAuth2User.class);
            ResponseEntity<String> serviceResponse = ResponseEntity.noContent().build();
            when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal)).thenReturn(serviceResponse);

            // Act
            ResponseEntity<String> response = playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isEqualTo("{\"error\": \"トラックの削除に失敗しました。\"}");
            verify(spotifyPlaylistTrackRemovalService).removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);
        }
    }
}
