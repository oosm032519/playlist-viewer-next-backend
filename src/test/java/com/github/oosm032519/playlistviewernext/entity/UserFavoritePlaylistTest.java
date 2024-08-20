package com.github.oosm032519.playlistviewernext.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class UserFavoritePlaylistTest {

    @Test
    void testUserFavoritePlaylistCreation() {
        // Arrange
        UserFavoritePlaylist playlist = new UserFavoritePlaylist();
        LocalDateTime now = LocalDateTime.now();

        // Act
        playlist.setId(1L);
        playlist.setUserId("user123");
        playlist.setPlaylistId("playlist456");
        playlist.setPlaylistName("My Favorite Songs");
        playlist.setTotalTracks(20);
        playlist.setAddedAt(now);
        playlist.setPlaylistOwnerName("John Doe");

        // Assert
        assertThat(playlist.getId()).isEqualTo(1L);
        assertThat(playlist.getUserId()).isEqualTo("user123");
        assertThat(playlist.getPlaylistId()).isEqualTo("playlist456");
        assertThat(playlist.getPlaylistName()).isEqualTo("My Favorite Songs");
        assertThat(playlist.getTotalTracks()).isEqualTo(20);
        assertThat(playlist.getAddedAt()).isEqualTo(now);
        assertThat(playlist.getPlaylistOwnerName()).isEqualTo("John Doe");
    }

    @Test
    void testDefaultAddedAtValue() {
        // Arrange
        UserFavoritePlaylist playlist = new UserFavoritePlaylist();

        // Act & Assert
        assertThat(playlist.getAddedAt()).isNotNull();
        assertThat(playlist.getAddedAt()).isCloseTo(LocalDateTime.now(), within(1, ChronoUnit.SECONDS));
    }
}
