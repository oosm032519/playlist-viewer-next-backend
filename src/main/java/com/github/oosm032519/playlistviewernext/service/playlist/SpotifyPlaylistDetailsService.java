package com.github.oosm032519.playlistviewernext.service.playlist;

import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
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
    public SpotifyPlaylistDetailsService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたプレイリストIDのプレイリスト情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリスト情報
     * @throws IOException                             Spotify APIとの通信中にエラーが発生した場合
     * @throws SpotifyWebApiException                  Spotify APIがエラーレスポンスを返した場合
     * @throws org.apache.hc.core5.http.ParseException レスポンスの解析中にエラーが発生した場合
     */
    private Playlist getPlaylist(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        return getPlaylistRequest.execute();
    }

    /**
     * 指定されたプレイリストIDのトラック情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリスト内のトラックの配列
     * @throws IOException                             Spotify APIとの通信中にエラーが発生した場合
     * @throws SpotifyWebApiException                  Spotify APIがエラーレスポンスを返した場合
     * @throws org.apache.hc.core5.http.ParseException レスポンスの解析中にエラーが発生した場合
     */
    public PlaylistTrack[] getPlaylistTracks(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        return getPlaylist(playlistId).getTracks().getItems();
    }

    /**
     * 指定されたプレイリストIDの名前を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリストの名前
     * @throws IOException                             Spotify APIとの通信中にエラーが発生した場合
     * @throws SpotifyWebApiException                  Spotify APIがエラーレスポンスを返した場合
     * @throws org.apache.hc.core5.http.ParseException レスポンスの解析中にエラーが発生した場合
     */
    public String getPlaylistName(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        return getPlaylist(playlistId).getName();
    }

    /**
     * 指定されたプレイリストIDのオーナー情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリストのオーナー情報
     * @throws IOException                             Spotify APIとの通信中にエラーが発生した場合
     * @throws SpotifyWebApiException                  Spotify APIがエラーレスポンスを返した場合
     * @throws org.apache.hc.core5.http.ParseException レスポンスの解析中にエラーが発生した場合
     */
    public User getPlaylistOwner(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        return getPlaylist(playlistId).getOwner();
    }
}
