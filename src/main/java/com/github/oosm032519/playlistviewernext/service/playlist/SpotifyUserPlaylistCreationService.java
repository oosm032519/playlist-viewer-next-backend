package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spotify APIを利用してユーザーのプレイリストを作成および管理するサービスクラス。
 * プレイリストの作成と楽曲の追加機能を提供する。
 */
@Service
public class SpotifyUserPlaylistCreationService {

    /**
     * ロガーインスタンス
     */
    private static final Logger logger = LoggerFactory.getLogger(SpotifyUserPlaylistCreationService.class);

    /**
     * Spotify トラックURIのプレフィックス
     */
    private static final String SPOTIFY_TRACK_URI_PREFIX = "spotify:track:";

    /**
     * Spotify API クライアントインスタンス
     */
    private final SpotifyApi spotifyApi;

    /**
     * コンストラクタ。
     *
     * @param spotifyApi Spotify APIクライアントインスタンス
     */
    public SpotifyUserPlaylistCreationService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 新しいプレイリストを作成し、指定された楽曲を追加する。
     * 処理は最大3回まで再試行される。
     *
     * @param accessToken  Spotify APIアクセストークン
     * @param userId       プレイリストを作成するユーザーID
     * @param playlistName 作成するプレイリスト名
     * @param trackIds     追加する楽曲IDのリスト
     * @return 作成されたプレイリストのID
     * @throws SpotifyWebApiException Spotify API呼び出し時にエラーが発生した場合
     */
    public String createPlaylist(String accessToken, String userId, String playlistName, List<String> trackIds) throws SpotifyWebApiException {
        logMethodCall(accessToken, userId, playlistName, trackIds);

        spotifyApi.setAccessToken(accessToken);

        return RetryUtil.executeWithRetry(() -> {
            try {
                // プレイリストを作成し、楽曲を追加
                String playlistId = createSpotifyPlaylist(userId, playlistName);
                addTracksToPlaylist(playlistId, trackIds);

                logger.info("プレイリストの作成が完了しました。");
                return playlistId;
            } catch (SpotifyWebApiException e) {
                logger.error("Spotify API エラー: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                logger.error("プレイリストの作成中にエラーが発生しました。", e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "プレイリストの作成中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS);
    }

    /**
     * メソッド呼び出し時のパラメータをログに記録する。
     */
    private void logMethodCall(String accessToken, String userId, String playlistName, List<String> trackIds) {
        logger.info("SpotifyUserPlaylistCreationService.createPlaylist() が呼び出されました。");
        logger.info("accessToken: {}", accessToken);
        logger.info("userId: {}", userId);
        logger.info("playlistName: {}", playlistName);
        logger.info("trackIds: {}", trackIds);
    }

    /**
     * Spotify APIを使用して新しいプレイリストを作成する。
     *
     * @param userId       ユーザーID
     * @param playlistName プレイリスト名
     * @return 作成されたプレイリストのID
     */
    private String createSpotifyPlaylist(String userId, String playlistName) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName)
                .public_(false)  // プライベートプレイリストとして作成
                .build();

        Playlist playlist = createPlaylistRequest.execute();
        String playlistId = playlist.getId();
        logger.info("playlistId: {}", playlistId);
        return playlistId;
    }

    /**
     * 指定されたプレイリストに楽曲を追加する。
     *
     * @param playlistId プレイリストID
     * @param trackIds   追加する楽曲IDのリスト
     */
    private void addTracksToPlaylist(String playlistId, List<String> trackIds) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        if (trackIds.isEmpty()) {
            return;
        }

        List<String> trackUris = convertToSpotifyUris(trackIds);
        logger.info("trackUris: {}", trackUris);

        AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi.addItemsToPlaylist(playlistId, trackUris.toArray(new String[0]))
                .build();
        addItemsToPlaylistRequest.execute();
        logger.info("トラックをプレイリストに追加しました。");
    }

    /**
     * 楽曲IDリストをSpotify URI形式に変換する。
     *
     * @param trackIds 変換する楽曲IDのリスト
     * @return Spotify URI形式に変換された楽曲IDのリスト
     */
    private List<String> convertToSpotifyUris(List<String> trackIds) {
        return trackIds.stream()
                .map(id -> SPOTIFY_TRACK_URI_PREFIX + id)
                .collect(Collectors.toList());
    }
}
