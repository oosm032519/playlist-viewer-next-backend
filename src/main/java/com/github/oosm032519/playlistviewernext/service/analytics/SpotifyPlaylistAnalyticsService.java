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
    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistAnalyticsService.class);

    private final SpotifyPlaylistDetailsService playlistDetailsService;
    private final GenreAggregatorService genreAggregatorService;

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

        PlaylistTrack[] tracks = fetchPlaylistTracks(playlistId);
        if (tracks == null) {
            logger.warn("プレイリストID: {} に対するトラックが見つかりませんでした。", playlistId);
            return Collections.emptyMap();
        }

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

        Map<String, Integer> genreCounts = getGenreCountsForPlaylist(playlistId);
        return genreAggregatorService.getTopGenres(genreCounts, 5);
    }

    /**
     * プレイリストのトラックを取得するヘルパーメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリストのトラック配列
     * @throws IOException            入出力例外
     * @throws SpotifyWebApiException Spotify API例外
     * @throws ParseException         パース例外
     */
    private PlaylistTrack[] fetchPlaylistTracks(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        try {
            return playlistDetailsService.getPlaylistTracks(playlistId);
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            logger.error("プレイリストID: {} のトラック取得中にエラーが発生しました。", playlistId, e);
            throw e;
        }
    }
}
