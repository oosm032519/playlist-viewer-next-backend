package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistDetailsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyPlaylistAnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistAnalyticsService.class);

    private final SpotifyPlaylistDetailsService playlistDetailsService;
    private final GenreAggregatorService genreAggregatorService;

    @Autowired
    public SpotifyPlaylistAnalyticsService(SpotifyPlaylistDetailsService playlistDetailsService, GenreAggregatorService genreAggregatorService) {
        this.playlistDetailsService = playlistDetailsService;
        this.genreAggregatorService = genreAggregatorService;
    }

    /**
     * プレイリストのジャンル出現頻度上位5つを取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return ジャンル出現頻度上位5つのリスト
     * @throws PlaylistViewerNextException ジャンル出現頻度上位5つの取得中にエラーが発生した場合
     */
    public List<String> getTop5GenresForPlaylist(String playlistId) {
        logger.info("プレイリストのジャンル出現頻度上位5つの取得を開始します。プレイリストID: {}", playlistId);

        try {
            Map<String, Integer> genreCounts = getGenreCountsForPlaylist(playlistId);
            return genreAggregatorService.getTopGenres(genreCounts, 5);
        } catch (Exception e) {
            // ジャンル出現頻度上位5つの取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("プレイリストID: {} のジャンル出現頻度上位5つの取得中にエラーが発生しました。", playlistId, e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TOP_GENRES_RETRIEVAL_ERROR",
                    "プレイリストのジャンル出現頻度上位5つの取得中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * プレイリストのジャンルごとのトラック数を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return ジャンルごとのトラック数を表すマップ
     * @throws PlaylistViewerNextException ジャンルごとのトラック数の取得中にエラーが発生した場合
     */
    public Map<String, Integer> getGenreCountsForPlaylist(String playlistId) {
        logger.info("プレイリストのジャンル集計を開始します。プレイリストID: {}", playlistId);

        try {
            PlaylistTrack[] tracks = fetchPlaylistTracks(playlistId);
            if (tracks == null) {
                logger.warn("プレイリストID: {} に対するトラックが見つかりませんでした。", playlistId);
                return Collections.emptyMap();
            }

            return genreAggregatorService.aggregateGenres(tracks);
        } catch (Exception e) {
            // ジャンルごとのトラック数の取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("プレイリストID: {} のジャンルごとのトラック数の取得中にエラーが発生しました。", playlistId, e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "GENRE_COUNTS_RETRIEVAL_ERROR",
                    "プレイリストのジャンルごとのトラック数の取得中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * プレイリストのトラックを取得するヘルパーメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリストのトラック配列
     * @throws Exception プレイリストのトラックの取得中にエラーが発生した場合
     */
    private PlaylistTrack[] fetchPlaylistTracks(String playlistId) throws Exception {
        try {
            return playlistDetailsService.getPlaylistTracks(playlistId);
        } catch (Exception e) {
            logger.error("プレイリストID: {} のトラック取得中にエラーが発生しました。", playlistId, e);
            throw e;
        }
    }
}
