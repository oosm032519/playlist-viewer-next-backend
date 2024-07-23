package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.repository.UserFavoritePlaylistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * データベースからログインユーザーのお気に入りプレイリストを取得するビジネスロジックを実装するサービスクラス
 */
@Service
public class UserFavoritePlaylistsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserFavoritePlaylistsService.class);

    private final UserFavoritePlaylistRepository userFavoritePlaylistRepository;

    /**
     * コンストラクタ
     *
     * @param userFavoritePlaylistRepository お気に入りプレイリストリポジトリ
     */
    public UserFavoritePlaylistsService(UserFavoritePlaylistRepository userFavoritePlaylistRepository) {
        this.userFavoritePlaylistRepository = userFavoritePlaylistRepository;
    }

    /**
     * 指定されたユーザーIDのお気に入りプレイリストを取得します。
     *
     * @param userId ユーザーID
     * @return お気に入りプレイリストのリスト
     */
    public List<FavoritePlaylistResponse> getFavoritePlaylists(String userId) {
        LOGGER.info("ユーザーID: {} のお気に入りプレイリストを取得します。", userId);

        List<UserFavoritePlaylist> favoritePlaylists = userFavoritePlaylistRepository.findByUserId(userId);

        return favoritePlaylists.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * UserFavoritePlaylistエンティティをFavoritePlaylistResponseモデルにマッピングします。
     *
     * @param favoritePlaylist UserFavoritePlaylistエンティティ
     * @return FavoritePlaylistResponseモデル
     */
    private FavoritePlaylistResponse mapToResponse(UserFavoritePlaylist favoritePlaylist) {
        return new FavoritePlaylistResponse(
                favoritePlaylist.getPlaylistId(),
                favoritePlaylist.getPlaylistName(),
                favoritePlaylist.getPlaylistOwnerName(),
                favoritePlaylist.getTotalTracks(),
                favoritePlaylist.getAddedAt()
        );
    }
}
