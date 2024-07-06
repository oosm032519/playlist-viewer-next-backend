package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotifyAnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyAnalyticsService.class);
    private final SpotifyApi spotifyApi;
    private final SpotifyPlaylistService playlistService;
    private final SpotifyArtistService artistService;

    @Autowired
    public SpotifyAnalyticsService(SpotifyApi spotifyApi, SpotifyPlaylistService playlistService, SpotifyArtistService artistService) {
        this.spotifyApi = spotifyApi;
        this.playlistService = playlistService;
        this.artistService = artistService;
    }

    public Map<String, Integer> getGenreCountsForPlaylist(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのジャンル集計を開始します。プレイリストID: {}", playlistId);
        PlaylistTrack[] tracks = playlistService.getPlaylistTracks(playlistId);
        if (tracks == null) {
            logger.warn("プレイリストID: {} に対するトラックが見つかりませんでした。", playlistId);
            return Collections.emptyMap();
        }

        Map<String, Integer> genreCount = new HashMap<>();

        for (PlaylistTrack track : tracks) {
            Track fullTrack = (Track) track.getTrack();
            ArtistSimplified[] artists = fullTrack.getArtists();
            for (ArtistSimplified artist : artists) {
                String artistId = artist.getId();
                List<String> genres = artistService.getArtistGenres(artistId);
                genres.forEach(genre -> genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1));
            }
        }

        // ジャンルの出現回数を降順でソート
        return genreCount.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, _) -> e1,
                        LinkedHashMap::new
                ));
    }

    public List<String> getTop5GenresForPlaylist(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのジャンル出現頻度上位5つの取得を開始します。プレイリストID: {}", playlistId);
        Map<String, Integer> genreCounts = getGenreCountsForPlaylist(playlistId);
        // 出現頻度上位5つのジャンルを取得
        return genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Track> getRecommendations(List<String> seedGenres) throws IOException, SpotifyWebApiException, ParseException {
        if (seedGenres.isEmpty()) {
            return Collections.emptyList();
        }

        String genres = String.join(",", seedGenres);
        GetRecommendationsRequest recommendationsRequest = spotifyApi.getRecommendations()
                .seed_genres(genres)
                .limit(20)
                .build();

        Recommendations recommendations = recommendationsRequest.execute();

        // null チェックを追加
        if (recommendations == null || recommendations.getTracks() == null) {
            return Collections.emptyList();
        }

        return Arrays.asList(recommendations.getTracks());
    }
}
