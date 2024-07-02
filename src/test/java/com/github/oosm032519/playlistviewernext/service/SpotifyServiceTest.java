package com.github.oosm032519.playlistviewernext.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.miscellaneous.PlaylistTracksInformation;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    private SpotifyService spotifyService;

    @BeforeEach
    void setUp() {
        spotifyService = new SpotifyService(spotifyApi, authorizedClientService);
    }

    @Test
    void testGetClientCredentialsToken() throws Exception {
        // Arrange
        ClientCredentialsRequest.Builder builder = mock(ClientCredentialsRequest.Builder.class);
        ClientCredentialsRequest clientCredentialsRequest = mock(ClientCredentialsRequest.class);
        ClientCredentials clientCredentials = mock(ClientCredentials.class);
        when(spotifyApi.clientCredentials()).thenReturn(builder);
        when(builder.build()).thenReturn(clientCredentialsRequest);
        when(clientCredentialsRequest.execute()).thenReturn(clientCredentials);
        when(clientCredentials.getAccessToken()).thenReturn("test-access-token");

        // Act
        spotifyService.getClientCredentialsToken();

        // Assert
        verify(spotifyApi).setAccessToken("test-access-token");
    }

    @Test
    void testSearchPlaylists() throws Exception {
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
        List<PlaylistSimplified> result = spotifyService.searchPlaylists(query);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(playlistSimplifieds[0]);
    }

    @Test
    void testGetPlaylistTracks() throws Exception {
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
        PlaylistTrack[] result = spotifyService.getPlaylistTracks(playlistId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(playlistTracks[0]);
    }

    @Test
    void testGetCurrentUsersPlaylists() throws Exception {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        GetListOfCurrentUsersPlaylistsRequest.Builder builder = mock(GetListOfCurrentUsersPlaylistsRequest.Builder.class);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = mock(GetListOfCurrentUsersPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistsPaging = mock(Paging.class);
        PlaylistSimplified[] playlistSimplifieds = new PlaylistSimplified[]{mock(PlaylistSimplified.class)};

        when(authentication.getName()).thenReturn("test-user");
        when(authorizedClientService.loadAuthorizedClient(eq("spotify"), eq("test-user")))
                .thenReturn(mock(OAuth2AuthorizedClient.class));
        when(authorizedClientService.loadAuthorizedClient("spotify", "test-user").getAccessToken())
                .thenReturn(mock(OAuth2AccessToken.class));
        when(authorizedClientService.loadAuthorizedClient("spotify", "test-user").getAccessToken().getTokenValue())
                .thenReturn("test-access-token");

        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(builder);
        when(builder.build()).thenReturn(playlistsRequest);
        when(playlistsRequest.execute()).thenReturn(playlistsPaging);
        when(playlistsPaging.getItems()).thenReturn(playlistSimplifieds);

        // Act
        List<PlaylistSimplified> result = spotifyService.getCurrentUsersPlaylists(authentication);

        // Assert
        verify(spotifyApi).setAccessToken("test-access-token");
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(playlistSimplifieds[0]);
    }
}
