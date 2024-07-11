// SpotifyPlaylistAnalyticsService.java

package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistDetailsService;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyPlaylistAnalyticsService {
    // ロガーの初期化
    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistAnalyticsService.class);

    // プレイリストの詳細サービス
    private final SpotifyPlaylistDetailsService playlistDetailsService;

    // ジャンル集計サービス
    private final GenreAggregatorService genreAggregatorService;

    /**
     * コンストラクタ
     *
     * @param playlistDetailsService プレイリストの詳細サービス
     * @param genreAggregatorService ジャンル集計サービス
     */
    @Autowired
    public SpotifyPlaylistAnalyticsService(SpotifyPlaylistDetailsService playlistDetailsService, GenreAggregatorService genreAggregatorService) {
        this.playlistDetailsService = playlistDetailsService;
        this.genreAggregatorService = genreAggregatorService;
    }

    /**
     * プレイリストのジャンルごとのトラック数を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return ジャンルごとのトラック数を表すマップ
     * @throws IOException            入出力例外
     * @throws SpotifyWebApiException Spotify API例外
     * @throws ParseException         パース例外
     */
    public Map<String, Integer> getGenreCountsForPlaylist(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのジャンル集計を開始します。プレイリストID: {}", playlistId);

        // プレイリストのトラックを取得
        PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(playlistId);

        // トラックが見つからなかった場合の処理
        if (tracks == null) {
            logger.warn("プレイリストID: {} に対するトラックが見つかりませんでした。", playlistId);
            return Collections.emptyMap();
        }

        // ジャンルの集計を行う
        return genreAggregatorService.aggregateGenres(tracks);
    }

    /**
     * プレイリストのジャンル出現頻度上位5つを取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return ジャンル出現頻度上位5つのリスト
     * @throws IOException            入出力例外
     * @throws SpotifyWebApiException Spotify API例外
     * @throws ParseException         パース例外
     */
    public List<String> getTop5GenresForPlaylist(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのジャンル出現頻度上位5つの取得を開始します。プレイリストID: {}", playlistId);

        // プレイリストのジャンルごとのトラック数を取得
        Map<String, Integer> genreCounts = getGenreCountsForPlaylist(playlistId);

        // 上位5つのジャンルを取得
        return genreAggregatorService.getTopGenres(genreCounts, 5);
    }
}
