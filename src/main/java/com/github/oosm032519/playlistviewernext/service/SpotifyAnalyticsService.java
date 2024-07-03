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
    private final SpotifyService spotifyService;

    @Autowired
    public SpotifyAnalyticsService(SpotifyApi spotifyApi, SpotifyService spotifyService) {
        this.spotifyApi = spotifyApi;
        this.spotifyService = spotifyService;
    }

    public Map<String, Integer> getGenreCountsForPlaylist(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのジャンル集計を開始します。プレイリストID: {}", playlistId);
        PlaylistTrack[] tracks = spotifyService.getPlaylistTracks(playlistId);
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
                List<String> genres = spotifyService.getArtistGenres(artistId);
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
        logger.info("SpotifyAnalyticsService: getRecommendationsメソッドが呼び出されました。シードジャンル: {}", seedGenres);
        if (seedGenres.isEmpty()) {
            logger.info("SpotifyAnalyticsService: シードジャンルが空のため、Spotify APIを呼び出しません。");
            return Collections.emptyList();
        }

        String joinedGenres = String.join(",", seedGenres);
        GetRecommendationsRequest getRecommendationsRequest = spotifyApi.getRecommendations()
                .seed_genres(joinedGenres)
                .limit(20)
                .build();
        Recommendations recommendations = getRecommendationsRequest.execute();
        List<Track> recommendedTracks = recommendations.getTracks() != null ? Arrays.asList(recommendations.getTracks()) : Collections.emptyList();
        logger.info("SpotifyAnalyticsService: オススメ楽曲を取得しました。楽曲数: {}", recommendedTracks.size());
        for (Track track : recommendedTracks) {
            logger.info(" - 曲名: {}, アーティスト: {}", track.getName(), track.getArtists()[0].getName());
        }
        return recommendedTracks;
    }

    private void logAudioFeatures(String trackName, AudioFeatures audioFeatures) {
        logger.info("Track: {}", trackName);
        logger.info("Audio Features:");
        logger.info(" Danceability: {}", audioFeatures.getDanceability());
        logger.info(" Energy: {}", audioFeatures.getEnergy());
        logger.info(" Key: {}", audioFeatures.getKey());
        logger.info(" Loudness: {}", audioFeatures.getLoudness());
        logger.info(" Mode: {}", audioFeatures.getMode());
        logger.info(" Speechiness: {}", audioFeatures.getSpeechiness());
        logger.info(" Acousticness: {}", audioFeatures.getAcousticness());
        logger.info(" Instrumentalness: {}", audioFeatures.getInstrumentalness());
        logger.info(" Liveness: {}", audioFeatures.getLiveness());
        logger.info(" Valence: {}", audioFeatures.getValence());
        logger.info(" Tempo: {}", audioFeatures.getTempo());
        logger.info(" Duration MS: {}", audioFeatures.getDurationMs());
        logger.info(" Time Signature: {}", audioFeatures.getTimeSignature());
        logger.info("--------------------");
    }
}
