package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
     * 指定されたアーティストIDに基づき、そのアーティストのジャンルを取得する
     *
     * @param artistId アーティストのID
     * @return アーティストのジャンルのリスト
     * @throws SpotifyApiException アーティスト情報の取得中にエラーが発生した場合
     */
    @Cacheable(value = "artistGenres", key = "#artistId")
    public List<String> getArtistGenres(String artistId) {
        return RetryUtil.executeWithRetry(() -> {
            try {
                return Optional.ofNullable(getArtist(artistId))
                        .map(Artist::getGenres)
                        .map(List::of)
                        .orElse(Collections.emptyList());
            } catch (Exception e) {
                // Spotify API 関連のエラーの場合は SpotifyApiException をスロー
                logger.error("アーティスト情報の取得中にエラーが発生しました。 artistId: {}", artistId, e);
                throw new SpotifyApiException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "ARTIST_INFO_RETRIEVAL_ERROR",
                        "アーティスト情報の取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }

    private Artist getArtist(String artistId) throws Exception {
        GetArtistRequest getArtistRequest = spotifyApi.getArtist(artistId).build();
        return getArtistRequest.execute();
    }
}
