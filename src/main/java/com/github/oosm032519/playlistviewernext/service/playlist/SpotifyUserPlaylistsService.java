package com.github.oosm032519.playlistviewernext.service.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class SpotifyUserPlaylistsService {
    private final SpotifyApi spotifyApi;

    /**
     * SpotifyUserPlaylistsServiceのコンストラクタ。
     *
     * @param spotifyApi Spotify API クライアント
     */
    @Autowired
    public SpotifyUserPlaylistsService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 現在のユーザーのプレイリストを取得します。
     *
     * @return プレイリストのリスト
     * @throws IOException                             入出力例外
     * @throws SpotifyWebApiException                  Spotify API 例外
     * @throws org.apache.hc.core5.http.ParseException パース例外
     */
    public List<PlaylistSimplified> getCurrentUsersPlaylists() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // SecurityContextHolder から OAuth2User を取得
        OAuth2User oauth2User = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // OAuth2User から spotify_access_token を取得
        String spotifyAccessToken = oauth2User.getAttribute("spotify_access_token");

        // SpotifyApi にアクセストークンを設定
        spotifyApi.setAccessToken(spotifyAccessToken);

        return getPlaylists();
    }

    private List<PlaylistSimplified> getPlaylists() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();
        Paging<PlaylistSimplified> playlistsPaging = playlistsRequest.execute();
        return Optional.ofNullable(playlistsPaging.getItems())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }
}
