// SpotifyPlaylistDetailsService.java

package com.github.oosm032519.playlistviewernext.service.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;

import java.io.IOException;

/**
 * Spotifyのプレイリストの詳細情報を取得するサービスクラス
 */
@Service
public class SpotifyPlaylistDetailsService {
    private final SpotifyApi spotifyApi;

    /**
     * コンストラクタ
     *
     * @param spotifyApi Spotify APIのインスタンス
     */
    @Autowired
    public SpotifyPlaylistDetailsService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたプレイリストIDのトラック情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリスト内のトラックの配列
     * @throws IOException                             入出力エラーが発生した場合
     * @throws SpotifyWebApiException                  Spotify APIのエラーが発生した場合
     * @throws org.apache.hc.core5.http.ParseException レスポンスの解析エラーが発生した場合
     */
    public PlaylistTrack[] getPlaylistTracks(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // プレイリスト取得リクエストを作成
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        // リクエストを実行し、プレイリストのトラック情報を取得
        return getPlaylistRequest.execute().getTracks().getItems();
    }

    /**
     * 指定されたプレイリストIDの名前を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリストの名前
     * @throws IOException                             入出力エラーが発生した場合
     * @throws SpotifyWebApiException                  Spotify APIのエラーが発生した場合
     * @throws org.apache.hc.core5.http.ParseException レスポンスの解析エラーが発生した場合
     */
    public String getPlaylistName(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // プレイリスト取得リクエストを作成
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        // リクエストを実行し、プレイリストの名前を取得
        return getPlaylistRequest.execute().getName();
    }

    /**
     * 指定されたプレイリストIDのオーナー情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリストのオーナー情報
     * @throws IOException                             入出力エラーが発生した場合
     * @throws SpotifyWebApiException                  Spotify APIのエラーが発生した場合
     * @throws org.apache.hc.core5.http.ParseException レスポンスの解析エラーが発生した場合
     */
    public User getPlaylistOwner(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // プレイリスト取得リクエストを作成
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        // リクエストを実行し、プレイリストのオーナー情報を取得
        return getPlaylistRequest.execute().getOwner();
    }
}
