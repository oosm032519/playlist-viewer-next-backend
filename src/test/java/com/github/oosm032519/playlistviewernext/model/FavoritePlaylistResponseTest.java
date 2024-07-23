package com.github.oosm032519.playlistviewernext.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FavoritePlaylistResponseTest {

    @Test
    @DisplayName("コンストラクタとゲッターのテスト")
    void testConstructorAndGetters() {
        // テストデータの準備
        String playlistId = "playlist123";
        String playlistName = "My Favorite Songs";
        String playlistOwnerName = "John Doe";
        int totalTracks = 20;
        LocalDateTime addedAt = LocalDateTime.of(2024, 7, 23, 10, 30);

        // FavoritePlaylistResponseオブジェクトの作成
        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                playlistId, playlistName, playlistOwnerName, totalTracks, addedAt);

        // アサーション
        assertThat(response)
                .extracting(
                        FavoritePlaylistResponse::getPlaylistId,
                        FavoritePlaylistResponse::getPlaylistName,
                        FavoritePlaylistResponse::getPlaylistOwnerName,
                        FavoritePlaylistResponse::getTotalTracks,
                        FavoritePlaylistResponse::getAddedAt
                )
                .containsExactly(
                        playlistId,
                        playlistName,
                        playlistOwnerName,
                        totalTracks,
                        addedAt
                );
    }

    @Test
    @DisplayName("セッターのテスト")
    void testSetters() {
        // FavoritePlaylistResponseオブジェクトの作成（初期値は重要ではない）
        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                "initial", "initial", "initial", 0, LocalDateTime.now());

        // 新しい値の設定
        String newPlaylistId = "newPlaylist456";
        String newPlaylistName = "Updated Playlist";
        String newPlaylistOwnerName = "Jane Smith";
        int newTotalTracks = 30;
        LocalDateTime newAddedAt = LocalDateTime.of(2024, 7, 24, 15, 45);

        response.setPlaylistId(newPlaylistId);
        response.setPlaylistName(newPlaylistName);
        response.setPlaylistOwnerName(newPlaylistOwnerName);
        response.setTotalTracks(newTotalTracks);
        response.setAddedAt(newAddedAt);

        // アサーション
        assertThat(response)
                .extracting(
                        FavoritePlaylistResponse::getPlaylistId,
                        FavoritePlaylistResponse::getPlaylistName,
                        FavoritePlaylistResponse::getPlaylistOwnerName,
                        FavoritePlaylistResponse::getTotalTracks,
                        FavoritePlaylistResponse::getAddedAt
                )
                .containsExactly(
                        newPlaylistId,
                        newPlaylistName,
                        newPlaylistOwnerName,
                        newTotalTracks,
                        newAddedAt
                );
    }

    @Test
    @DisplayName("空の文字列を設定できることのテスト")
    void testEmptyStringValues() {
        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                "", "", "", 0, LocalDateTime.now());

        assertThat(response.getPlaylistId()).isEmpty();
        assertThat(response.getPlaylistName()).isEmpty();
        assertThat(response.getPlaylistOwnerName()).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 100, Integer.MAX_VALUE})
    @DisplayName("様々な曲数でのテスト")
    void testVariousTotalTracks(int totalTracks) {
        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                "id", "name", "owner", totalTracks, LocalDateTime.now());

        assertThat(response.getTotalTracks()).isEqualTo(totalTracks);
    }

    @Test
    @DisplayName("nullの日時を設定できることのテスト")
    void testNullDateTime() {
        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                "id", "name", "owner", 10, null);

        assertThat(response.getAddedAt()).isNull();
    }

    @Test
    @DisplayName("オブジェクトの等価性テスト")
    void testEquality() {
        LocalDateTime now = LocalDateTime.now();
        FavoritePlaylistResponse response1 = new FavoritePlaylistResponse(
                "id", "name", "owner", 10, now);
        FavoritePlaylistResponse response2 = new FavoritePlaylistResponse(
                "id", "name", "owner", 10, now);

        assertThat(response1).isEqualTo(response2);
        assertThat(response1.hashCode()).isEqualTo(response2.hashCode());
    }

    @Test
    @DisplayName("オブジェクトの不等価性テスト")
    void testInequality() {
        LocalDateTime now = LocalDateTime.now();
        FavoritePlaylistResponse response1 = new FavoritePlaylistResponse(
                "id1", "name1", "owner1", 10, now);
        FavoritePlaylistResponse response2 = new FavoritePlaylistResponse(
                "id2", "name2", "owner2", 20, now.plusDays(1));

        assertThat(response1).isNotEqualTo(response2);
    }

    @Test
    @DisplayName("toString()メソッドのテスト")
    void testToString() {
        LocalDateTime now = LocalDateTime.of(2024, 7, 23, 10, 30);
        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                "id", "name", "owner", 10, now);

        String expectedString = "FavoritePlaylistResponse(playlistId=id, playlistName=name, " +
                "playlistOwnerName=owner, totalTracks=10, addedAt=2024-07-23T10:30)";

        assertThat(response.toString()).isEqualTo(expectedString);
    }
}
