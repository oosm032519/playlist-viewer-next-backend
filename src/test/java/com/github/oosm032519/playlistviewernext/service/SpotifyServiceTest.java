package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @InjectMocks
    private SpotifyService spotifyService;

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetClientCredentialsToken_正常系() throws IOException, SpotifyWebApiException, ParseException {
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
    void testGetClientCredentialsToken_異常系_APIエラー() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        ClientCredentialsRequest.Builder builder = mock(ClientCredentialsRequest.Builder.class);
        ClientCredentialsRequest clientCredentialsRequest = mock(ClientCredentialsRequest.class);
        when(spotifyApi.clientCredentials()).thenReturn(builder);
        when(builder.build()).thenReturn(clientCredentialsRequest);
        when(clientCredentialsRequest.execute()).thenThrow(new IOException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyService.getClientCredentialsToken())
                .isInstanceOf(IOException.class)
                .hasMessage("API error");
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
        List<PlaylistSimplified> result = spotifyService.searchPlaylists(query);

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
        List<PlaylistSimplified> result = spotifyService.searchPlaylists(query);

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
        PlaylistTrack[] result = spotifyService.getPlaylistTracks(playlistId);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(playlistTracks[0]);
    }

    @Test
    void testGetArtistGenres_正常系_ジャンルあり() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String artistId = "test-artist-id";
        GetArtistRequest.Builder builder = mock(GetArtistRequest.Builder.class);
        GetArtistRequest getArtistRequest = mock(GetArtistRequest.class);
        Artist artist = mock(Artist.class);
        String[] genres = new String[]{"pop", "rock"};

        when(spotifyApi.getArtist(artistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getArtistRequest);
        when(getArtistRequest.execute()).thenReturn(artist);
        when(artist.getGenres()).thenReturn(genres);
        when(artist.getName()).thenReturn("Test Artist");

        // Act
        List<String> result = spotifyService.getArtistGenres(artistId);

        // Assert
        assertThat(result).containsExactly("pop", "rock");
    }

    @Test
    void testGetArtistGenres_正常系_ジャンルなし() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String artistId = "test-artist-id";
        GetArtistRequest.Builder builder = mock(GetArtistRequest.Builder.class);
        GetArtistRequest getArtistRequest = mock(GetArtistRequest.class);
        Artist artist = mock(Artist.class);

        when(spotifyApi.getArtist(artistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getArtistRequest);
        when(getArtistRequest.execute()).thenReturn(artist);
        when(artist.getGenres()).thenReturn(null);
        when(artist.getName()).thenReturn("Test Artist");

        // Act
        List<String> result = spotifyService.getArtistGenres(artistId);

        // Assert
        assertThat(result).isEmpty();
    }


    @Test
    void testGetAudioFeaturesForTrack_正常系() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String trackId = "test-track-id";
        GetAudioFeaturesForTrackRequest.Builder builder = mock(GetAudioFeaturesForTrackRequest.Builder.class);
        GetAudioFeaturesForTrackRequest getAudioFeaturesRequest = mock(GetAudioFeaturesForTrackRequest.class);
        AudioFeatures audioFeatures = mock(AudioFeatures.class);

        when(spotifyApi.getAudioFeaturesForTrack(trackId)).thenReturn(builder);
        when(builder.build()).thenReturn(getAudioFeaturesRequest);
        when(getAudioFeaturesRequest.execute()).thenReturn(audioFeatures);

        // Act
        AudioFeatures result = spotifyService.getAudioFeaturesForTrack(trackId);

        // Assert
        assertThat(result).isEqualTo(audioFeatures);
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
        String result = spotifyService.getPlaylistName(playlistId);

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
        User result = spotifyService.getPlaylistOwner(playlistId);

        // Assert
        assertThat(result).isEqualTo(owner);
        assertThat(result.getId()).isEqualTo("owner-id");
        assertThat(result.getDisplayName()).isEqualTo("Owner Name");
    }

    @Test
    void testGetCurrentUsersPlaylists_正常系_プレイリストあり() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        GetListOfCurrentUsersPlaylistsRequest.Builder builder = mock(GetListOfCurrentUsersPlaylistsRequest.Builder.class);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = mock(GetListOfCurrentUsersPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistsPaging = mock(Paging.class);
        PlaylistSimplified[] playlistSimplifieds = new PlaylistSimplified[]{mock(PlaylistSimplified.class)};

        when(authentication.getName()).thenReturn("test-user");
        when(authorizedClientService.loadAuthorizedClient("spotify", "test-user")).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("test-access-token");

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

    @Test
    void testGetCurrentUsersPlaylists_正常系_プレイリストなし() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);
        GetListOfCurrentUsersPlaylistsRequest.Builder builder = mock(GetListOfCurrentUsersPlaylistsRequest.Builder.class);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = mock(GetListOfCurrentUsersPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistsPaging = mock(Paging.class);

        when(authentication.getName()).thenReturn("test-user");
        when(authorizedClientService.loadAuthorizedClient("spotify", "test-user")).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("test-access-token");

        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(builder);
        when(builder.build()).thenReturn(playlistsRequest);
        when(playlistsRequest.execute()).thenReturn(playlistsPaging);
        when(playlistsPaging.getItems()).thenReturn(null);

        // Act
        List<PlaylistSimplified> result = spotifyService.getCurrentUsersPlaylists(authentication);

        // Assert
        verify(spotifyApi).setAccessToken("test-access-token");
        assertThat(result).isEmpty();
    }

    @Test
    void testSetAccessToken_正常系() {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        OAuth2AuthorizedClient authorizedClient = mock(OAuth2AuthorizedClient.class);
        OAuth2AccessToken accessToken = mock(OAuth2AccessToken.class);

        when(authentication.getName()).thenReturn("test-user");
        when(authorizedClientService.loadAuthorizedClient("spotify", "test-user")).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("test-access-token");

        // Act
        spotifyService.setAccessToken(authentication);

        // Assert
        verify(spotifyApi).setAccessToken("test-access-token");
    }

    @Test
    void testGetClientCredentialsToken_異常系_SpotifyWebApiException() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        ClientCredentialsRequest.Builder builder = mock(ClientCredentialsRequest.Builder.class);
        ClientCredentialsRequest clientCredentialsRequest = mock(ClientCredentialsRequest.class);
        when(spotifyApi.clientCredentials()).thenReturn(builder);
        when(builder.build()).thenReturn(clientCredentialsRequest);
        when(clientCredentialsRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API error"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyService.getClientCredentialsToken())
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Spotify API error");
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
        assertThatThrownBy(() -> spotifyService.searchPlaylists(query))
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
        assertThatThrownBy(() -> spotifyService.getPlaylistTracks(playlistId))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Playlist not found");
    }

    @Test
    void testGetArtistGenres_異常系_アーティストが存在しない() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String artistId = "non-existent-artist-id";
        GetArtistRequest.Builder builder = mock(GetArtistRequest.Builder.class);
        GetArtistRequest getArtistRequest = mock(GetArtistRequest.class);

        when(spotifyApi.getArtist(artistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getArtistRequest);
        when(getArtistRequest.execute()).thenThrow(new SpotifyWebApiException("Artist not found"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyService.getArtistGenres(artistId))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Artist not found");
    }

    @Test
    void testGetAudioFeaturesForTrack_異常系_トラックが存在しない() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String trackId = "non-existent-track-id";
        GetAudioFeaturesForTrackRequest.Builder builder = mock(GetAudioFeaturesForTrackRequest.Builder.class);
        GetAudioFeaturesForTrackRequest getAudioFeaturesRequest = mock(GetAudioFeaturesForTrackRequest.class);

        when(spotifyApi.getAudioFeaturesForTrack(trackId)).thenReturn(builder);
        when(builder.build()).thenReturn(getAudioFeaturesRequest);
        when(getAudioFeaturesRequest.execute()).thenThrow(new SpotifyWebApiException("Track not found"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyService.getAudioFeaturesForTrack(trackId))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Track not found");
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
        assertThatThrownBy(() -> spotifyService.getPlaylistName(playlistId))
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
        assertThatThrownBy(() -> spotifyService.getPlaylistOwner(playlistId))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Playlist not found");
    }

    @Test
    void testGetCurrentUsersPlaylists_異常系_認証エラー() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        when(authentication.getName()).thenReturn("test-user");
        when(authorizedClientService.loadAuthorizedClient("spotify", "test-user")).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> spotifyService.getCurrentUsersPlaylists(authentication))
                .isInstanceOf(NullPointerException.class);
    }
}
