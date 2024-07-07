package com.github.oosm032519.playlistviewernext.service;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.service.playlist.PlaylistDetailsRetrievalService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistDetailsService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyTrackService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.model_objects.specification.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistDetailsRetrievalServiceTest {

    @Mock
    private SpotifyPlaylistDetailsService playlistDetailsService;

    @Mock
    private SpotifyTrackService trackService;

    @Mock
    private SpotifyClientCredentialsAuthentication authController;

    @InjectMocks
    private PlaylistDetailsRetrievalService playlistDetailsRetrievalService;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void getPlaylistDetails_ReturnsDetailsSuccessfully() throws Exception {
        // Given
        String playlistId = "testPlaylistId";
        PlaylistTrack[] tracks = new PlaylistTrack[]{
                new PlaylistTrack.Builder().setTrack(new Track.Builder().setId("track1").build()).build(),
                new PlaylistTrack.Builder().setTrack(new Track.Builder().setId("track2").build()).build()
        };
        String playlistName = "Test Playlist";
        User owner = new User.Builder().setId("ownerId").setDisplayName("Owner Name").build();

        when(playlistDetailsService.getPlaylistTracks(playlistId)).thenReturn(tracks);
        when(playlistDetailsService.getPlaylistName(playlistId)).thenReturn(playlistName);
        when(playlistDetailsService.getPlaylistOwner(playlistId)).thenReturn(owner);
        when(trackService.getAudioFeaturesForTrack(anyString())).thenReturn(new AudioFeatures.Builder().build());

        // When
        Map<String, Object> response = playlistDetailsRetrievalService.getPlaylistDetails(playlistId);

        // Then
        assertThat(response.get("tracks")).isInstanceOf(Map.class);
        assertThat(response.get("playlistName")).isEqualTo(playlistName);
        assertThat(response.get("ownerId")).isEqualTo(owner.getId());
        assertThat(response.get("ownerName")).isEqualTo(owner.getDisplayName());

        verify(playlistDetailsService).getPlaylistTracks(playlistId);
        verify(playlistDetailsService).getPlaylistName(playlistId);
        verify(playlistDetailsService).getPlaylistOwner(playlistId);
        verify(trackService, times(tracks.length)).getAudioFeaturesForTrack(anyString());
    }
}
