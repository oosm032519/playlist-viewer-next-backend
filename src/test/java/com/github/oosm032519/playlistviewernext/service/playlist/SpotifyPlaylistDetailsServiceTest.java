package com.github.oosm032519.playlistviewernext.service.playlist;

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

    private static final String PLAYLIST_ID = "test-playlist-id";
    private static final String NON_EXISTENT_PLAYLIST_ID = "non-existent-playlist-id";

    @BeforeEach
    void setUp() {
        // 各テストの前に実行されるセットアップメソッド
    }

    @Test
    void testGetPlaylistTracks_正常系() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);
        Playlist playlist = mock(Playlist.class);
        Paging<PlaylistTrack> playlistTrackPaging = mock(Paging.class);
        PlaylistTrack[] playlistTracks = new PlaylistTrack[]{mock(PlaylistTrack.class)};

        when(spotifyApi.getPlaylist(PLAYLIST_ID)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getTracks()).thenReturn(playlistTrackPaging);
        when(playlistTrackPaging.getItems()).thenReturn(playlistTracks);

        // Act
        PlaylistTrack[] result = playlistDetailsService.getPlaylistTracks(PLAYLIST_ID);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result[0]).isEqualTo(playlistTracks[0]);
    }

    @Test
    void testGetPlaylistName_正常系() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);
        Playlist playlist = mock(Playlist.class);

        when(spotifyApi.getPlaylist(PLAYLIST_ID)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getName()).thenReturn("Test Playlist");

        // Act
        String result = playlistDetailsService.getPlaylistName(PLAYLIST_ID);

        // Assert
        assertThat(result).isEqualTo("Test Playlist");
    }

    @Test
    void testGetPlaylistOwner_正常系() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);
        Playlist playlist = mock(Playlist.class);
        User owner = mock(User.class);

        when(spotifyApi.getPlaylist(PLAYLIST_ID)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getOwner()).thenReturn(owner);
        when(owner.getId()).thenReturn("owner-id");
        when(owner.getDisplayName()).thenReturn("Owner Name");

        // Act
        User result = playlistDetailsService.getPlaylistOwner(PLAYLIST_ID);

        // Assert
        assertThat(result).isEqualTo(owner);
        assertThat(result.getId()).isEqualTo("owner-id");
        assertThat(result.getDisplayName()).isEqualTo("Owner Name");
    }

    @Test
    void testGetPlaylistTracks_異常系_プレイリストが存在しない() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);

        when(spotifyApi.getPlaylist(NON_EXISTENT_PLAYLIST_ID)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Playlist not found"));

        // Act & Assert
        assertThatThrownBy(() -> playlistDetailsService.getPlaylistTracks(NON_EXISTENT_PLAYLIST_ID))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Playlist not found");
    }

    @Test
    void testGetPlaylistName_異常系_プレイリストが存在しない() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);

        when(spotifyApi.getPlaylist(NON_EXISTENT_PLAYLIST_ID)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Playlist not found"));

        // Act & Assert
        assertThatThrownBy(() -> playlistDetailsService.getPlaylistName(NON_EXISTENT_PLAYLIST_ID))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Playlist not found");
    }

    @Test
    void testGetPlaylistOwner_異常系_プレイリストが存在しない() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);

        when(spotifyApi.getPlaylist(NON_EXISTENT_PLAYLIST_ID)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Playlist not found"));

        // Act & Assert
        assertThatThrownBy(() -> playlistDetailsService.getPlaylistOwner(NON_EXISTENT_PLAYLIST_ID))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Playlist not found");
    }
}
