package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;

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
     * 指定されたプレイリストIDのトラック情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリスト内のトラックの配列
     * @throws PlaylistViewerNextException トラック情報の取得中にエラーが発生した場合
     */
    public PlaylistTrack[] getPlaylistTracks(String playlistId) {
        try {
            return getPlaylist(playlistId).getTracks().getItems();
        } catch (Exception e) {
            // トラック情報の取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRACKS_RETRIEVAL_ERROR",
                    "トラック情報の取得中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * 指定されたプレイリストIDのプレイリスト情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリスト情報
     * @throws PlaylistViewerNextException プレイリスト情報の取得中にエラーが発生した場合
     */
    private Playlist getPlaylist(String playlistId) {
        try {
            GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
            return getPlaylistRequest.execute();
        } catch (Exception e) {
            // プレイリスト情報の取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PLAYLIST_INFO_RETRIEVAL_ERROR",
                    "プレイリスト情報の取得中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * 指定されたプレイリストIDの名前を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリストの名前
     * @throws PlaylistViewerNextException プレイリスト名の取得中にエラーが発生した場合
     */
    public String getPlaylistName(String playlistId) {
        try {
            return getPlaylist(playlistId).getName();
        } catch (Exception e) {
            // プレイリスト名の取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PLAYLIST_NAME_RETRIEVAL_ERROR",
                    "プレイリスト名の取得中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * 指定されたプレイリストIDのオーナー情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリストのオーナー情報
     * @throws PlaylistViewerNextException オーナー情報の取得中にエラーが発生した場合
     */
    public User getPlaylistOwner(String playlistId) {
        try {
            return getPlaylist(playlistId).getOwner();
        } catch (Exception e) {
            // オーナー情報の取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OWNER_INFO_RETRIEVAL_ERROR",
                    "オーナー情報の取得中にエラーが発生しました。",
                    e
            );
        }
    }
}
