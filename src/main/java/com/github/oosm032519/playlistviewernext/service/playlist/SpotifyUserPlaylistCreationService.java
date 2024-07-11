// SpotifyUserPlaylistCreationService.java

package com.github.oosm032519.playlistviewernext.service.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;

import java.io.IOException;
import java.util.List;

/**
 * Spotify APIを使用してユーザーのプレイリストを作成するサービスクラス。
 */
@Service
public class SpotifyUserPlaylistCreationService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyUserPlaylistCreationService.class);

    @Autowired
    private SpotifyApi spotifyApi;

    /**
     * 新しいプレイリストを作成し、指定されたトラックを追加します。
     *
     * @param accessToken  Spotify APIへのアクセスに使用するトークン
     * @param userId       プレイリストを作成するユーザーのID
     * @param playlistName 作成するプレイリストの名前
     * @param trackIds     プレイリストに追加するトラックのIDリスト
     * @return 作成されたプレイリストのID
     * @throws IOException                             入出力例外が発生した場合
     * @throws SpotifyWebApiException                  Spotify APIの例外が発生した場合
     * @throws org.apache.hc.core5.http.ParseException HTTPレスポンスの解析例外が発生した場合
     */
    public String createPlaylist(String accessToken, String userId, String playlistName, List<String> trackIds) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        logger.info("SpotifyUserPlaylistCreationService.createPlaylist() が呼び出されました。");
        logger.info("accessToken: {}", accessToken);
        logger.info("userId: {}", userId);
        logger.info("playlistName: {}", playlistName);
        logger.info("trackIds: {}", trackIds);

        // アクセストークンを設定
        spotifyApi.setAccessToken(accessToken);

        // プレイリスト作成リクエストを構築
        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName)
                .public_(false) // プレイリストを非公開に設定
                .build();

        // プレイリストを作成
        Playlist playlist = createPlaylistRequest.execute();
        String playlistId = playlist.getId();
        logger.info("playlistId: {}", playlistId);

        // トラックIDをSpotify URIに変換
        List<String> trackUris = trackIds.stream()
                .map(id -> "spotify:track:" + id)
                .toList();
        logger.info("trackUris: {}", trackUris);

        // トラックが存在する場合、プレイリストに追加
        if (!trackUris.isEmpty()) {
            AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi.addItemsToPlaylist(playlistId, trackUris.toArray(new String[0]))
                    .build();
            addItemsToPlaylistRequest.execute();
            logger.info("トラックをプレイリストに追加しました。");
        }

        logger.info("プレイリストの作成が完了しました。");
        return playlistId;
    }
}
