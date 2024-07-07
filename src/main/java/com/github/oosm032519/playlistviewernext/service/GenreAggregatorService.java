package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenreAggregatorService {
    private final SpotifyArtistService artistService;

    @Autowired
    public GenreAggregatorService(SpotifyArtistService artistService) {
        this.artistService = artistService;
    }

    public Map<String, Integer> aggregateGenres(PlaylistTrack[] tracks) throws IOException, SpotifyWebApiException, ParseException {
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

    public List<String> getTopGenres(Map<String, Integer> genreCounts, int limit) {
        return genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
