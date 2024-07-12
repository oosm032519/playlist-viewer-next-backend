package com.github.oosm032519.playlistviewernext.service.playlist;

import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class SpotifyArtistService {
    private final SpotifyApi spotifyApi;

    /**
     * SpotifyApiインスタンスを注入するコンストラクタ。
     *
     * @param spotifyApi Spotify APIのインスタンス
     */
    public SpotifyArtistService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたアーティストIDに基づいて、そのアーティストのジャンルを取得します。
     *
     * @param artistId アーティストのID
     * @return アーティストのジャンルのリスト
     * @throws IOException                             入出力例外が発生した場合
     * @throws SpotifyWebApiException                  Spotify APIの例外が発生した場合
     * @throws org.apache.hc.core5.http.ParseException パース例外が発生した場合
     */
    public List<String> getArtistGenres(String artistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        return Optional.ofNullable(getArtist(artistId))
                .map(Artist::getGenres)
                .map(List::of)
                .orElse(Collections.emptyList());
    }

    private Artist getArtist(String artistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetArtistRequest getArtistRequest = spotifyApi.getArtist(artistId).build();
        return getArtistRequest.execute();
    }
}
