package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;

@Service
public class SpotifyPlaylistTrackAdditionService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistTrackAdditionService.class);

    private final SpotifyApi spotifyApi;

    public SpotifyPlaylistTrackAdditionService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたプレイリストにトラックを追加する
     *
     * @param accessToken Spotify APIにアクセスするためのアクセストークン
     * @param playlistId  トラックを追加するプレイリストのID
     * @param trackId     追加するトラックのID
     * @return SnapshotResult プレイリストのスナップショット結果
     * @throws SpotifyApiException トラックの追加中にエラーが発生した場合
     */
    public SnapshotResult addTrackToPlaylist(String accessToken, String playlistId, String trackId) {
        return RetryUtil.executeWithRetry(() -> {
            try {
                spotifyApi.setAccessToken(accessToken);
                String trackUri = String.format("spotify:track:%s", trackId);
                return spotifyApi.addItemsToPlaylist(playlistId, new String[]{trackUri})
                        .build()
                        .execute();
            } catch (Exception e) {
                // トラックの追加中にエラーが発生した場合は SpotifyApiException をスロー
                logger.error("トラックの追加中にエラーが発生しました。 accessToken: {}, playlistId: {}, trackId: {}", accessToken, playlistId, trackId, e);
                throw new SpotifyApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "TRACK_ADDITION_ERROR",
                        "トラックの追加中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }
}
