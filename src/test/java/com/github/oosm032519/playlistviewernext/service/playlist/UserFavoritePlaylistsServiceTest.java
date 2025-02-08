package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.repository.UserFavoritePlaylistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserFavoritePlaylistsServiceTest {

    @Mock
    private UserFavoritePlaylistRepository userFavoritePlaylistRepository;

    private UserFavoritePlaylistsService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new UserFavoritePlaylistsService(userFavoritePlaylistRepository);
    }

    /**
     * ユーザーIDを指定して、お気に入りプレイリストのリストが正常に取得できることを確認する。
     */
    @Test
    void getFavoritePlaylists_Success() {
        // Arrange: テストデータの準備
        String userId = "testUser";
        LocalDateTime now = LocalDateTime.now();
        List<UserFavoritePlaylist> favoritePlaylists = Arrays.asList(
                createUserFavoritePlaylist(1L, userId, "1", "Playlist 1", 10, now, "Owner 1"),
                createUserFavoritePlaylist(2L, userId, "2", "Playlist 2", 15, now, "Owner 2")
        );
        when(userFavoritePlaylistRepository.findByUserId(userId)).thenReturn(favoritePlaylists);

        // Act: テスト対象メソッドの実行
        List<FavoritePlaylistResponse> result = service.getFavoritePlaylists(userId);

        // Assert: 結果の検証
        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getPlaylistId()).isEqualTo("1");
        assertThat(result.getFirst().getPlaylistName()).isEqualTo("Playlist 1");
        assertThat(result.getFirst().getPlaylistOwnerName()).isEqualTo("Owner 1");
        assertThat(result.get(0).getTotalTracks()).isEqualTo(10);
        assertThat(result.get(0).getAddedAt()).isEqualTo(now);

        assertThat(result.get(1).getPlaylistId()).isEqualTo("2");
        assertThat(result.get(1).getPlaylistName()).isEqualTo("Playlist 2");
        assertThat(result.get(1).getPlaylistOwnerName()).isEqualTo("Owner 2");
        assertThat(result.get(1).getTotalTracks()).isEqualTo(15);
        assertThat(result.get(1).getAddedAt()).isEqualTo(now);

        verify(userFavoritePlaylistRepository).findByUserId(userId);
    }

    // テストデータ作成用のヘルパーメソッド
    private UserFavoritePlaylist createUserFavoritePlaylist(Long id, String userId, String playlistId, String playlistName, int totalTracks, LocalDateTime addedAt, String playlistOwnerName) {
        UserFavoritePlaylist playlist = new UserFavoritePlaylist();
        playlist.setId(id);
        playlist.setUserId(userId);
        playlist.setPlaylistId(playlistId);
        playlist.setPlaylistName(playlistName);
        playlist.setTotalTracks(totalTracks);
        playlist.setAddedAt(addedAt);
        playlist.setPlaylistOwnerName(playlistOwnerName);
        return playlist;
    }

    /**
     * 指定したユーザーのお気に入りプレイリストが存在しない場合、空のリストが返されることを確認する。
     */
    @Test
    void getFavoritePlaylists_EmptyList() {
        // Arrange: テストデータの準備
        String userId = "testUser";
        when(userFavoritePlaylistRepository.findByUserId(userId)).thenReturn(List.of());

        // Act: テスト対象メソッドの実行
        List<FavoritePlaylistResponse> result = service.getFavoritePlaylists(userId);

        // Assert: 結果の検証
        assertThat(result).isEmpty();
        verify(userFavoritePlaylistRepository).findByUserId(userId);
    }

    /**
     * データベースアクセス中に例外が発生した場合、DatabaseAccessExceptionがスローされることを確認する。
     */
    @Test
    void getFavoritePlaylists_DatabaseAccessException() {
        // Arrange: モックの設定（例外をスロー）
        String userId = "testUser";
        when(userFavoritePlaylistRepository.findByUserId(userId)).thenThrow(new RuntimeException("Database error"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> service.getFavoritePlaylists(userId))
                .isInstanceOf(DatabaseAccessException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
                .hasMessage("お気に入りプレイリストの取得中にデータベースアクセスエラーが発生しました。");

        verify(userFavoritePlaylistRepository).findByUserId(userId);
    }
}
