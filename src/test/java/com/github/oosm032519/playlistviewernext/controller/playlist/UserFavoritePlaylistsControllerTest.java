package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.UserFavoritePlaylistsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserFavoritePlaylistsControllerTest {

    @Mock
    private UserFavoritePlaylistsService userFavoritePlaylistsService;

    @Mock
    private OAuth2User principal;

    private UserFavoritePlaylistsController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        controller = new UserFavoritePlaylistsController(userFavoritePlaylistsService);
    }

    @Test
    void getFavoritePlaylists_Success() {
        // Arrange
        String userId = "testUser";
        when(principal.getAttribute("id")).thenReturn(userId);

        List<FavoritePlaylistResponse> expectedPlaylists = Arrays.asList(
                new FavoritePlaylistResponse("1", "Playlist 1", "Owner 1", 10, LocalDateTime.now()),
                new FavoritePlaylistResponse("2", "Playlist 2", "Owner 2", 15, LocalDateTime.now())
        );
        when(userFavoritePlaylistsService.getFavoritePlaylists(userId)).thenReturn(expectedPlaylists);

        // Act
        ResponseEntity<List<FavoritePlaylistResponse>> response = controller.getFavoritePlaylists(principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);
        verify(userFavoritePlaylistsService).getFavoritePlaylists(userId);
    }

    @Test
    void getFavoritePlaylists_Exception() {
        // Arrange
        String userId = "testUser";
        when(principal.getAttribute("id")).thenReturn(userId);
        when(userFavoritePlaylistsService.getFavoritePlaylists(userId)).thenThrow(new RuntimeException("Test exception"));

        // Act
        ResponseEntity<List<FavoritePlaylistResponse>> response = controller.getFavoritePlaylists(principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }
}
