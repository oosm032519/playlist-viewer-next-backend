package com.github.oosm032519.playlistviewernext.service;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistDetailsService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistDetailsServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @InjectMocks
    private SpotifyPlaylistDetailsService playlistDetailsService;

    @BeforeEach
    void setUp() {
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
        PlaylistTrack[] result = playlistDetailsService.getPlaylistTracks(playlistId);

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
        String result = playlistDetailsService.getPlaylistName(playlistId);

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
        User result = playlistDetailsService.getPlaylistOwner(playlistId);

        // Assert
        assertThat(result).isEqualTo(owner);
        assertThat(result.getId()).isEqualTo("owner-id");
        assertThat(result.getDisplayName()).isEqualTo("Owner Name");
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
        assertThatThrownBy(() -> playlistDetailsService.getPlaylistTracks(playlistId))
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
        assertThatThrownBy(() -> playlistDetailsService.getPlaylistName(playlistId))
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
        assertThatThrownBy(() -> playlistDetailsService.getPlaylistOwner(playlistId))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Playlist not found");
    }
}
