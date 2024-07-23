package com.github.oosm032519.playlistviewernext.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FavoritePlaylistResponse {
    private String playlistId;
    private String playlistName;
    private String playlistOwnerName;
    private int totalTracks;
    private LocalDateTime addedAt;

    public FavoritePlaylistResponse(String playlistId, String playlistName, String playlistOwnerName, int totalTracks, LocalDateTime addedAt) {
        this.playlistId = playlistId;
        this.playlistName = playlistName;
        this.playlistOwnerName = playlistOwnerName;
        this.totalTracks = totalTracks;
        this.addedAt = addedAt;
    }
}
