package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.controller.playlist.PlaylistSearchController;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistSearchControllerTest {

    @Mock
    private SpotifyPlaylistSearchService playlistSearchService;

    @Mock
    private SpotifyClientCredentialsAuthentication authController;

    @InjectMocks
    private PlaylistSearchController searchController;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void searchPlaylists_ReturnsPlaylistsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        String query = "test query";
        List<PlaylistSimplified> expectedPlaylists = Arrays.asList(
                new PlaylistSimplified.Builder().setName("Playlist 1").build(),
                new PlaylistSimplified.Builder().setName("Playlist 2").build()
        );

        when(playlistSearchService.searchPlaylists(query)).thenReturn(expectedPlaylists);

        // When
        ResponseEntity<List<PlaylistSimplified>> response = searchController.searchPlaylists(query);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);
        verify(playlistSearchService).searchPlaylists(query);
    }

    @Test
    void searchPlaylists_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given
        String query = "test query";
        when(playlistSearchService.searchPlaylists(query)).thenThrow(new RuntimeException("API error"));

        // When
        ResponseEntity<List<PlaylistSimplified>> response = searchController.searchPlaylists(query);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
        verify(playlistSearchService).searchPlaylists(query);
    }
}
