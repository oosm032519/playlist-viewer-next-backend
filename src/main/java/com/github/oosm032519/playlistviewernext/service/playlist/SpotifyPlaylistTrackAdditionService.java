package com.github.oosm032519.playlistviewernext.service.playlist;

import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

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
     * @throws IOException            入出力例外が発生した場合
     * @throws SpotifyWebApiException Spotify APIからのエラーが発生した場合
     * @throws ParseException         レスポンスの解析中にエラーが発生した場合
     */
    public SnapshotResult addTrackToPlaylist(String accessToken, String playlistId, String trackId)
            throws IOException, SpotifyWebApiException, ParseException {
        spotifyApi.setAccessToken(accessToken);
        String trackUri = String.format("spotify:track:%s", trackId);
        return spotifyApi.addItemsToPlaylist(playlistId, new String[]{trackUri})
                .build()
                .execute();
    }
}
