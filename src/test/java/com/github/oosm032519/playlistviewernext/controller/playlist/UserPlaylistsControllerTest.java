package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserPlaylistsControllerTest {

    @Mock
    private SpotifyUserPlaylistsService userPlaylistsService;

    @InjectMocks
    private UserPlaylistsController userPlaylistsController;

    /**
     * フォロー中のプレイリストが正常に取得できることを確認する。
     */
    @Test
    void getFollowedPlaylists_Success() throws SpotifyWebApiException {
        // Arrange: テストデータの準備
        PlaylistSimplified playlist1 = mock(PlaylistSimplified.class);
        PlaylistSimplified playlist2 = mock(PlaylistSimplified.class);
        List<PlaylistSimplified> mockPlaylists = Arrays.asList(playlist1, playlist2);
        when(userPlaylistsService.getCurrentUsersPlaylists()).thenReturn(mockPlaylists);

        // Act: メソッドの実行
        ResponseEntity<?> response = userPlaylistsController.getFollowedPlaylists();

        // Assert: 検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockPlaylists);
        verify(userPlaylistsService, times(1)).getCurrentUsersPlaylists();
    }

    /**
     * フォロー中のプレイリストが空の場合に、空のリストが返されることを確認する。
     */
    @Test
    void getFollowedPlaylists_EmptyList() throws SpotifyWebApiException {
        // Arrange: 空のリストを返すようにモックを設定
        when(userPlaylistsService.getCurrentUsersPlaylists()).thenReturn(List.of());

        // Act: メソッドの実行
        ResponseEntity<?> response = userPlaylistsController.getFollowedPlaylists();

        // Assert: 検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(List.of());
        verify(userPlaylistsService, times(1)).getCurrentUsersPlaylists();
    }
}
