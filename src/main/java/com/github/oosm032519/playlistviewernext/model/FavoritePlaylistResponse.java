package com.github.oosm032519.playlistviewernext.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * お気に入りプレイリストの応答を表すモデルクラス
 * お気に入りプレイリストの詳細情報を保持する
 */
@Getter
@Setter
public class FavoritePlaylistResponse {

    /**
     * プレイリストの一意識別子
     */
    private String playlistId;

    /**
     * プレイリストの名前
     */
    private String playlistName;

    /**
     * プレイリストの所有者名
     */
    private String playlistOwnerName;

    /**
     * プレイリスト内のトラック総数
     */
    private int totalTracks;

    /**
     * プレイリストがお気に入りに追加された日時
     */
    private LocalDateTime addedAt;

    /**
     * FavoritePlaylistResponseオブジェクトを生成するコンストラクタ
     *
     * @param playlistId        プレイリストの一意識別子
     * @param playlistName      プレイリストの名前
     * @param playlistOwnerName プレイリストの所有者名
     * @param totalTracks       プレイリスト内のトラック総数
     * @param addedAt           プレイリストがお気に入りに追加された日時
     */
    public FavoritePlaylistResponse(String playlistId, String playlistName, String playlistOwnerName, int totalTracks, LocalDateTime addedAt) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.playlistOwnerName = playlistOwnerName;
        this.totalTracks = totalTracks;
        this.addedAt = addedAt;
    }

    public FavoritePlaylistResponse(final String playlistId, final String playlistName, final String playlistOwnerName, final int totalTracks, final Date date) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.playlistOwnerName = playlistOwnerName;
        this.totalTracks = totalTracks;
    }
}
