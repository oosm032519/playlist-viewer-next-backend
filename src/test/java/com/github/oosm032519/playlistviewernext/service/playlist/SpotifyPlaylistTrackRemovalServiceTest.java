package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
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
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistTrackRemovalServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private SpotifyPlaylistTrackRemovalService service;

    private PlaylistTrackRemovalRequest request;

    @BeforeEach
    void setUp() {
        request = new PlaylistTrackRemovalRequest();
        request.setPlaylistId("testPlaylistId");
        request.setTrackId("testTrackId");
    }

    @Test
    void removeTrackFromPlaylist_shouldRemoveTrackSuccessfully() throws Exception {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("spotify_access_token", "validAccessToken");
        when(principal.getAttributes()).thenReturn(attributes);

        SnapshotResult snapshotResult = mock(SnapshotResult.class);
        when(snapshotResult.getSnapshotId()).thenReturn("snapshotId");

        RemoveItemsFromPlaylistRequest removeRequest = mock(RemoveItemsFromPlaylistRequest.class);
        when(removeRequest.execute()).thenReturn(snapshotResult);

        RemoveItemsFromPlaylistRequest.Builder removeRequestBuilder = mock(RemoveItemsFromPlaylistRequest.Builder.class);
        when(removeRequestBuilder.build()).thenReturn(removeRequest);
        when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(removeRequestBuilder);

        // Act
        ResponseEntity<String> response = service.removeTrackFromPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("トラックが正常に削除されました。Snapshot ID: snapshotId");
    }

    @Test
    void removeTrackFromPlaylist_shouldThrowAuthenticationException_whenAccessTokenIsMissing() {
        // Arrange
        when(principal.getAttributes()).thenReturn(new HashMap<>());

        // Act & Assert
        assertThatThrownBy(() -> service.removeTrackFromPlaylist(request, principal))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("有効なアクセストークンがありません。");
    }

    @Test
    void removeTrackFromPlaylist_shouldThrowInternalServerException_onIOException() throws Exception {
        // Arrange
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("spotify_access_token", "validAccessToken");
        when(principal.getAttributes()).thenReturn(attributes);

        RemoveItemsFromPlaylistRequest removeRequest = mock(RemoveItemsFromPlaylistRequest.class);
        when(removeRequest.execute()).thenThrow(new IOException("IO error"));

        RemoveItemsFromPlaylistRequest.Builder removeRequestBuilder = mock(RemoveItemsFromPlaylistRequest.Builder.class);
        when(removeRequestBuilder.build()).thenReturn(removeRequest);
        when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(removeRequestBuilder);

        // Act & Assert
        assertThatThrownBy(() -> service.removeTrackFromPlaylist(request, principal))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("トラックの削除中にエラーが発生しました。");
    }
}
