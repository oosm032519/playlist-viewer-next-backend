package com.github.oosm032519.playlistviewernext.service.playlist;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistTrackAdditionServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private AddItemsToPlaylistRequest.Builder addItemsToPlaylistRequestBuilder;

    @Mock
    private AddItemsToPlaylistRequest addItemsToPlaylistRequest;

    @Mock
    private SnapshotResult snapshotResult;

    @InjectMocks
    private SpotifyPlaylistTrackAdditionService spotifyService;

    @Test
    void addTrackToPlaylist_成功時() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // Arrange
        String accessToken = "validAccessToken";
        String playlistId = "playlistId123";
        String trackId = "trackId456";
        String snapshotId = "snapshotId789";

        when(spotifyApi.addItemsToPlaylist(anyString(), any(String[].class))).thenReturn(addItemsToPlaylistRequestBuilder);
        when(addItemsToPlaylistRequestBuilder.build()).thenReturn(addItemsToPlaylistRequest);
        when(addItemsToPlaylistRequest.execute()).thenReturn(snapshotResult);
        when(snapshotResult.getSnapshotId()).thenReturn(snapshotId);

        // Act
        SnapshotResult result = spotifyService.addTrackToPlaylist(accessToken, playlistId, trackId);

        // Assert
        assertThat(result.getSnapshotId()).isEqualTo(snapshotId);

        verify(spotifyApi).setAccessToken(accessToken);
        verify(spotifyApi).addItemsToPlaylist(playlistId, new String[]{"spotify:track:" + trackId});
        verify(addItemsToPlaylistRequest).execute();
    }

    @Test
    void addTrackToPlaylist_SpotifyAPIエラーの場合() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // Arrange
        String accessToken = "validAccessToken";
        String playlistId = "playlistId123";
        String trackId = "trackId456";

        when(spotifyApi.addItemsToPlaylist(anyString(), any(String[].class))).thenReturn(addItemsToPlaylistRequestBuilder);
        when(addItemsToPlaylistRequestBuilder.build()).thenReturn(addItemsToPlaylistRequest);
        when(addItemsToPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API error"));

        // Act & Assert
        try {
            spotifyService.addTrackToPlaylist(accessToken, playlistId, trackId);
        } catch (SpotifyWebApiException e) {
            assertThat(e.getMessage()).isEqualTo("Spotify API error");
        }

        verify(spotifyApi).setAccessToken(accessToken);
        verify(spotifyApi).addItemsToPlaylist(playlistId, new String[]{"spotify:track:" + trackId});
        verify(addItemsToPlaylistRequest).execute();
    }
}
