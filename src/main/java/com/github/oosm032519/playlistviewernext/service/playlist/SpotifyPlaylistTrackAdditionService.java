// SpotifyPlaylistTrackAdditionService.java

package com.github.oosm032519.playlistviewernext.service.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@Service
public class SpotifyPlaylistTrackAdditionService {

    // Spotify APIへのアクセスを提供するためのSpotifyApiインスタンスを注入
    @Autowired
    private SpotifyApi spotifyApi;

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
    public SnapshotResult addTrackToPlaylist(String accessToken, String playlistId, String trackId) throws IOException, SpotifyWebApiException, ParseException {
        // アクセストークンを設定
        spotifyApi.setAccessToken(accessToken);

        // 追加するトラックのURIを配列に格納
        String[] uris = new String[]{"spotify:track:" + trackId};

        // プレイリストにトラックを追加するリクエストを作成
        AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi.addItemsToPlaylist(playlistId, uris).build();

        // リクエストを実行し、結果を返す
        return addItemsToPlaylistRequest.execute();
    }
}
