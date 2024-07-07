package com.github.oosm032519.playlistviewernext.service;

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

    public Map<String, Integer> getGenreCountsForPlaylist(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのジャンル集計を開始します。プレイリストID: {}", playlistId);
        PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(playlistId);
        if (tracks == null) {
            logger.warn("プレイリストID: {} に対するトラックが見つかりませんでした。", playlistId);
            return Collections.emptyMap();
        }
        return genreAggregatorService.aggregateGenres(tracks);
    }

    public List<String> getTop5GenresForPlaylist(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのジャンル出現頻度上位5つの取得を開始します。プレイリストID: {}", playlistId);
        Map<String, Integer> genreCounts = getGenreCountsForPlaylist(playlistId);
        return genreAggregatorService.getTopGenres(genreCounts, 5);
    }
}
