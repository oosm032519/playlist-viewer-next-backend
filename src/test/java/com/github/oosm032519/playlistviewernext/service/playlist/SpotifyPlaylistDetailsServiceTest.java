package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
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
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    void testGetPlaylistTracks_異常系_プレイリストが存在しない() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        GetPlaylistRequest.Builder builder = mock(GetPlaylistRequest.Builder.class);
        GetPlaylistRequest getPlaylistRequest = mock(GetPlaylistRequest.class);

        when(spotifyApi.getPlaylist(NON_EXISTENT_PLAYLIST_ID)).thenReturn(builder);
        when(builder.build()).thenReturn(getPlaylistRequest);
        when(getPlaylistRequest.execute()).thenReturn(null);

        // Act & Assert
        assertThatThrownBy(() -> playlistDetailsService.getPlaylistTracks(NON_EXISTENT_PLAYLIST_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("指定されたプレイリストが見つかりません。");
    }
}
