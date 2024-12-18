package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.apache.hc.core5.http.ParseException;
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
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistSearchServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @InjectMocks
    private SpotifyPlaylistSearchService playlistSearchService;

    @Test
    void testSearchPlaylists_正常系_検索結果あり() throws SpotifyWebApiException, ParseException, IOException {
        // Arrange
        String query = "test query";
        int offset = 0;
        int limit = 20;
        SearchPlaylistsRequest.Builder builder = mock(SearchPlaylistsRequest.Builder.class);
        SearchPlaylistsRequest searchPlaylistsRequest = mock(SearchPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistSimplifiedPaging = mock(Paging.class);
        PlaylistSimplified[] playlistSimplifieds = new PlaylistSimplified[]{mock(PlaylistSimplified.class)};

        when(spotifyApi.searchPlaylists(query)).thenReturn(builder);
        when(builder.limit(limit)).thenReturn(builder);
        when(builder.offset(offset)).thenReturn(builder);
        when(builder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenReturn(playlistSimplifiedPaging);
        when(playlistSimplifiedPaging.getItems()).thenReturn(playlistSimplifieds);
        when(playlistSimplifiedPaging.getTotal()).thenReturn(1);

        // Act
        Map<String, Object> result = playlistSearchService.searchPlaylists(query, offset, limit);

        // Assert
        assertThat(result.get("playlists")).isEqualTo(Arrays.asList(playlistSimplifieds));
        assertThat(result.get("total")).isEqualTo(1);
    }

    @Test
    void testSearchPlaylists_正常系_検索結果なし() throws SpotifyWebApiException, ParseException, IOException {
        // Arrange
        String query = "empty query";
        int offset = 0;
        int limit = 20;
        SearchPlaylistsRequest.Builder builder = mock(SearchPlaylistsRequest.Builder.class);
        SearchPlaylistsRequest searchPlaylistsRequest = mock(SearchPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistSimplifiedPaging = mock(Paging.class);

        when(spotifyApi.searchPlaylists(query)).thenReturn(builder);
        when(builder.limit(limit)).thenReturn(builder);
        when(builder.offset(offset)).thenReturn(builder);
        when(builder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenReturn(playlistSimplifiedPaging);
        when(playlistSimplifiedPaging.getItems()).thenReturn(null);
        when(playlistSimplifiedPaging.getTotal()).thenReturn(0);


        // Act
        Map<String, Object> result = playlistSearchService.searchPlaylists(query, offset, limit);

        // Assert
        assertThat(result.get("playlists")).isEqualTo(Collections.emptyList());
        assertThat(result.get("total")).isEqualTo(0);
    }

    @Test
    void testSearchPlaylists_異常系_APIエラー() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String query = "test query";
        int offset = 0;
        int limit = 20;
        SearchPlaylistsRequest.Builder builder = mock(SearchPlaylistsRequest.Builder.class);
        SearchPlaylistsRequest searchPlaylistsRequest = mock(SearchPlaylistsRequest.class);

        when(spotifyApi.searchPlaylists(query)).thenReturn(builder);
        when(builder.limit(limit)).thenReturn(builder);
        when(builder.offset(offset)).thenReturn(builder);
        when(builder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenThrow(new IOException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> playlistSearchService.searchPlaylists(query, offset, limit))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Spotifyプレイリストの検索中にエラーが発生しました。");
    }
}
