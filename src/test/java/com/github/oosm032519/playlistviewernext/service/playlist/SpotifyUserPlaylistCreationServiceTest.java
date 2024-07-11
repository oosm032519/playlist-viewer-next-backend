// SpotifyUserPlaylistCreationServiceTest.java

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

    /**
     * プレイリストの作成が成功する場合のテスト
     */
    @Test
    void createPlaylist_SuccessfulCreation() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // モックの設定
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getId()).thenReturn(playlistId);
        when(spotifyApi.addItemsToPlaylist(anyString(), any(String[].class))).thenReturn(addItemsToPlaylistRequestBuilder);
        when(addItemsToPlaylistRequestBuilder.build()).thenReturn(addItemsToPlaylistRequest);

        // サービスメソッドの呼び出し
        String result = service.createPlaylist(accessToken, userId, playlistName, trackIds);

        // 結果の検証
        assertThat(result).isEqualTo(playlistId);
        verify(spotifyApi).setAccessToken(accessToken);
        verify(spotifyApi).createPlaylist(userId, playlistName);
        verify(createPlaylistRequestBuilder).public_(false);
        verify(createPlaylistRequest).execute();
        verify(spotifyApi).addItemsToPlaylist(eq(playlistId), any(String[].class));
        verify(addItemsToPlaylistRequest).execute();
    }

    /**
     * 空のトラックリストでプレイリストを作成する場合のテスト
     */
    @Test
    void createPlaylist_EmptyTrackList() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // モックの設定
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getId()).thenReturn(playlistId);

        // サービスメソッドの呼び出し
        String result = service.createPlaylist(accessToken, userId, playlistName, List.of());

        // 結果の検証
        assertThat(result).isEqualTo(playlistId);
        verify(spotifyApi, never()).addItemsToPlaylist(anyString(), any(String[].class));
    }

    /**
     * Spotify APIの例外が発生する場合のテスト
     */
    @Test
    void createPlaylist_SpotifyApiException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // モックの設定
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API error"));

        // 例外の検証
        assertThatThrownBy(() -> service.createPlaylist(accessToken, userId, playlistName, trackIds))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Spotify API error");
    }

    /**
     * ネットワークエラーが発生する場合のテスト
     */
    @Test
    void createPlaylist_IOException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // モックの設定
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenThrow(new IOException("Network error"));

        // 例外の検証
        assertThatThrownBy(() -> service.createPlaylist(accessToken, userId, playlistName, trackIds))
                .isInstanceOf(IOException.class)
                .hasMessage("Network error");
    }

    /**
     * パースエラーが発生する場合のテスト
     */
    @Test
    void createPlaylist_ParseException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // モックの設定
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenThrow(new org.apache.hc.core5.http.ParseException("Parse error"));

        // 例外の検証
        assertThatThrownBy(() -> service.createPlaylist(accessToken, userId, playlistName, trackIds))
                .isInstanceOf(org.apache.hc.core5.http.ParseException.class)
                .hasMessage("Parse error");
    }

    /**
     * トラックの追加に失敗する場合のテスト
     */
    @Test
    void createPlaylist_AddTracksException() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // モックの設定
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getId()).thenReturn(playlistId);
        when(spotifyApi.addItemsToPlaylist(anyString(), any(String[].class))).thenReturn(addItemsToPlaylistRequestBuilder);
        when(addItemsToPlaylistRequestBuilder.build()).thenReturn(addItemsToPlaylistRequest);
        when(addItemsToPlaylistRequest.execute()).thenThrow(new SpotifyWebApiException("Failed to add tracks"));

        // 例外の検証
        assertThatThrownBy(() -> service.createPlaylist(accessToken, userId, playlistName, trackIds))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Failed to add tracks");
    }
}
