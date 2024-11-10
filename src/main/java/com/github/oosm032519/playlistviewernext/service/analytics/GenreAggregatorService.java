package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyArtistService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GenreAggregatorService {
    private final SpotifyArtistService artistService;

    /**
     * コンストラクタ - SpotifyArtistService を注入
     *
     * @param artistService SpotifyArtistService のインスタンス
     */
    @Autowired
    public GenreAggregatorService(SpotifyArtistService artistService) {
        this.artistService = artistService;
    }

    /**
     * プレイリストのトラックからジャンルを集計するメソッド
     *
     * @param tracks プレイリストのトラック配列
     * @return ジャンルとその出現回数のマップ
     * @throws PlaylistViewerNextException ジャンルの集計中にエラーが発生した場合
     */
    public Map<String, Integer> aggregateGenres(PlaylistTrack[] tracks) {
        try {
            Map<String, Integer> genreCount = new HashMap<>();
            Map<String, Integer> artistCount = new HashMap<>();

            List<String> artistIds = Arrays.stream(tracks)
                    .filter(Objects::nonNull)
                    .map(PlaylistTrack::getTrack)
                    .filter(Objects::nonNull)
                    .map(Track.class::cast)
                    .flatMap(track -> Arrays.stream(track.getArtists()))
                    .map(ArtistSimplified::getId)
                    .toList();

            artistIds.forEach(artistId -> artistCount.merge(artistId, 1, Integer::sum));

            List<String> uniqueArtistIds = new ArrayList<>(artistCount.keySet());

            Map<String, List<String>> artistGenresMap = artistService.getArtistGenres(uniqueArtistIds);

            Arrays.stream(tracks)
                    .filter(Objects::nonNull)
                    .map(PlaylistTrack::getTrack)
                    .filter(Objects::nonNull)
                    .map(Track.class::cast)
                    .flatMap(track -> Arrays.stream(track.getArtists()))
                    .forEach(artist -> {
                        List<String> genres = artistGenresMap.get(artist.getId());
                        if (genres != null) {
                            int weight = artistCount.get(artist.getId());
                            genres.forEach(genre -> genreCount.merge(genre, weight, Integer::sum));
                        }
                    });

            return genreCount.entrySet()
                    .stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            Map.Entry::getValue,
                            (e1, _) -> e1,
                            LinkedHashMap::new
                    ));
        } catch (Exception e) {
            throw new InvalidRequestException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ジャンルの集計中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * 集計されたジャンルの上位を取得するメソッド
     *
     * @param genreCounts ジャンルとその出現回数のマップ
     * @param limit       取得する上位ジャンルの数
     * @return 上位ジャンルのリスト
     */
    public List<String> getTopGenres(Map<String, Integer> genreCounts, int limit) {
        return genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }


    /**
     * 集計されたアーティストIDの上位を取得するメソッド
     *
     * @param artistCounts アーティストIDとその出現回数のマップ
     * @param limit        取得する上位アーティストIDの数
     * @return 上位アーティストIDのリスト
     */
    public List<String> getTopArtists(Map<String, Integer> artistCounts, int limit) {
        Random random = new Random();

        List<Map.Entry<String, Integer>> sortedEntries = artistCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());


        Map<Integer, List<String>> groupedArtists = sortedEntries.stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toList())));

        List<String> topArtists = new ArrayList<>();
        int count = 0;

        for (List<String> artists : groupedArtists.values()) {
            if (count + artists.size() <= limit) {
                topArtists.addAll(artists);
                count += artists.size();
            } else {

                Collections.shuffle(artists, random);
                topArtists.addAll(artists.subList(0, limit - count));
                count = limit;
                break;
            }
        }

        return topArtists;
    }
}
