// SpotifyArtistService.java

package com.github.oosm032519.playlistviewernext.service.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SpotifyArtistService {
    private final SpotifyApi spotifyApi;

    /**
     * コンストラクタでSpotifyApiインスタンスを注入します。
     *
     * @param spotifyApi Spotify APIのインスタンス
     */
    @Autowired
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
        // アーティスト情報を取得するリクエストを構築
        GetArtistRequest getArtistRequest = spotifyApi.getArtist(artistId).build();

        // リクエストを実行してアーティスト情報を取得
        Artist artist = getArtistRequest.execute();

        // アーティストのジャンルをリストとして返す。ジャンルがnullの場合は空のリストを返す
        return artist.getGenres() != null ? Arrays.asList(artist.getGenres()) : Collections.emptyList();
    }
}
