package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotifyAnalyticsService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyAnalyticsService.class);
    private final SpotifyPlaylistService playlistService;
    private final SpotifyArtistService artistService;

    @Autowired
    public SpotifyAnalyticsService(SpotifyPlaylistService playlistService, SpotifyArtistService artistService) {
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
            Arrays.stream(fullTrack.getArtists())
                    .map(artist -> {
                        try {
                            return artistService.getArtistGenres(artist.getId());
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .flatMap(Collection::stream)
                    .forEach(genre -> genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1));
        }

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
        return genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
