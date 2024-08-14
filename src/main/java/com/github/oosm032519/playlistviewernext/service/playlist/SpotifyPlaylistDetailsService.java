package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistDetailsService.class);

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
     * @throws ResourceNotFoundException プレイリストが見つからない場合
     * @throws SpotifyApiException       トラック情報の取得中にエラーが発生した場合
     */
    public PlaylistTrack[] getPlaylistTracks(String playlistId) {
        try {
            Playlist playlist = getPlaylist(playlistId);
            if (playlist == null) {
                throw new ResourceNotFoundException(
                        HttpStatus.NOT_FOUND,
                        "PLAYLIST_NOT_FOUND",
                        "指定されたプレイリストが見つかりません。"
                );
            }
            return playlist.getTracks().getItems();
        } catch (ResourceNotFoundException e) {
            // ResourceNotFoundException はそのまま再スロー
            throw e;
        } catch (Exception e) {
            logger.error("トラック情報の取得中にエラーが発生しました。 playlistId: {}", playlistId, e);
            throw new SpotifyApiException(
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
     * @throws ResourceNotFoundException プレイリストが見つからない場合
     * @throws SpotifyApiException       プレイリスト情報の取得中にエラーが発生した場合
     */
    private Playlist getPlaylist(String playlistId) {
        try {
            GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
            Playlist playlist = getPlaylistRequest.execute();
            if (playlist == null) {
                throw new ResourceNotFoundException(
                        HttpStatus.NOT_FOUND,
                        "PLAYLIST_NOT_FOUND",
                        "指定されたプレイリストが見つかりません。"
                );
            }
            return playlist;
        } catch (ResourceNotFoundException e) {
            // ResourceNotFoundException はそのまま再スロー
            throw e;
        } catch (Exception e) {
            logger.error("プレイリスト情報の取得中にエラーが発生しました。 playlistId: {}", playlistId, e);
            throw new SpotifyApiException(
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
     * @throws ResourceNotFoundException プレイリストが見つからない場合
     * @throws SpotifyApiException       プレイリスト名の取得中にエラーが発生した場合
     */
    public String getPlaylistName(String playlistId) {
        try {
            Playlist playlist = getPlaylist(playlistId);
            return playlist.getName();
        } catch (ResourceNotFoundException e) {
            // ResourceNotFoundException はそのまま再スロー
            throw e;
        } catch (Exception e) {
            logger.error("プレイリスト名の取得中にエラーが発生しました。 playlistId: {}", playlistId, e);
            throw new SpotifyApiException(
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
     * @throws ResourceNotFoundException プレイリストが見つからない場合
     * @throws SpotifyApiException       オーナー情報の取得中にエラーが発生した場合
     */
    public User getPlaylistOwner(String playlistId) {
        try {
            Playlist playlist = getPlaylist(playlistId);
            return playlist.getOwner();
        } catch (ResourceNotFoundException e) {
            // ResourceNotFoundException はそのまま再スロー
            throw e;
        } catch (Exception e) {
            logger.error("オーナー情報の取得中にエラーが発生しました。 playlistId: {}", playlistId, e);
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "OWNER_INFO_RETRIEVAL_ERROR",
                    "オーナー情報の取得中にエラーが発生しました。",
                    e
            );
        }
    }
}
