package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * プレイリストの分析サービスを提供するクラスです。
 */
@Service
public class PlaylistAnalyticsService {

    private final SpotifyPlaylistAnalyticsService analyticsService;

    /**
     * コンストラクタを使用して依存性を注入します。
     *
     * @param analyticsService SpotifyPlaylistAnalyticsServiceのインスタンス
     */
    @Autowired
    public PlaylistAnalyticsService(SpotifyPlaylistAnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    /**
     * 指定されたプレイリストのジャンルごとの曲数を取得します。
     *
     * @param id プレイリストのID
     * @return ジャンルごとの曲数を表すMap
     * @throws PlaylistViewerNextException プレイリストの分析中にエラーが発生した場合
     */
    public Map<String, Integer> getGenreCountsForPlaylist(String id) {
        try {
            return analyticsService.getGenreCountsForPlaylist(id);
        } catch (Exception e) {
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "GENRE_COUNTS_RETRIEVAL_ERROR",
                    "プレイリストのジャンルごとの曲数の取得中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * 指定されたプレイリストのトップ5ジャンルを取得します。
     *
     * @param id プレイリストのID
     * @return トップ5ジャンルのリスト
     * @throws PlaylistViewerNextException プレイリストの分析中にエラーが発生した場合
     */
    public List<String> getTop5GenresForPlaylist(String id) {
        try {
            return analyticsService.getTop5GenresForPlaylist(id);
        } catch (Exception e) {
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TOP_GENRES_RETRIEVAL_ERROR",
                    "プレイリストのトップ5ジャンルの取得中にエラーが発生しました。",
                    e
            );
        }
    }
}
