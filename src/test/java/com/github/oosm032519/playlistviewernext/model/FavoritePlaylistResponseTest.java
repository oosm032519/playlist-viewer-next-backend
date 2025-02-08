package com.github.oosm032519.playlistviewernext.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FavoritePlaylistResponseTest {

    /**
     * コンストラクタと全てのゲッターメソッドが正しく動作することを確認する。
     */
    @Test
    @DisplayName("コンストラクタと全てのゲッターのテスト")
    void testConstructorAndGetters() {
        // Arrange: テストデータの準備
        String playlistId = "playlist123";
        String playlistName = "My Favorite Songs";
        String playlistOwnerName = "John Doe";
        int totalTracks = 20;
        LocalDateTime addedAt = LocalDateTime.now();

        // Act: オブジェクトの生成
        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                playlistId, playlistName, playlistOwnerName, totalTracks, addedAt);

        // Assert: アサーション
        assertThat(response.getPlaylistId()).isEqualTo(playlistId);
        assertThat(response.getPlaylistName()).isEqualTo(playlistName);
        assertThat(response.getPlaylistOwnerName()).isEqualTo(playlistOwnerName);
        assertThat(response.getTotalTracks()).isEqualTo(totalTracks);
        assertThat(response.getAddedAt()).isEqualTo(addedAt);
    }

    /**
     * 全てのセッターメソッドが正しく動作することを確認する。
     */
    @Test
    @DisplayName("全てのセッターのテスト")
    void testSetters() {
        // Arrange: オブジェクトの生成
        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                null, null, null, 0, (LocalDateTime) null);

        // Act: 新しい値の設定
        String newPlaylistId = "newPlaylist456";
        String newPlaylistName = "Updated Playlist";
        String newPlaylistOwnerName = "Jane Smith";
        int newTotalTracks = 30;
        LocalDateTime newAddedAt = LocalDateTime.now();

        response.setPlaylistId(newPlaylistId);
        response.setPlaylistName(newPlaylistName);
        response.setPlaylistOwnerName(newPlaylistOwnerName);
        response.setTotalTracks(newTotalTracks);
        response.setAddedAt(newAddedAt);

        // Assert: アサーション
        assertThat(response.getPlaylistId()).isEqualTo(newPlaylistId);
        assertThat(response.getPlaylistName()).isEqualTo(newPlaylistName);
        assertThat(response.getPlaylistOwnerName()).isEqualTo(newPlaylistOwnerName);
        assertThat(response.getTotalTracks()).isEqualTo(newTotalTracks);
        assertThat(response.getAddedAt()).isEqualTo(newAddedAt);
    }

    /**
     * totalTracksフィールドの境界値テストを行う。
     */
    @Test
    @DisplayName("totalTracksの境界値テスト")
    void testTotalTracksBoundaries() {
        // Arrange: オブジェクトの生成
        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                "id", "name", "owner", 0, LocalDateTime.now());

        // Act & Assert: 最小値（0）のテスト
        assertThat(response.getTotalTracks()).isZero();

        // Act & Assert: 大きな値のテスト
        response.setTotalTracks(Integer.MAX_VALUE);
        assertThat(response.getTotalTracks()).isEqualTo(Integer.MAX_VALUE);

        // Act & Assert: 負の値のテスト
        response.setTotalTracks(-1);
        assertThat(response.getTotalTracks()).isNegative();
    }

    /**
     * nullの許容性テストを行う。
     */
    @Test
    @DisplayName("nullの許容性テスト")
    void testNullability() {
        // Arrange & Act: オブジェクトの生成（nullを許容するコンストラクタを使用）
        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                null, null, null, 0, (LocalDateTime) null);

        // Assert: 各フィールドがnullであることを確認
        assertThat(response.getPlaylistId()).isNull();
        assertThat(response.getPlaylistName()).isNull();
        assertThat(response.getPlaylistOwnerName()).isNull();
        assertThat(response.getAddedAt()).isNull(); // LocalDateTimeはnullにできないので注意
    }
}
