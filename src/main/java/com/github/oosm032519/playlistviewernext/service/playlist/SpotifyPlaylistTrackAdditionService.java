package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
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
     * @throws InternalServerException トラックの追加中に内部エラーが発生した場合
     */
    public SnapshotResult addTrackToPlaylist(String accessToken, String playlistId, String trackId) throws SpotifyWebApiException {
        return RetryUtil.executeWithRetry(() -> {
            try {
                spotifyApi.setAccessToken(accessToken);
                String trackUri = String.format("spotify:track:%s", trackId);
                return spotifyApi.addItemsToPlaylist(playlistId, new String[]{trackUri})
                        .build()
                        .execute();
            } catch (SpotifyWebApiException e) {
                // SpotifyWebApiException はそのまま再スロー
                logger.error("Spotify API エラー: {}, playlistId: {}, trackId: {}", e.getMessage(), playlistId, trackId, e);
                throw e;
            } catch (Exception e) {
                // その他の例外は InternalServerException にラップしてスロー
                logger.error("トラックの追加中にエラーが発生しました。 accessToken: {}, playlistId: {}, trackId: {}", accessToken, playlistId, trackId, e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "トラックの追加中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }
}
