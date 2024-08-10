package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;

@Service
public class SpotifyPlaylistTrackAdditionService {

    private final SpotifyApi spotifyApi;

    public SpotifyPlaylistTrackAdditionService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたプレイリストにトラックを追加します。
     *
     * @param accessToken Spotify APIにアクセスするためのアクセストークン
     * @param playlistId  トラックを追加するプレイリストのID
     * @param trackId     追加するトラックのID
     * @return SnapshotResult プレイリストのスナップショット結果
     * @throws PlaylistViewerNextException トラックの追加中にエラーが発生した場合
     */
    public SnapshotResult addTrackToPlaylist(String accessToken, String playlistId, String trackId) {
        try {
            spotifyApi.setAccessToken(accessToken);
            String trackUri = String.format("spotify:track:%s", trackId);
            return spotifyApi.addItemsToPlaylist(playlistId, new String[]{trackUri})
                    .build()
                    .execute();
        } catch (Exception e) {
            // トラックの追加中にエラーが発生した場合は PlaylistViewerNextException をスロー
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRACK_ADDITION_ERROR",
                    "トラックの追加中にエラーが発生しました。",
                    e
            );
        }
    }
}
