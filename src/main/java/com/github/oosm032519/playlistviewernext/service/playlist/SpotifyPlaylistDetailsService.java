package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
    @Cacheable(value = "playlistTracks", key = "#playlistId")
    public PlaylistTrack[] getPlaylistTracks(String playlistId) {
        return RetryUtil.executeWithRetry(() -> {
            try {
                Playlist playlist = getPlaylist(playlistId);
                if (playlist == null) {
                    throw new ResourceNotFoundException(
                            HttpStatus.NOT_FOUND,
                            "指定されたプレイリストが見つかりません。"
                    );
                }

                // 最初の100曲を追加
                List<PlaylistTrack> allTracks = new ArrayList<>(Arrays.asList(playlist.getTracks().getItems()));

                int offset = 100;
                int limit = 100;

                // 全曲取得するまで繰り返す
                while (allTracks.size() < playlist.getTracks().getTotal()) {
                    GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi
                            .getPlaylistsItems(playlistId)
                            .limit(limit)
                            .offset(offset)
                            .build();
                    Paging<PlaylistTrack> playlistTracks = getPlaylistsItemsRequest.execute();
                    allTracks.addAll(Arrays.asList(playlistTracks.getItems()));
                    offset += limit;
                }

                return allTracks.toArray(new PlaylistTrack[0]);
            } catch (ResourceNotFoundException e) {
                // ResourceNotFoundException はそのまま再スロー
                throw e;
            } catch (Exception e) {
                logger.error("トラック情報の取得中にエラーが発生しました。 playlistId: {}", playlistId, e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "トラック情報の取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }

    /**
     * 指定されたプレイリストIDのプレイリスト情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリスト情報、見つからない場合は null
     * @throws SpotifyApiException プレイリスト情報の取得中にエラーが発生した場合
     */
    @Cacheable(value = "playlist", key = "#playlistId")
    public Playlist getPlaylist(String playlistId) {
        return RetryUtil.executeWithRetry(() -> {
            try {
                GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
                return getPlaylistRequest.execute();
            } catch (Exception e) {
                logger.error("プレイリスト情報の取得中にエラーが発生しました。 playlistId: {}", playlistId, e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "プレイリスト情報の取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS);
    }
}
