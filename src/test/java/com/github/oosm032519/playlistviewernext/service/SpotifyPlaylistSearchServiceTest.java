package com.github.oosm032519.playlistviewernext.service;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistSearchServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @InjectMocks
    private SpotifyPlaylistSearchService playlistSearchService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testSearchPlaylists_正常系_検索結果あり() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String query = "test query";
        SearchPlaylistsRequest.Builder builder = mock(SearchPlaylistsRequest.Builder.class);
        SearchPlaylistsRequest searchPlaylistsRequest = mock(SearchPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistSimplifiedPaging = mock(Paging.class);
        PlaylistSimplified[] playlistSimplifieds = new PlaylistSimplified[]{mock(PlaylistSimplified.class)};

        when(spotifyApi.searchPlaylists(query)).thenReturn(builder);
        when(builder.limit(20)).thenReturn(builder);
        when(builder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenReturn(playlistSimplifiedPaging);
        when(playlistSimplifiedPaging.getItems()).thenReturn(playlistSimplifieds);

        // Act
        List<PlaylistSimplified> result = playlistSearchService.searchPlaylists(query);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(playlistSimplifieds[0]);
    }

    @Test
    void testSearchPlaylists_正常系_検索結果なし() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String query = "empty query";
        SearchPlaylistsRequest.Builder builder = mock(SearchPlaylistsRequest.Builder.class);
        SearchPlaylistsRequest searchPlaylistsRequest = mock(SearchPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistSimplifiedPaging = mock(Paging.class);

        when(spotifyApi.searchPlaylists(query)).thenReturn(builder);
        when(builder.limit(20)).thenReturn(builder);
        when(builder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenReturn(playlistSimplifiedPaging);
        when(playlistSimplifiedPaging.getItems()).thenReturn(null);

        // Act
        List<PlaylistSimplified> result = playlistSearchService.searchPlaylists(query);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testSearchPlaylists_異常系_APIエラー() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String query = "test query";
        SearchPlaylistsRequest.Builder builder = mock(SearchPlaylistsRequest.Builder.class);
        SearchPlaylistsRequest searchPlaylistsRequest = mock(SearchPlaylistsRequest.class);

        when(spotifyApi.searchPlaylists(query)).thenReturn(builder);
        when(builder.limit(20)).thenReturn(builder);
        when(builder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenThrow(new IOException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> playlistSearchService.searchPlaylists(query))
                .isInstanceOf(IOException.class)
                .hasMessage("API error");
    }
}
