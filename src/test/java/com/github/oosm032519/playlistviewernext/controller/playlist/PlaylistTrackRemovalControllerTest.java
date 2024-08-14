package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.ErrorResponse;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
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
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
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
        @DisplayName("Should throw AuthenticationException when principal is null")
        void shouldThrowAuthenticationExceptionWhenPrincipalIsNull() {
            // Arrange
            OAuth2User principal = null;

            // Act & Assert
            assertThatThrownBy(() -> playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal))
                    .isInstanceOf(AuthenticationException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.UNAUTHORIZED)
                    .hasFieldOrPropertyWithValue("errorCode", "AUTHENTICATION_ERROR") // エラーコードを修正
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
            ResponseEntity<?> response = playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            // Assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            verify(spotifyPlaylistTrackRemovalService).removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);
        }

        @Test
        @DisplayName("Should return INTERNAL_SERVER_ERROR and correct error code when track removal fails")
        void shouldThrowSpotifyApiExceptionWhenTrackRemovalFails() {
            // Arrange
            OAuth2User principal = mock(OAuth2User.class);
            ResponseEntity<String> serviceResponse = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error body");
            when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal)).thenReturn(serviceResponse);

            // Act
            ResponseEntity<?> responseEntity = playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            // Assert
            assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
            assertThat(errorResponse.getErrorCode()).isEqualTo("TRACK_REMOVAL_ERROR");
            verify(spotifyPlaylistTrackRemovalService).removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);
        }

        @Test
        @DisplayName("Should throw SpotifyApiException when an exception occurs during track removal")
        void shouldThrowSpotifyApiExceptionWhenExceptionOccursDuringTrackRemoval() {
            // Arrange
            OAuth2User principal = mock(OAuth2User.class);
            when(spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal)).thenThrow(new RuntimeException("Some error"));

            // Act & Assert
            assertThatThrownBy(() -> playlistTrackRemovalController.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal))
                    .isInstanceOf(SpotifyApiException.class)
                    .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
                    .hasFieldOrPropertyWithValue("errorCode", "TRACK_REMOVAL_ERROR")
                    .hasMessage("トラックの削除中にエラーが発生しました。しばらく時間をおいてから再度お試しください。")
                    .hasCauseInstanceOf(RuntimeException.class);
            verify(spotifyPlaylistTrackRemovalService).removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);
        }
    }
}
