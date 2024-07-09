package com.github.oosm032519.playlistviewernext.service.playlist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyUserPlaylistCreationServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private CreatePlaylistRequest.Builder createPlaylistRequestBuilder;

    @Mock
    private CreatePlaylistRequest createPlaylistRequest;

    @Mock
    private AddItemsToPlaylistRequest.Builder addItemsToPlaylistRequestBuilder;

    @Mock
    private AddItemsToPlaylistRequest addItemsToPlaylistRequest;

    @Mock
    private Playlist playlist;

    @InjectMocks
    private SpotifyUserPlaylistCreationService service;

    private final String accessToken = "test_access_token";
    private final String userId = "test_user_id";
    private final String playlistName = "Test Playlist";
    private final List<String> trackIds = Arrays.asList("track1", "track2", "track3");
    private final String playlistId = "test_playlist_id";

    @Test
    void createPlaylist_SuccessfulCreation() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getId()).thenReturn(playlistId);
        when(spotifyApi.addItemsToPlaylist(anyString(), any(String[].class))).thenReturn(addItemsToPlaylistRequestBuilder);
        when(addItemsToPlaylistRequestBuilder.build()).thenReturn(addItemsToPlaylistRequest);

        String result = service.createPlaylist(accessToken, userId, playlistName, trackIds);

        assertThat(result).isEqualTo(playlistId);
        verify(spotifyApi).setAccessToken(accessToken);
        verify(spotifyApi).createPlaylist(userId, playlistName);
        verify(createPlaylistRequestBuilder).public_(false);
        verify(createPlaylistRequest).execute();
        verify(spotifyApi).addItemsToPlaylist(eq(playlistId), any(String[].class));
        verify(addItemsToPlaylistRequest).execute();
    }

    @Test
    void createPlaylist_EmptyTrackList() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getId()).thenReturn(playlistId);

        String result = service.createPlaylist(accessToken, userId, playlistName, List.of());

        assertThat(result).isEqualTo(playlistId);
        verify(spotifyApi, never()).addItemsToPlaylist(anyString(), any(String[].class));
    }

    @Test
    void createPlaylist_SpotifyApiException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API error"));

        assertThatThrownBy(() -> service.createPlaylist(accessToken, userId, playlistName, trackIds))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Spotify API error");
    }

    @Test
    void createPlaylist_IOException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenThrow(new IOException("Network error"));

        assertThatThrownBy(() -> service.createPlaylist(accessToken, userId, playlistName, trackIds))
                .isInstanceOf(IOException.class)
                .hasMessage("Network error");
    }

    @Test
    void createPlaylist_ParseException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenThrow(new org.apache.hc.core5.http.ParseException("Parse error"));

        assertThatThrownBy(() -> service.createPlaylist(accessToken, userId, playlistName, trackIds))
                .isInstanceOf(org.apache.hc.core5.http.ParseException.class)
                .hasMessage("Parse error");
    }

    @Test
    void createPlaylist_AddTracksException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getId()).thenReturn(playlistId);
        when(spotifyApi.addItemsToPlaylist(anyString(), any(String[].class))).thenReturn(addItemsToPlaylistRequestBuilder);
        when(addItemsToPlaylistRequestBuilder.build()).thenReturn(addItemsToPlaylistRequest);
        when(addItemsToPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Failed to add tracks"));

        assertThatThrownBy(() -> service.createPlaylist(accessToken, userId, playlistName, trackIds))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Failed to add tracks");
    }
}