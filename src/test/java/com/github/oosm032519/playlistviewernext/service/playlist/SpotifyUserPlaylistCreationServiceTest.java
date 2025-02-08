package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.apache.hc.core5.http.ParseException;
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
import static org.mockito.Mockito.eq;
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
     * プレイリストが正常に作成され、トラックが追加されることを確認する。
     */
    @Test
    void createPlaylist_SuccessfulCreation() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange: モックの設定
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getId()).thenReturn(playlistId);
        when(spotifyApi.addItemsToPlaylist(anyString(), any(String[].class))).thenReturn(addItemsToPlaylistRequestBuilder);
        when(addItemsToPlaylistRequestBuilder.build()).thenReturn(addItemsToPlaylistRequest);

        // Act: テスト対象メソッドの実行
        String result = service.createPlaylist(accessToken, userId, playlistName, trackIds);

        // Assert: 結果の検証
        assertThat(result).isEqualTo(playlistId);
        verify(spotifyApi).setAccessToken(accessToken);
        verify(spotifyApi).createPlaylist(userId, playlistName);
        verify(createPlaylistRequestBuilder).public_(false);
        verify(createPlaylistRequest).execute();
        verify(spotifyApi).addItemsToPlaylist(eq(playlistId), any(String[].class));
        verify(addItemsToPlaylistRequest).execute();
    }

    /**
     * トラックリストが空の場合でも、プレイリストが正常に作成されることを確認する。
     */
    @Test
    void createPlaylist_EmptyTrackList() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange: モックの設定
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenReturn(playlist);
        when(playlist.getId()).thenReturn(playlistId);

        // Act: テスト対象メソッドの実行
        String result = service.createPlaylist(accessToken, userId, playlistName, List.of());

        // Assert: 結果の検証
        assertThat(result).isEqualTo(playlistId);
        verify(spotifyApi, never()).addItemsToPlaylist(anyString(), any(String[].class));
    }

    /**
     * プレイリスト作成中にIOExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void createPlaylist_IOException() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange: IOExceptionをスローするモック設定
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenThrow(new IOException("Network error"));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> service.createPlaylist(accessToken, userId, playlistName, trackIds))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("プレイリストの作成中にエラーが発生しました。")
                .hasCauseInstanceOf(IOException.class);
    }

    /**
     * プレイリスト作成中にParseExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void createPlaylist_ParseException() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange: ParseExceptionをスローするモック設定
        when(spotifyApi.createPlaylist(anyString(), anyString())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.public_(anyBoolean())).thenReturn(createPlaylistRequestBuilder);
        when(createPlaylistRequestBuilder.build()).thenReturn(createPlaylistRequest);
        when(createPlaylistRequest.execute()).thenThrow(new ParseException("Parse error"));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> service.createPlaylist(accessToken, userId, playlistName, trackIds))
                .isInstanceOf(InternalServerException.class)
                .hasMessage("プレイリストの作成中にエラーが発生しました。")
                .hasCauseInstanceOf(ParseException.class);
    }
}
