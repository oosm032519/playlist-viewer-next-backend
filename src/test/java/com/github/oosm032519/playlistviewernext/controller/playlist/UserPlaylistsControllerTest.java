package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistsService;
import org.junit.jupiter.api.BeforeEach;
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

    @BeforeEach
    void setUp() {
        // 各テストの前に実行される設定
    }

    @Test
    void getFollowedPlaylists_Success() throws SpotifyWebApiException {
        // テストデータの準備
        PlaylistSimplified playlist1 = mock(PlaylistSimplified.class);
        PlaylistSimplified playlist2 = mock(PlaylistSimplified.class);
        List<PlaylistSimplified> mockPlaylists = Arrays.asList(playlist1, playlist2);
        when(userPlaylistsService.getCurrentUsersPlaylists()).thenReturn(mockPlaylists);

        // メソッドの実行
        ResponseEntity<?> response = userPlaylistsController.getFollowedPlaylists();

        // 検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(mockPlaylists);
        verify(userPlaylistsService, times(1)).getCurrentUsersPlaylists();
    }

    // 追加のテストケース: 空のプレイリストリストを返す場合
    @Test
    void getFollowedPlaylists_EmptyList() throws SpotifyWebApiException {
        // 空のリストを返すようにモックを設定
        when(userPlaylistsService.getCurrentUsersPlaylists()).thenReturn(List.of());

        // メソッドの実行
        ResponseEntity<?> response = userPlaylistsController.getFollowedPlaylists();

        // 検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(List.of());
        verify(userPlaylistsService, times(1)).getCurrentUsersPlaylists();
    }
}
