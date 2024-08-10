package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
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
     * @throws PlaylistViewerNextException アーティスト情報の取得中にエラーが発生した場合
     */
    public List<String> getArtistGenres(String artistId) {
        try {
            return Optional.ofNullable(getArtist(artistId))
                    .map(Artist::getGenres)
                    .map(List::of)
                    .orElse(Collections.emptyList());
        } catch (Exception e) {
            // アーティスト情報の取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ARTIST_INFO_RETRIEVAL_ERROR",
                    "アーティスト情報の取得中にエラーが発生しました。",
                    e
            );
        }
    }

    private Artist getArtist(String artistId) throws Exception {
        GetArtistRequest getArtistRequest = spotifyApi.getArtist(artistId).build();
        return getArtistRequest.execute();
    }
}
