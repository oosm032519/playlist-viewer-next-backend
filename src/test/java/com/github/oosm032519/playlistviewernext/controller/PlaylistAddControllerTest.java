package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.model.AddTrackRequest;
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
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import com.github.oosm032519.playlistviewernext.model.AddTrackRequest;

@ExtendWith(MockitoExtension.class)
class PlaylistAddControllerTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private AddItemsToPlaylistRequest.Builder addItemsToPlaylistRequestBuilder;

    @Mock
    private AddItemsToPlaylistRequest addItemsToPlaylistRequest;

    @Mock
    private SnapshotResult snapshotResult;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private PlaylistAddController playlistAddController;

    @Test
    void addTrackToPlaylist_成功時() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // Arrange
        String playlistId = "playlistId123";
        String trackId = "trackId456";
        String accessToken = "validAccessToken";
        String snapshotId = "snapshotId789";

        AddTrackRequest request = new AddTrackRequest();
        request.setPlaylistId(playlistId);
        request.setTrackId(trackId);

        // principalのモックを正しく設定
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("access_token", accessToken);
        when(principal.getAttributes()).thenReturn(attributes);

        when(spotifyApi.addItemsToPlaylist(anyString(), any(String[].class))).thenReturn(addItemsToPlaylistRequestBuilder);
        when(addItemsToPlaylistRequestBuilder.build()).thenReturn(addItemsToPlaylistRequest);
        when(addItemsToPlaylistRequest.execute()).thenReturn(snapshotResult);
        when(snapshotResult.getSnapshotId()).thenReturn(snapshotId);

        // Act
        ResponseEntity<String> response = playlistAddController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("トラックが正常に追加されました。Snapshot ID: " + snapshotId);

        verify(spotifyApi).setAccessToken(accessToken);
        verify(spotifyApi).addItemsToPlaylist(playlistId, new String[]{"spotify:track:" + trackId});
        verify(addItemsToPlaylistRequest).execute();
    }

    @Test
    void addTrackToPlaylist_認証されていない場合() {
        // Arrange
        AddTrackRequest request = new AddTrackRequest();

        // Act
        ResponseEntity<String> response = playlistAddController.addTrackToPlaylist(request, null);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("認証が必要です。");
    }

    @Test
    void addTrackToPlaylist_アクセストークンがない場合() {
        // Arrange
        AddTrackRequest request = new AddTrackRequest();
        request.setPlaylistId("playlistId123");
        request.setTrackId("trackId456");

        // principalのモックを設定（アクセストークンなし）
        when(principal.getAttributes()).thenReturn(new HashMap<>());

        // Act
        ResponseEntity<String> response = playlistAddController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("有効なアクセストークンがありません。");
    }


    @Test
    void addTrackToPlaylist_SpotifyAPIエラーの場合() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // Arrange
        String playlistId = "playlistId123";
        String trackId = "trackId456";
        String accessToken = "validAccessToken";

        AddTrackRequest request = new AddTrackRequest();
        request.setPlaylistId(playlistId);
        request.setTrackId(trackId);

        // principalのモックを正しく設定
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("access_token", accessToken);
        when(principal.getAttributes()).thenReturn(attributes);

        when(spotifyApi.addItemsToPlaylist(anyString(), any(String[].class))).thenReturn(addItemsToPlaylistRequestBuilder);
        when(addItemsToPlaylistRequestBuilder.build()).thenReturn(addItemsToPlaylistRequest);
        when(addItemsToPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API error"));

        // Act
        ResponseEntity<String> response = playlistAddController.addTrackToPlaylist(request, principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("エラー: Spotify API error");

        verify(spotifyApi).setAccessToken(accessToken);
        verify(spotifyApi).addItemsToPlaylist(playlistId, new String[]{"spotify:track:" + trackId});
        verify(addItemsToPlaylistRequest).execute();
    }
}
