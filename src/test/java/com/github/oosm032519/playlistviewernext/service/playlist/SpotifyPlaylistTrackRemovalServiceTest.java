package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.google.gson.JsonArray;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistTrackRemovalServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private RemoveItemsFromPlaylistRequest.Builder removeItemsFromPlaylistRequestBuilder;

    @Mock
    private RemoveItemsFromPlaylistRequest removeItemsFromPlaylistRequest;

    @InjectMocks
    private SpotifyPlaylistTrackRemovalService service;

    private PlaylistTrackRemovalRequest request;
    private String accessToken;

    @BeforeEach
    void setUp() {
        request = new PlaylistTrackRemovalRequest();
        request.setPlaylistId("playlistId");
        request.setTrackId("trackId");
        accessToken = "validAccessToken";
    }

    @Test
    void removeTrackFromPlaylist_WithNullAccessToken_ReturnsUnauthorized() {
        ResponseEntity<String> response = service.removeTrackFromPlaylist(request, null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("有効なアクセストークンがありません。");
    }

    @Test
    void removeTrackFromPlaylist_SuccessfulRemoval_ReturnsOk() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        SnapshotResult snapshotResult = mock(SnapshotResult.class);
        when(snapshotResult.getSnapshotId()).thenReturn("snapshotId");

        when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(removeItemsFromPlaylistRequestBuilder);
        when(removeItemsFromPlaylistRequestBuilder.build()).thenReturn(removeItemsFromPlaylistRequest);
        when(removeItemsFromPlaylistRequest.execute()).thenReturn(snapshotResult);

        ResponseEntity<String> response = service.removeTrackFromPlaylist(request, accessToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("トラックが正常に削除されました。Snapshot ID: snapshotId");
        verify(spotifyApi).setAccessToken(accessToken);
    }

    @Test
    void removeTrackFromPlaylist_SpotifyApiException_ReturnsInternalServerError() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(removeItemsFromPlaylistRequestBuilder);
        when(removeItemsFromPlaylistRequestBuilder.build()).thenReturn(removeItemsFromPlaylistRequest);
        when(removeItemsFromPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API error"));

        ResponseEntity<String> response = service.removeTrackFromPlaylist(request, accessToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("エラー: Spotify API error");
        verify(spotifyApi).setAccessToken(accessToken);
    }

    @Test
    void removeTrackFromPlaylist_IOException_ReturnsInternalServerError() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(removeItemsFromPlaylistRequestBuilder);
        when(removeItemsFromPlaylistRequestBuilder.build()).thenReturn(removeItemsFromPlaylistRequest);
        when(removeItemsFromPlaylistRequest.execute()).thenThrow(new IOException("IO error"));

        ResponseEntity<String> response = service.removeTrackFromPlaylist(request, accessToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("エラー: IO error");
        verify(spotifyApi).setAccessToken(accessToken);
    }

    @Test
    void removeTrackFromPlaylist_ParseException_ReturnsInternalServerError() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(removeItemsFromPlaylistRequestBuilder);
        when(removeItemsFromPlaylistRequestBuilder.build()).thenReturn(removeItemsFromPlaylistRequest);
        when(removeItemsFromPlaylistRequest.execute()).thenThrow(new org.apache.hc.core5.http.ParseException("Parse error"));

        ResponseEntity<String> response = service.removeTrackFromPlaylist(request, accessToken);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("エラー: Parse error");
        verify(spotifyApi).setAccessToken(accessToken);
    }

    @Test
    void removeTrackFromPlaylist_VerifyJsonArrayCreation() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        SnapshotResult snapshotResult = mock(SnapshotResult.class);
        when(snapshotResult.getSnapshotId()).thenReturn("snapshotId");

        when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(removeItemsFromPlaylistRequestBuilder);
        when(removeItemsFromPlaylistRequestBuilder.build()).thenReturn(removeItemsFromPlaylistRequest);
        when(removeItemsFromPlaylistRequest.execute()).thenReturn(snapshotResult);

        service.removeTrackFromPlaylist(request, accessToken);

        verify(spotifyApi).removeItemsFromPlaylist(eq("playlistId"), argThat(jsonArray ->
                jsonArray.size() == 1 &&
                        jsonArray.get(0).getAsJsonObject().get("uri").getAsString().equals("spotify:track:trackId")
        ));
    }
}
