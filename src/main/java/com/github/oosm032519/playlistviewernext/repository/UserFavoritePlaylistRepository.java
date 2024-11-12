package com.github.oosm032519.playlistviewernext.repository;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserFavoritePlaylistRepository extends JpaRepository<UserFavoritePlaylist, Long> {
    boolean existsByUserIdAndPlaylistId(String userId, String playlistId);

    long deleteByUserIdAndPlaylistId(String userId, String playlistId);

    List<UserFavoritePlaylist> findByUserId(String userId);

}
