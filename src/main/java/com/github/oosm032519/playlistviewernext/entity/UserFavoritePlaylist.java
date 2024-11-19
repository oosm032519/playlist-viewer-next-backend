package com.github.oosm032519.playlistviewernext.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * ユーザーのお気に入りプレイリスト情報を管理するエンティティクラス。
 * データベースの user_favorite_playlists テーブルにマッピングされる。
 */
@Entity
@Table(name = "user_favorite_playlists")
@Getter
@Setter
public class UserFavoritePlaylist {

    /**
     * お気に入りプレイリストのユニークID。
     * 自動生成される主キー。
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * お気に入りを登録したユーザーのID。
     * null不可。
     */
    @Column(nullable = false)
    private String userId;

    /**
     * プレイリストの一意識別子。
     * Spotifyから提供されるID。
     * null不可。
     */
    @Column(nullable = false)
    private String playlistId;

    /**
     * プレイリストの名称。
     * null不可。
     */
    @Column(nullable = false)
    private String playlistName;

    /**
     * プレイリストに含まれる楽曲の総数。
     * null不可。
     */
    @Column(nullable = false)
    private int totalTracks;

    /**
     * お気に入りに追加された日時。
     * デフォルトで現在時刻が設定される。
     * null不可。
     */
    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime addedAt = LocalDateTime.now();

    /**
     * プレイリストの作成者名。
     * null不可。
     */
    @Column(nullable = false)
    private String playlistOwnerName;
}
