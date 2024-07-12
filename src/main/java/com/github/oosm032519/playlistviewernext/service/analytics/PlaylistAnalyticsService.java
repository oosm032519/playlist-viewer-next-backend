package com.github.oosm032519.playlistviewernext.service.analytics;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
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
     * @throws PlaylistAnalyticsException プレイリストの分析中にエラーが発生した場合
     */
    public Map<String, Integer> getGenreCountsForPlaylist(String id) throws PlaylistAnalyticsException {
        try {
            return analyticsService.getGenreCountsForPlaylist(id);
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            throw new PlaylistAnalyticsException("Failed to get genre counts for playlist: " + id, e);
        }
    }

    /**
     * 指定されたプレイリストのトップ5ジャンルを取得します。
     *
     * @param id プレイリストのID
     * @return トップ5ジャンルのリスト
     * @throws PlaylistAnalyticsException プレイリストの分析中にエラーが発生した場合
     */
    public List<String> getTop5GenresForPlaylist(String id) throws PlaylistAnalyticsException {
        try {
            return analyticsService.getTop5GenresForPlaylist(id);
        } catch (IOException | ParseException | SpotifyWebApiException e) {
            throw new PlaylistAnalyticsException("Failed to get top 5 genres for playlist: " + id, e);
        }
    }
}
