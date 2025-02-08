package com.github.oosm032519.playlistviewernext.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserFavoritePlaylistTest {

    /**
     * UserFavoritePlaylistエンティティの各フィールドに値を設定し、
     * ゲッターメソッドで正しく値が取得できることを確認する。
     */
    @Test
    void testUserFavoritePlaylistCreation() {
        // Arrange: テストデータの準備
        UserFavoritePlaylist playlist = new UserFavoritePlaylist();
        LocalDateTime now = LocalDateTime.now();

        // Act: 各フィールドに値を設定
        playlist.setId(1L);
        playlist.setUserId("user123");
        playlist.setPlaylistId("playlist456");
        playlist.setPlaylistName("My Favorite Songs");
        playlist.setTotalTracks(20);
        playlist.setAddedAt(now);
        playlist.setPlaylistOwnerName("John Doe");

        // Assert: ゲッターメソッドで値が正しく取得できることを確認
        assertThat(playlist.getId()).isEqualTo(1L);
        assertThat(playlist.getUserId()).isEqualTo("user123");
        assertThat(playlist.getPlaylistId()).isEqualTo("playlist456");
        assertThat(playlist.getPlaylistName()).isEqualTo("My Favorite Songs");
        assertThat(playlist.getTotalTracks()).isEqualTo(20);
        assertThat(playlist.getAddedAt()).isEqualTo(now);
        assertThat(playlist.getPlaylistOwnerName()).isEqualTo("John Doe");
    }

    /**
     * UserFavoritePlaylistエンティティのaddedAtフィールドにデフォルト値が設定されることを確認する。
     */
    @Test
    void testDefaultAddedAtValue() {
        // Arrange: UserFavoritePlaylistエンティティのインスタンスを作成
        UserFavoritePlaylist playlist = new UserFavoritePlaylist();

        // Act & Assert: addedAtフィールドにデフォルト値が設定されていることを確認
        assertThat(playlist.getAddedAt()).isNotNull();
        assertThat(playlist.getAddedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
    }
}
