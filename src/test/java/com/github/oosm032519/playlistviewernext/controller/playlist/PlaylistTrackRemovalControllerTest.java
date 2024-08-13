package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        @DisplayName("Should throw PlaylistViewerNextException when principal is null")
        void shouldThrowPlaylistViewerNextExceptionWhenPrincipalIsNull() {
            // Arrange
            OAuth2User principal = null;

            // Act & Assert
            assertThatThrownBy(() -> playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal))
                    .isInstanceOf(PlaylistViewerNextException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.UNAUTHORIZED)
                    .hasFieldOrPropertyWithValue("errorCode", "UNAUTHORIZED_ACCESS")
                    .hasMessage("認証されていないユーザーがアクセスしようとしました。");
        }

        @Test
        @DisplayName("Should return OK when track removal is successful")
        void shouldReturnOkWhenTrackRemovalIsSuccessful() {
            // Arrange
            OAuth2User principal = mock(OAuth2User.class);
            ResponseEntity<String> serviceResponse = ResponseEntity.ok("Some content");
            when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal)).thenReturn(serviceResponse);

            // Act
            ResponseEntity<Map<String, String>> response = playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo(Map.of("message", "トラックが正常に削除されました。"));
            verify(spotifyPlaylistTrackRemovalService).removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);
        }

        @Test
        @DisplayName("Should throw PlaylistViewerNextException when track removal fails")
        void shouldThrowPlaylistViewerNextExceptionWhenTrackRemovalFails() {
            // Arrange
            OAuth2User principal = mock(OAuth2User.class);
            ResponseEntity<String> serviceResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal)).thenReturn(serviceResponse);

            // Act & Assert
            assertThatThrownBy(() -> playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal))
                    .isInstanceOf(PlaylistViewerNextException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
                    .hasFieldOrPropertyWithValue("errorCode", "TRACK_REMOVAL_ERROR")
                    .hasMessage("トラックの削除中にエラーが発生しました。");
            verify(spotifyPlaylistTrackRemovalService).removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);
        }

        @Test
        @DisplayName("Should throw PlaylistViewerNextException when an exception occurs during track removal")
        void shouldThrowPlaylistViewerNextExceptionWhenExceptionOccursDuringTrackRemoval() {
            // Arrange
            OAuth2User principal = mock(OAuth2User.class);
            when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal)).thenThrow(new RuntimeException("Some error"));

            // Act & Assert
            assertThatThrownBy(() -> playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal))
                    .isInstanceOf(PlaylistViewerNextException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
                    .hasFieldOrPropertyWithValue("errorCode", "TRACK_REMOVAL_ERROR")
                    .hasMessage("トラックの削除中にエラーが発生しました。")
                    .hasCauseInstanceOf(RuntimeException.class);
            verify(spotifyPlaylistTrackRemovalService).removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);
        }
    }
}
