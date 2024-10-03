package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistCreationService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class PlaylistCreationControllerTest {

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService;

    @Mock
    private OAuth2User principal;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PlaylistCreationController playlistCreationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void whenUserIsNotAuthenticated_thenThrowPlaylistViewerNextException() {
        // Arrange
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> playlistCreationController.createPlaylist(List.of("track1", "track2"), principal))
                .isInstanceOf(AuthenticationException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.UNAUTHORIZED)
                .hasFieldOrPropertyWithValue("errorCode", "AUTHENTICATION_ERROR")
                .hasMessage("ユーザーが認証されていないか、アクセストークンが見つかりません。");
    }

    @Test
    void whenUserIsAuthenticated_thenCreatePlaylistSuccessfully() {
        // Arrange
        String accessToken = "validAccessToken";
        String userId = "userId";
        String userName = "userName";
        String playlistId = "newPlaylistId";
        List<String> trackIds = List.of("track1", "track2");

        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttribute("name")).thenReturn(userName);
        when(spotifyUserPlaylistCreationService.createPlaylist(eq(accessToken), eq(userId), any(String.class), eq(trackIds))).thenReturn(playlistId);

        // Act
        ResponseEntity<String> response = playlistCreationController.createPlaylist(trackIds, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(playlistId);
    }

    @Test
    void whenInternalServerErrorOccurs_thenThrowPlaylistViewerNextException() {
        // Arrange
        String accessToken = "validAccessToken";
        String userId = "userId";
        String userName = "userName";
        List<String> trackIds = List.of("track1", "track2");

        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttribute("name")).thenReturn(userName);
        when(spotifyUserPlaylistCreationService.createPlaylist(eq(accessToken), eq(userId), any(String.class), eq(trackIds)))
                .thenThrow(new RuntimeException("Some error"));

        // Act & Assert
        assertThatThrownBy(() -> playlistCreationController.createPlaylist(trackIds, principal))
                .isInstanceOf(SpotifyApiException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
                .hasFieldOrPropertyWithValue("errorCode", "PLAYLIST_CREATION_ERROR")
                .hasMessage("Spotify APIでプレイリストの作成中にエラーが発生しました。しばらく時間をおいてから再度お試しください。")
                .hasCauseInstanceOf(RuntimeException.class)
                .hasRootCauseMessage("Some error");
    }
}
