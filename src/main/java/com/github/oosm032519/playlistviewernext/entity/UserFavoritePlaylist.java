package com.github.oosm032519.playlistviewernext.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorite_playlists")
@Getter
@Setter
public class UserFavoritePlaylist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String playlistId;

    @Column(nullable = false)
    private String playlistName;

    @Column(nullable = false)
    private int totalTracks;

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime addedAt = LocalDateTime.now();

    @Column(nullable = false)
    private String playlistOwnerName;
}
