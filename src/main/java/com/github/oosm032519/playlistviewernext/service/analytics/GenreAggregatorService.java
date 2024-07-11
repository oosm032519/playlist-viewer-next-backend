// GenreAggregatorService.java

package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyArtistService;
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
     */
    public Map<String, Integer> aggregateGenres(PlaylistTrack[] tracks) {
        Map<String, Integer> genreCount = new HashMap<>();

        for (PlaylistTrack track : tracks) {
            if (track == null || track.getTrack() == null) {
                continue; // track または track.getTrack() が null の場合はスキップ
            }
            Track fullTrack = (Track) track.getTrack();
            Arrays.stream(fullTrack.getArtists())
                    .map(artist -> {
                        try {
                            // アーティストのジャンルを取得
                            return artistService.getArtistGenres(artist.getId());
                        } catch (IOException | SpotifyWebApiException | ParseException e) {
                            throw new RuntimeException(e); // 例外発生時はランタイム例外をスロー
                        }
                    })
                    .flatMap(Collection::stream)
                    .forEach(genre -> genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1)); // ジャンルの出現回数をカウント
        }

        // 出現回数の降順でソートして結果を返す
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
}
