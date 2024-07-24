package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;

import java.io.IOException;

@Service
public class SpotifyPlaylistTrackRemovalService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistTrackRemovalService.class);

    @Autowired
    private SpotifyApi spotifyApi;

    public ResponseEntity<String> removeTrackFromPlaylist(PlaylistTrackRemovalRequest request, String accessToken) {
        if (accessToken == null) {
            return unauthorizedResponse();
        }

        spotifyApi.setAccessToken(accessToken);

        String playlistId = request.getPlaylistId();
        String trackId = request.getTrackId();

        logger.info("プレイリストID: {}, トラックID: {}", playlistId, trackId);

        JsonArray tracks = createTracksJsonArray(trackId);

        try {
            RemoveItemsFromPlaylistRequest removeItemsFromPlaylistRequest = spotifyApi
                    .removeItemsFromPlaylist(playlistId, tracks)
                    .build();

            SnapshotResult snapshotResult = removeItemsFromPlaylistRequest.execute();
            return successResponse(snapshotResult);
        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            return errorResponse(e);
        }
    }

    private JsonArray createTracksJsonArray(String trackId) {
        return JsonParser.parseString("[{\"uri\":\"spotify:track:" + trackId + "\"}]").getAsJsonArray();
    }

    private ResponseEntity<String> unauthorizedResponse() {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("有効なアクセストークンがありません。");
    }

    private ResponseEntity<String> successResponse(SnapshotResult snapshotResult) {
        logger.info("トラックが正常に削除されました。Snapshot ID: {}", snapshotResult.getSnapshotId());
        return ResponseEntity.ok("トラックが正常に削除されました。Snapshot ID: " + snapshotResult.getSnapshotId());
    }

    private ResponseEntity<String> errorResponse(Exception e) {
        logger.error("トラックの削除中にエラーが発生しました。", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("エラー: " + e.getMessage());
    }
}
