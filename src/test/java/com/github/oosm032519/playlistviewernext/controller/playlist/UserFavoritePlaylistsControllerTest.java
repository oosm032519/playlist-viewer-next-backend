package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.UserFavoritePlaylistsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserFavoritePlaylistsControllerTest {

    @Mock
    private UserFavoritePlaylistsService userFavoritePlaylistsService;

    @InjectMocks
    private UserFavoritePlaylistsController controller;

    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        session = new MockHttpSession();
    }

    @Test
    void getFavoritePlaylists_WithAuthenticatedUser_ReturnsOkWithPlaylists() {
        // Arrange
        String userId = "testUser";
        session.setAttribute("userId", userId);
        LocalDateTime now = LocalDateTime.now();
        List<FavoritePlaylistResponse> expectedPlaylists = Arrays.asList(
                new FavoritePlaylistResponse("1", "Playlist 1", "Owner 1", 10, now),
                new FavoritePlaylistResponse("2", "Playlist 2", "Owner 2", 15, now.plusHours(1))
        );
        when(userFavoritePlaylistsService.getFavoritePlaylists(userId)).thenReturn(expectedPlaylists);

        // Act
        ResponseEntity<List<FavoritePlaylistResponse>> response = controller.getFavoritePlaylists(session);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).getPlaylistId()).isEqualTo("1");
        assertThat(response.getBody().get(0).getPlaylistName()).isEqualTo("Playlist 1");
        assertThat(response.getBody().get(0).getPlaylistOwnerName()).isEqualTo("Owner 1");
        assertThat(response.getBody().get(0).getTotalTracks()).isEqualTo(10);
        assertThat(response.getBody().get(0).getAddedAt()).isEqualTo(now);
        verify(userFavoritePlaylistsService).getFavoritePlaylists(userId);
    }

    @Test
    void getFavoritePlaylists_WithUnauthenticatedUser_ReturnsUnauthorized() {
        // Arrange
        session.removeAttribute("userId");

        // Act
        ResponseEntity<List<FavoritePlaylistResponse>> response = controller.getFavoritePlaylists(session);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNull();
        verifyNoInteractions(userFavoritePlaylistsService);
    }

    @Test
    void getFavoritePlaylists_WhenServiceThrowsException_ReturnsInternalServerError() {
        // Arrange
        String userId = "testUser";
        session.setAttribute("userId", userId);
        when(userFavoritePlaylistsService.getFavoritePlaylists(userId)).thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<List<FavoritePlaylistResponse>> response = controller.getFavoritePlaylists(session);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
        verify(userFavoritePlaylistsService).getFavoritePlaylists(userId);
    }
}
