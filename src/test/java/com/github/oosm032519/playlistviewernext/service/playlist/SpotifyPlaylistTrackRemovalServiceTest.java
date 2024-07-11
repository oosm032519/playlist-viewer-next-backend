// SpotifyPlaylistTrackRemovalServiceTest.java

package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * SpotifyPlaylistTrackRemovalServiceTest クラスは、SpotifyPlaylistTrackRemovalService のユニットテストを行います。
 */
@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistTrackRemovalServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private OAuth2User principal;

    @Mock
    private RemoveItemsFromPlaylistRequest.Builder builder;

    @Mock
    private RemoveItemsFromPlaylistRequest removeItemsFromPlaylistRequest;

    @InjectMocks
    private SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;

    private PlaylistTrackRemovalRequest playlistTrackRemovalRequest;

    /**
     * 各テストの前に実行されるセットアップメソッド。
     */
    @BeforeEach
    void setUp() {
        playlistTrackRemovalRequest = new PlaylistTrackRemovalRequest();
        playlistTrackRemovalRequest.setPlaylistId("playlistId");
        playlistTrackRemovalRequest.setTrackId("trackId");
    }

    /**
     * removeTrackFromPlaylist メソッドのテストを行うネストクラス。
     */
    @Nested
    @DisplayName("removeTrackFromPlaylist method tests")
    class RemoveTrackFromPlaylistTests {

        /**
         * アクセストークンが null の場合に UNAUTHORIZED を返すことをテストします。
         */
        @Test
        @DisplayName("Should return unauthorized when access token is null")
        void shouldReturnUnauthorizedWhenAccessTokenIsNull() {
            when(principal.getAttributes()).thenReturn(new HashMap<>());

            ResponseEntity<String> response = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isEqualTo("有効なアクセストークンがありません。");
        }

        /**
         * アクセストークンが空の場合に UNAUTHORIZED を返すことをテストします。
         */
        @Test
        @DisplayName("Should return unauthorized when access token is empty")
        void shouldReturnUnauthorizedWhenAccessTokenIsEmpty() {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("access_token", "");
            when(principal.getAttributes()).thenReturn(attributes);

            ResponseEntity<String> response = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isEqualTo("有効なアクセストークンがありません。");
        }

        /**
         * トラックが正常に削除されることをテストします。
         */
        @Test
        @DisplayName("Should remove track successfully")
        void shouldRemoveTrackSuccessfully() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("access_token", "validToken");
            when(principal.getAttributes()).thenReturn(attributes);

            when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(builder);
            when(builder.build()).thenReturn(removeItemsFromPlaylistRequest);
            SnapshotResult snapshotResult = mock(SnapshotResult.class);
            when(snapshotResult.getSnapshotId()).thenReturn("snapshotId");
            when(removeItemsFromPlaylistRequest.execute()).thenReturn(snapshotResult);

            ResponseEntity<String> response = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(response.getBody()).isEqualTo("トラックが正常に削除されました。Snapshot ID: snapshotId");

            verify(spotifyApi).setAccessToken("validToken");
            verify(spotifyApi).removeItemsFromPlaylist("playlistId", JsonParser.parseString("[{\"uri\":\"spotify:track:trackId\"}]").getAsJsonArray());
        }

        /**
         * IOException が発生した場合のエラーハンドリングをテストします。
         */
        @Test
        @DisplayName("Should handle IOException")
        void shouldHandleIOException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("access_token", "validToken");
            when(principal.getAttributes()).thenReturn(attributes);

            when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(builder);
            when(builder.build()).thenReturn(removeItemsFromPlaylistRequest);
            when(removeItemsFromPlaylistRequest.execute()).thenThrow(new IOException("IO Error"));

            ResponseEntity<String> response = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isEqualTo("エラー: IO Error");
        }

        /**
         * SpotifyWebApiException が発生した場合のエラーハンドリングをテストします。
         */
        @Test
        @DisplayName("Should handle SpotifyWebApiException")
        void shouldHandleSpotifyWebApiException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("access_token", "validToken");
            when(principal.getAttributes()).thenReturn(attributes);

            when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(builder);
            when(builder.build()).thenReturn(removeItemsFromPlaylistRequest);
            when(removeItemsFromPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API Error"));

            ResponseEntity<String> response = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isEqualTo("エラー: Spotify API Error");
        }

        /**
         * ParseException が発生した場合のエラーハンドリングをテストします。
         */
        @Test
        @DisplayName("Should handle ParseException")
        void shouldHandleParseException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
            Map<String, Object> attributes = new HashMap<>();
            attributes.put("access_token", "validToken");
            when(principal.getAttributes()).thenReturn(attributes);

            when(spotifyApi.removeItemsFromPlaylist(anyString(), any(JsonArray.class))).thenReturn(builder);
            when(builder.build()).thenReturn(removeItemsFromPlaylistRequest);
            when(removeItemsFromPlaylistRequest.execute()).thenThrow(new org.apache.hc.core5.http.ParseException("Parse Error"));

            ResponseEntity<String> response = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(playlistTrackRemovalRequest, principal);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
            assertThat(response.getBody()).isEqualTo("エラー: Parse Error");
        }
    }
}
