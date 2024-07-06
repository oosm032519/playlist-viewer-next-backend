package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private SpotifyAuthService authService;

    @InjectMocks
    private SpotifyPlaylistService playlistService;

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
        List<PlaylistSimplified> result = playlistService.searchPlaylists(query);

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
        List<PlaylistSimplified> result = playlistService.searchPlaylists(query);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetPlaylistTracks_正常系() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "test-playlist-id";
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);
        Playlist playlist = mock(Playlist.class);
        Paging<PlaylistTrack> playlistTrackPaging = mock(Paging.class);
        PlaylistTrack[] playlistTracks = new PlaylistTrack[]{mock(PlaylistTrack.class)};

        when(spotifyApi.getPlaylist(playlistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getTracks()).thenReturn(playlistTrackPaging);
        when(playlistTrackPaging.getItems()).thenReturn(playlistTracks);

        // Act
        PlaylistTrack[] result = playlistService.getPlaylistTracks(playlistId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(playlistTracks[0]);
    }

    @Test
    void testGetPlaylistName_正常系() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "test-playlist-id";
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);
        Playlist playlist = mock(Playlist.class);

        when(spotifyApi.getPlaylist(playlistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getName()).thenReturn("Test Playlist");

        // Act
        String result = playlistService.getPlaylistName(playlistId);

        // Assert
        assertThat(result).isEqualTo("Test Playlist");
    }

    @Test
    void testGetPlaylistOwner_正常系() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "test-playlist-id";
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);
        Playlist playlist = mock(Playlist.class);
        User owner = mock(User.class);

        when(spotifyApi.getPlaylist(playlistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn("owner-id");
        when(owner.getDisplayName()).thenReturn("Owner Name");

        // Act
        User result = playlistService.getPlaylistOwner(playlistId);

        // Assert
        assertThat(result).isEqualTo(owner);
        assertThat(result.getId()).isEqualTo("owner-id");
        assertThat(result.getDisplayName()).isEqualTo("Owner Name");
    }

    @Test
    void testGetCurrentUsersPlaylists_正常系_プレイリストあり() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        GetListOfCurrentUsersPlaylistsRequest.Builder builder = mock(GetListOfCurrentUsersPlaylistsRequest.Builder.class);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = mock(GetListOfCurrentUsersPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistsPaging = mock(Paging.class);
        PlaylistSimplified[] playlistSimplifieds = new PlaylistSimplified[]{mock(PlaylistSimplified.class)};

        doNothing().when(authService).setAccessToken(authentication);
        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(builder);
        when(builder.build()).thenReturn(playlistsRequest);
        when(playlistsRequest.execute()).thenReturn(playlistsPaging);
        when(playlistsPaging.getItems()).thenReturn(playlistSimplifieds);

        // Act
        List<PlaylistSimplified> result = playlistService.getCurrentUsersPlaylists(authentication);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(playlistSimplifieds[0]);
    }

    @Test
    void testGetCurrentUsersPlaylists_正常系_プレイリストなし() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        GetListOfCurrentUsersPlaylistsRequest.Builder builder = mock(GetListOfCurrentUsersPlaylistsRequest.Builder.class);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = mock(GetListOfCurrentUsersPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistsPaging = mock(Paging.class);

        doNothing().when(authService).setAccessToken(authentication);
        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(builder);
        when(builder.build()).thenReturn(playlistsRequest);
        when(playlistsRequest.execute()).thenReturn(playlistsPaging);
        when(playlistsPaging.getItems()).thenReturn(null);

        // Act
        List<PlaylistSimplified> result = playlistService.getCurrentUsersPlaylists(authentication);

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
        assertThatThrownBy(() -> playlistService.searchPlaylists(query))
                .isInstanceOf(IOException.class)
                .hasMessage("API error");
    }

    @Test
    void testGetPlaylistTracks_異常系_プレイリストが存在しない() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "non-existent-playlist-id";
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);

        when(spotifyApi.getPlaylist(playlistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Playlist not found"));

        // Act & Assert
        assertThatThrownBy(() -> playlistService.getPlaylistTracks(playlistId))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Playlist not found");
    }

    @Test
    void testGetPlaylistName_異常系_プレイリストが存在しない() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "non-existent-playlist-id";
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);

        when(spotifyApi.getPlaylist(playlistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Playlist not found"));

        // Act & Assert
        assertThatThrownBy(() -> playlistService.getPlaylistName(playlistId))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Playlist not found");
    }

    @Test
    void testGetPlaylistOwner_異常系_プレイリストが存在しない() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String playlistId = "non-existent-playlist-id";
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);

        when(spotifyApi.getPlaylist(playlistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Playlist not found"));

        // Act & Assert
        assertThatThrownBy(() -> playlistService.getPlaylistOwner(playlistId))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Playlist not found");
    }
}
