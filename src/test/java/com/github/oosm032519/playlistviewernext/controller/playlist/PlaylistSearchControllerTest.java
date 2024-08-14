package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    void givenValidQuery_whenSearchPlaylists_thenReturnsPlaylistsSuccessfully() throws Exception {
        // Arrange
        String query = "test query";
        int offset = 0;
        int limit = 20;
        List<PlaylistSimplified> expectedPlaylists = createMockPlaylists();

        // モックサービスが期待されるプレイリストを返すように設定
        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenReturn(expectedPlaylists);

        // Act
        ResponseEntity<List<PlaylistSimplified>> response = searchController.searchPlaylists(query, offset, limit);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);
        verify(playlistSearchService).searchPlaylists(query, offset, limit);
        verify(authController).authenticate();
    }

    @Test
    void givenServiceThrowsException_whenSearchPlaylists_thenThrowsSpotifyApiException() throws Exception {
        // Arrange
        String query = "test query";
        int offset = 0;
        int limit = 20;

        // モックサービスが例外をスローするように設定
        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenThrow(new RuntimeException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> searchController.searchPlaylists(query, offset, limit))
                .isInstanceOf(SpotifyApiException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
                .hasFieldOrPropertyWithValue("errorCode", "PLAYLIST_SEARCH_ERROR")
                .hasMessage("Spotify APIでプレイリストの検索中にエラーが発生しました。検索キーワードを見直して、しばらく時間をおいてから再度お試しください。");

        verify(playlistSearchService).searchPlaylists(query, offset, limit);
        verify(authController).authenticate();
    }

    private List<PlaylistSimplified> createMockPlaylists() {
        return Arrays.asList(
                new PlaylistSimplified.Builder().setName("Playlist 1").build(),
                new PlaylistSimplified.Builder().setName("Playlist 2").build()
        );
    }
}
