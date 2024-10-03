package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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

/**
 * Spotifyユーザーのプレイリスト情報を取得するサービスクラス
 * このクラスはSpotify APIを使用して、現在認証されているユーザーのプレイリスト一覧を取得する
 */
@Service
public class SpotifyUserPlaylistsService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyUserPlaylistsService.class);

    private final SpotifyApi spotifyApi;

    /**
     * SpotifyApiインスタンスを注入するコンストラクタ
     *
     * @param spotifyApi Spotify APIクライアント
     */
    public SpotifyUserPlaylistsService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 現在のユーザーのプレイリスト一覧を取得する
     *
     * @return プレイリストの簡略情報のリスト
     * @throws AuthenticationException 認証エラーが発生した場合
     */
    public List<PlaylistSimplified> getCurrentUsersPlaylists() {
        return RetryUtil.executeWithRetry(() -> {
            try {
                OAuth2User oauth2User = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String spotifyAccessToken = oauth2User.getAttribute("spotify_access_token");

                if (spotifyAccessToken == null) {
                    throw new AuthenticationException(
                            HttpStatus.UNAUTHORIZED,
                            "AUTHENTICATION_ERROR",
                            "Spotify access token is missing"
                    );
                }

                // アクセストークンをSpotifyApiインスタンスにセット
                spotifyApi.setAccessToken(spotifyAccessToken);
                return getPlaylists();
            } catch (AuthenticationException e) {
                // AuthenticationException はそのまま再スロー
                throw e;
            } catch (SpotifyWebApiException e) {
                // SpotifyWebApiException はそのまま再スロー
                logger.error("Spotify API エラー: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                logger.error("Error occurred while retrieving playlists", e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Error occurred while retrieving playlists",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }

    /**
     * Spotify APIを使用してプレイリスト一覧を取得する
     *
     * @return プレイリストの簡略情報のリスト
     * @throws IOException                             Spotify APIの呼び出し中にIOエラーが発生した場合
     * @throws SpotifyWebApiException                  Spotify APIの呼び出し中にエラーが発生した場合
     * @throws org.apache.hc.core5.http.ParseException Spotify APIのレスポンスのパース中にエラーが発生した場合
     */
    private List<PlaylistSimplified> getPlaylists() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();
        Paging<PlaylistSimplified> playlistsPaging = playlistsRequest.execute();
        return Optional.ofNullable(playlistsPaging.getItems())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }
}
