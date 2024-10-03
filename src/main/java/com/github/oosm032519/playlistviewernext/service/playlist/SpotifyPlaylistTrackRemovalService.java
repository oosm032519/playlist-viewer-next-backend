package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;

import java.io.IOException;
import java.util.Map;

/**
 * Spotifyのプレイリストからトラックを削除するサービスクラス
 * このクラスはSpotify APIを使用してプレイリストの操作を行う
 */
@Service
public class SpotifyPlaylistTrackRemovalService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistTrackRemovalService.class);

    @Autowired
    private SpotifyApi spotifyApi;

    /**
     * プレイリストからトラックを削除する
     *
     * @param request   削除リクエスト情報を含むオブジェクト
     * @param principal 認証されたユーザー情報
     * @return 削除操作の結果を含むResponseEntity
     * @throws AuthenticationException 認証エラーが発生した場合
     */
    public ResponseEntity<String> removeTrackFromPlaylist(PlaylistTrackRemovalRequest request, OAuth2User principal) throws SpotifyWebApiException {
        String accessToken = getAccessToken(principal);
        if (accessToken == null) {
            logger.warn("Unauthorized access attempt with missing access token.");
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "AUTHENTICATION_ERROR",
                    "有効なアクセストークンがありません。"
            );
        }

        spotifyApi.setAccessToken(accessToken);

        String playlistId = request.getPlaylistId();
        String trackId = request.getTrackId();

        logger.info("Removing track from playlist. Playlist ID: {}, Track ID: {}", playlistId, trackId);

        JsonArray tracks = createTracksJsonArray(trackId);

        return RetryUtil.executeWithRetry(() -> {
            try {
                RemoveItemsFromPlaylistRequest removeItemsFromPlaylistRequest = spotifyApi
                        .removeItemsFromPlaylist(playlistId, tracks)
                        .build();

                SnapshotResult snapshotResult = removeItemsFromPlaylistRequest.execute();
                return successResponse(snapshotResult);
            } catch (SpotifyWebApiException e) {
                // SpotifyWebApiException はそのまま再スロー
                logger.error("Spotify API エラー: {}", e.getMessage(), e);
                throw e;
            } catch (IOException | org.apache.hc.core5.http.ParseException e) {
                logger.error("Error occurred while removing track from playlist.", e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "トラックの削除中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }

    /**
     * OAuth2Userからアクセストークンを取得する
     *
     * @param principal 認証されたユーザー情報
     * @return アクセストークン。トークンが見つからない場合はnull
     */
    private String getAccessToken(OAuth2User principal) {
        Map<String, Object> attributes = principal.getAttributes();
        String accessToken = (String) attributes.get("spotify_access_token");

        if (accessToken == null || accessToken.isEmpty()) {
            logger.warn("No valid access token found. User attributes: {}", attributes);
            return null;
        }
        return accessToken;
    }

    /**
     * トラックIDからJsonArrayを作成する
     *
     * @param trackId トラックID
     * @return トラック情報を含むJsonArray
     */
    private JsonArray createTracksJsonArray(String trackId) {
        return JsonParser.parseString("[{\"uri\":\"spotify:track:" + trackId + "\"}]").getAsJsonArray();
    }

    /**
     * 成功レスポンスを生成する
     *
     * @param snapshotResult 削除操作の結果
     * @return 成功メッセージを含むResponseEntity
     */
    private ResponseEntity<String> successResponse(SnapshotResult snapshotResult) {
        logger.info("Track successfully removed. Snapshot ID: {}", snapshotResult.getSnapshotId());
        return ResponseEntity.ok("トラックが正常に削除されました。Snapshot ID: " + snapshotResult.getSnapshotId());
    }
}
