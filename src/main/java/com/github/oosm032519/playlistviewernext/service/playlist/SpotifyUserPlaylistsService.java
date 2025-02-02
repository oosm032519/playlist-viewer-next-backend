package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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
    private final WebClient webClient;

    @Value("${spotify.mock-api.url}")
    private String mockApiUrl;

    @Value("${spotify.mock.enabled:false}")
    private boolean mockEnabled;

    @Autowired
    public SpotifyUserPlaylistsService(SpotifyApi spotifyApi, WebClient.Builder webClientBuilder) {
        this.spotifyApi = spotifyApi;
        webClient = webClientBuilder.build();
    }

    /**
     * 現在のユーザーのプレイリスト一覧を取得する
     *
     * @return プレイリストの簡略情報のリスト
     * @throws AuthenticationException 認証エラーが発生した場合
     */
    public List<PlaylistSimplified> getCurrentUsersPlaylists() throws SpotifyWebApiException {
        if (mockEnabled && mockApiUrl != null && !mockApiUrl.isEmpty()) {
            return getCurrentUsersPlaylistsMock();
        } else {
            return getCurrentUsersPlaylistsReal();
        }
    }

    private List<PlaylistSimplified> getCurrentUsersPlaylistsMock() {
        logger.info("Getting current user's playlists using mock API.");

        // WebClientを使用してモックAPIからデータを取得
        List<PlaylistSimplified> playlists = webClient.get()
                .uri(mockApiUrl + "/following/playlists")
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<PlaylistSimplified>>() {
                })
                .onErrorMap(WebClientResponseException.class, e -> {
                    logger.error("Error calling mock API: {}", e.getResponseBodyAsString(), e);
                    return new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Error calling mock API", e);
                })
                .block();

        return playlists;
    }

    private List<PlaylistSimplified> getCurrentUsersPlaylistsReal() throws SpotifyWebApiException {
        logger.info("Getting current user's playlists using real API.");

        return RetryUtil.executeWithRetry(() -> {
            try {
                OAuth2User oauth2User = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
                String spotifyAccessToken = oauth2User.getAttribute("spotify_access_token");

                if (spotifyAccessToken == null) {
                    throw new AuthenticationException(
                            HttpStatus.UNAUTHORIZED,
                            "アクセストークンが見つかりません。"
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
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfCurrentUsersPlaylists()
                .limit(50)
                .build();
        Paging<PlaylistSimplified> playlistsPaging = playlistsRequest.execute();
        return Optional.ofNullable(playlistsPaging.getItems())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }
}
