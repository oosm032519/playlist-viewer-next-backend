package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
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
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.RemoveItemsFromPlaylistRequest;

import java.util.Map;

@Service
public class SpotifyPlaylistTrackRemovalService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistTrackRemovalService.class);

    @Autowired
    private SpotifyApi spotifyApi;

    public ResponseEntity<String> removeTrackFromPlaylist(PlaylistTrackRemovalRequest request, OAuth2User principal) {
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

        try {
            RemoveItemsFromPlaylistRequest removeItemsFromPlaylistRequest = spotifyApi
                    .removeItemsFromPlaylist(playlistId, tracks)
                    .build();

            SnapshotResult snapshotResult = removeItemsFromPlaylistRequest.execute();
            return successResponse(snapshotResult);
        } catch (Exception e) {
            logger.error("Error occurred while removing track from playlist.", e);
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRACK_REMOVAL_ERROR",
                    "トラックの削除中にエラーが発生しました。",
                    e
            );
        }
    }

    private String getAccessToken(OAuth2User principal) {
        Map<String, Object> attributes = principal.getAttributes();
        String accessToken = (String) attributes.get("spotify_access_token");

        if (accessToken == null || accessToken.isEmpty()) {
            logger.warn("No valid access token found. User attributes: {}", attributes);
            return null;
        }
        return accessToken;
    }

    private JsonArray createTracksJsonArray(String trackId) {
        return JsonParser.parseString("[{\"uri\":\"spotify:track:" + trackId + "\"}]").getAsJsonArray();
    }

    private ResponseEntity<String> successResponse(SnapshotResult snapshotResult) {
        logger.info("Track successfully removed. Snapshot ID: {}", snapshotResult.getSnapshotId());
        return ResponseEntity.ok("トラックが正常に削除されました。Snapshot ID: " + snapshotResult.getSnapshotId());
    }
}
