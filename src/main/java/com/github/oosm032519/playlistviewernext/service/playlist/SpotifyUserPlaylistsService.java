// SpotifyUserPlaylistsService.java

package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
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

@Service
public class SpotifyUserPlaylistsService {
    private final SpotifyApi spotifyApi;
    private final SpotifyAuthService authService;

    /**
     * コンストラクタ。SpotifyApiとSpotifyAuthServiceを注入します。
     *
     * @param spotifyApi  Spotify API クライアント
     * @param authService Spotify 認証サービス
     */
    @Autowired
    public SpotifyUserPlaylistsService(SpotifyApi spotifyApi, SpotifyAuthService authService) {
        this.spotifyApi = spotifyApi;
        this.authService = authService;
    }

    /**
     * 現在のユーザーのプレイリストを取得します。
     *
     * @param authentication OAuth2 認証トークン
     * @return プレイリストのリスト
     * @throws IOException                             入出力例外
     * @throws SpotifyWebApiException                  Spotify API 例外
     * @throws org.apache.hc.core5.http.ParseException パース例外
     */
    public List<PlaylistSimplified> getCurrentUsersPlaylists(OAuth2AuthenticationToken authentication) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // 認証トークンを使用してアクセストークンを設定
        authService.setAccessToken(authentication);

        // 現在のユーザーのプレイリストを取得するリクエストを作成
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();

        // リクエストを実行してプレイリストのページング情報を取得
        Paging<PlaylistSimplified> playlistsPaging = playlistsRequest.execute();

        // プレイリストのアイテムを取得
        PlaylistSimplified[] items = playlistsPaging.getItems();

        // プレイリストのアイテムが存在する場合はリストとして返し、存在しない場合は空のリストを返す
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }
}
