package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
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

@Service
public class SpotifyPlaylistTrackRemovalService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistTrackRemovalService.class);

    @Autowired
    private SpotifyApi spotifyApi;

    public ResponseEntity<String> removeTrackFromPlaylist(PlaylistTrackRemovalRequest request, OAuth2User principal) {
        Map<String, Object> attributes = principal.getAttributes();
        String accessToken = (String) attributes.get("access_token");

        if (accessToken == null || accessToken.isEmpty()) {
            logger.warn("有効なアクセストークンがありません。ユーザー属性: {}", attributes);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("有効なアクセストークンがありません。");
        }

        logger.debug("アクセストークンを設定します。");
        spotifyApi.setAccessToken(accessToken);

        String playlistId = request.getPlaylistId();
        String trackId = request.getTrackId();

        logger.info("プレイリストID: {}, トラックID: {}", playlistId, trackId);

        JsonArray tracks = JsonParser.parseString("[{\"uri\":\"spotify:track:" + trackId + "\"}]").getAsJsonArray();

        try {
            logger.debug("RemoveItemsFromPlaylistRequest を構築します。");
            RemoveItemsFromPlaylistRequest removeItemsFromPlaylistRequest = spotifyApi
                    .removeItemsFromPlaylist(playlistId, tracks)
                    .build();

            logger.info("トラックの削除を実行します。");
            SnapshotResult snapshotResult = removeItemsFromPlaylistRequest.execute();
            logger.info("トラックが正常に削除されました。Snapshot ID: {}", snapshotResult.getSnapshotId());
            return ResponseEntity.ok("トラックが正常に削除されました。Snapshot ID: " + snapshotResult.getSnapshotId());
        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            logger.error("トラックの削除中にエラーが発生しました。", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("エラー: " + e.getMessage());
        }
    }
}