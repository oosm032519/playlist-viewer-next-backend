package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.repository.UserFavoritePlaylistRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
     * 指定されたユーザーIDのお気に入りプレイリストを取得する
     *
     * @param userId ユーザーID
     * @return お気に入りプレイリストのリスト
     * @throws DatabaseAccessException お気に入りプレイリストの取得中にデータベースアクセスエラーが発生した場合
     */
    public List<FavoritePlaylistResponse> getFavoritePlaylists(String userId) {
        LOGGER.info("getFavoritePlaylists() 開始 - userId: {}", userId);

        try {
            List<UserFavoritePlaylist> favoritePlaylists = userFavoritePlaylistRepository.findByUserId(userId);

            LOGGER.debug("getFavoritePlaylists() - リポジトリから取得したプレイリスト数: {}", favoritePlaylists.size());

            List<FavoritePlaylistResponse> responseList = favoritePlaylists.stream()
                    .map(this::mapToResponse)
                    .collect(Collectors.toList());

            LOGGER.info("getFavoritePlaylists() 終了 - userId: {}, 返却するプレイリスト数: {}", userId, responseList.size());

            return responseList;
        } catch (Exception e) {
            LOGGER.error("お気に入りプレイリストの取得中にデータベースアクセスエラーが発生しました。 userId: {}", userId, e);
            throw new DatabaseAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "お気に入りプレイリストの取得中にデータベースアクセスエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * UserFavoritePlaylistエンティティをFavoritePlaylistResponseモデルにマッピングする
     *
     * @param favoritePlaylist UserFavoritePlaylistエンティティ
     * @return FavoritePlaylistResponseモデル
     */
    private FavoritePlaylistResponse mapToResponse(UserFavoritePlaylist favoritePlaylist) {
        LOGGER.debug("mapToResponse() 開始 - favoritePlaylist: {}", favoritePlaylist);

        FavoritePlaylistResponse response = new FavoritePlaylistResponse(
                favoritePlaylist.getPlaylistId(),
                favoritePlaylist.getPlaylistName(),
                favoritePlaylist.getPlaylistOwnerName(),
                favoritePlaylist.getTotalTracks(),
                favoritePlaylist.getAddedAt()
        );

        LOGGER.debug("mapToResponse() 終了 - response: {}", response);

        return response;
    }
}
