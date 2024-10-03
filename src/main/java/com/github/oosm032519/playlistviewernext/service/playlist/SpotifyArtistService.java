package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.artists.GetSeveralArtistsRequest;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SpotifyArtistService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyArtistService.class);

    private final SpotifyApi spotifyApi;

    /**
     * SpotifyApiインスタンスを注入するコンストラクタ
     *
     * @param spotifyApi Spotify APIのインスタンス
     */
    public SpotifyArtistService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたアーティストIDのリストに基づき、各アーティストのジャンルを取得する
     *
     * @param artistIds アーティストIDのリスト
     * @return アーティストIDとジャンルのリストのマップ
     * @throws SpotifyApiException アーティスト情報の取得中にエラーが発生した場合
     */
    @Cacheable(value = "artistGenres", key = "#artistIds")
    public Map<String, List<String>> getArtistGenres(List<String> artistIds) {
        return RetryUtil.executeWithRetry(() -> {
            try {
                // アーティストIDのリストを50個以下のチャンクに分割
                List<List<String>> artistIdChunks = new ArrayList<>();
                for (int i = 0; i < artistIds.size(); i += 50) {
                    artistIdChunks.add(artistIds.subList(i, Math.min(i + 50, artistIds.size())));
                }

                // 各チャンクに対して GetSeveralArtistsRequest を実行
                Map<String, List<String>> artistGenresMap = new HashMap<>();
                for (List<String> chunk : artistIdChunks) {
                    Artist[] artists = getArtists(chunk);
                    artistGenresMap.putAll(Arrays.stream(artists)
                            .collect(Collectors.toMap(Artist::getId, artist -> List.of(artist.getGenres()))));
                }

                return artistGenresMap;
            } catch (Exception e) {
                // Spotify API 関連のエラーの場合は SpotifyApiException をスロー
                logger.error("アーティスト情報の取得中にエラーが発生しました。 artistIds: {}", artistIds, e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "アーティスト情報の取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }

    private Artist[] getArtists(List<String> artistIds) throws Exception {
        GetSeveralArtistsRequest getSeveralArtistsRequest = spotifyApi.getSeveralArtists(artistIds.toArray(new String[0])).build();
        return getSeveralArtistsRequest.execute();
    }
}
