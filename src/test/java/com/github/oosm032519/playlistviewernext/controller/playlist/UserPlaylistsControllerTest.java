package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistsService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPlaylistsControllerTest {

    @Mock
    private SpotifyUserPlaylistsService userPlaylistsService;

    @InjectMocks
    private UserPlaylistsController userPlaylistsController;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void getFollowedPlaylists_ReturnsPlaylistsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        List<PlaylistSimplified> expectedPlaylists = Arrays.asList(
                new PlaylistSimplified.Builder().setName("Followed Playlist 1").build(),
                new PlaylistSimplified.Builder().setName("Followed Playlist 2").build()
        );

        when(userPlaylistsService.getCurrentUsersPlaylists(authToken)).thenReturn(expectedPlaylists);

        // When
        ResponseEntity<?> response = userPlaylistsController.getFollowedPlaylists(authToken);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);
        verify(userPlaylistsService).getCurrentUsersPlaylists(authToken);
    }

    @Test
    void getFollowedPlaylists_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        when(userPlaylistsService.getCurrentUsersPlaylists(authToken)).thenThrow(new RuntimeException("Authentication error"));

        // When
        ResponseEntity<?> response = userPlaylistsController.getFollowedPlaylists(authToken);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Error: Authentication error");
        verify(userPlaylistsService).getCurrentUsersPlaylists(authToken);
    }
}
