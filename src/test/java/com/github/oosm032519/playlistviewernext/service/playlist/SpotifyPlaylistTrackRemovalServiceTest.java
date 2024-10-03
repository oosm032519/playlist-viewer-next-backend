package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistTrackRemovalServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private OAuth2User principal;

    @Mock
    private RemoveItemsFromPlaylistRequest.Builder removeItemsBuilder;

    @Mock
    private RemoveItemsFromPlaylistRequest removeItemsRequest;

    @InjectMocks
    private SpotifyPlaylistTrackRemovalService service;

    private PlaylistTrackRemovalRequest request;

    @BeforeEach
    void setUp() {
        request = new PlaylistTrackRemovalRequest();
        request.setPlaylistId("playlistId");
        request.setTrackId("trackId");
    }

    @Test
    void removeTrackFromPlaylist_Success() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("spotify_access_token", "validToken");
        when(principal.getAttributes()).thenReturn(attributes);

        SnapshotResult snapshotResult = mock(SnapshotResult.class);
        when(snapshotResult.getSnapshotId()).thenReturn("snapshotId");

        when(spotifyApi.removeItemsFromPlaylist(eq("playlistId"), any(JsonArray.class))).thenReturn(removeItemsBuilder);
        when(removeItemsBuilder.build()).thenReturn(removeItemsRequest);
        when(removeItemsRequest.execute()).thenReturn(snapshotResult);

        ResponseEntity<String> response = service.removeTrackFromPlaylist(request, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("トラックが正常に削除されました。Snapshot ID: snapshotId");

        verify(spotifyApi).setAccessToken("validToken");
        verify(spotifyApi).removeItemsFromPlaylist(eq("playlistId"), any(JsonArray.class));
        verify(removeItemsRequest).execute();
    }

    @Test
    void removeTrackFromPlaylist_UnauthorizedAccess() {
        when(principal.getAttributes()).thenReturn(new HashMap<>());

        AuthenticationException exception = assertThrows(AuthenticationException.class,
                () -> service.removeTrackFromPlaylist(request, principal));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(exception.getMessage()).isEqualTo("有効なアクセストークンがありません。");

        verify(spotifyApi, never()).setAccessToken(any());
        verify(spotifyApi, never()).removeItemsFromPlaylist(any(), any());
    }

    @Test
    void removeTrackFromPlaylist_SpotifyApiException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("spotify_access_token", "validToken");
        when(principal.getAttributes()).thenReturn(attributes);

        when(spotifyApi.removeItemsFromPlaylist(eq("playlistId"), any(JsonArray.class))).thenReturn(removeItemsBuilder);
        when(removeItemsBuilder.build()).thenReturn(removeItemsRequest);
        when(removeItemsRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API error"));

        SpotifyApiException exception = assertThrows(SpotifyApiException.class,
                () -> service.removeTrackFromPlaylist(request, principal));

        assertThat(exception.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(exception.getMessage()).isEqualTo("トラックの削除中にエラーが発生しました。");

        verify(spotifyApi).setAccessToken("validToken");
        verify(spotifyApi).removeItemsFromPlaylist(eq("playlistId"), any(JsonArray.class));
        verify(removeItemsRequest).execute();
    }
}
