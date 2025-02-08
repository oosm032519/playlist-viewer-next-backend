package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spotifyのプレイリスト検索機能を提供するサービスクラス。
 * SpotifyAPIを使用してプレイリストの検索を行い、結果をキャッシュする。
 */
@Service
public class SpotifyPlaylistSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistSearchService.class);

    private final SpotifyApi spotifyApi;
    private final WebClient webClient;

    /**
     * モックAPIサーバーのURL
     */
    @Value("${spotify.mock-api.url}")
    private String mockApiUrl;

    /**
     * モックモードの有効/無効
     */
    @Value("${spotify.mock.enabled:false}")
    private boolean mockEnabled;

    /**
     * SpotifyPlaylistSearchServiceのコンストラクタ。
     *
     * @param spotifyApi       Spotify APIクライアントインスタンス
     * @param webClientBuilder WebClientのビルダー
     */
    @Autowired
    public SpotifyPlaylistSearchService(SpotifyApi spotifyApi, WebClient.Builder webClientBuilder) {
        this.spotifyApi = spotifyApi;
        webClient = webClientBuilder.build();
    }

    /**
     * 指定されたクエリに基づいてSpotifyのプレイリストを検索する。
     * 結果はキャッシュされ、同じパラメータでの再検索時にはキャッシュから返される。
     * モックモードが有効で、かつモックAPIのURLが設定されている場合は、モックAPIサーバーからデータを取得する。
     *
     * @param query  検索クエリ文字列
     * @param offset 検索結果の開始位置（ページネーション用）
     * @param limit  取得する結果の最大数
     * @return プレイリスト情報と総件数を含むMap。キー"playlists"にプレイリストのリスト、キー"total"に総件数が格納される
     * @throws SpotifyWebApiException Spotify APIでエラーが発生した場合
     */
    public Map<String, Object> searchPlaylists(String query, int offset, int limit) throws SpotifyWebApiException {
        // モックモードが有効かつ、モックAPIのURLが設定されている場合のみモックAPIを呼び出す
        if (mockEnabled && mockApiUrl != null && !mockApiUrl.isEmpty()) {
            return searchPlaylistsMock(query, offset, limit);
        } else {
            return searchPlaylistsReal(query, offset, limit);
        }
    }

    /**
     * モックAPIサーバーを使用してプレイリストを検索する。
     *
     * @param query  検索クエリ文字列
     * @param offset 検索結果の開始位置（ページネーション用）
     * @param limit  取得する結果の最大数
     * @return プレイリスト情報と総件数を含むMap
     */
    private Map<String, Object> searchPlaylistsMock(String query, int offset, int limit) {
        logger.info("Searching playlists using mock API. Query: {}, Offset: {}, Limit: {}", query, offset, limit);

        return webClient.get()
                .uri(mockApiUrl + "/search/playlists?query={query}&offset={offset}&limit={limit}", query, offset, limit)
                .retrieve()
                .bodyToMono(Map.class)
                .onErrorMap(WebClientResponseException.class, e -> {
                    logger.error("Error calling mock API: {}", e.getResponseBodyAsString(), e);
                    return new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Error calling mock API", e);
                })
                .block();
    }

    /**
     * Spotify APIを使用してプレイリストを検索する。
     *
     * @param query  検索クエリ文字列
     * @param offset 検索結果の開始位置（ページネーション用）
     * @param limit  取得する結果の最大数
     * @return プレイリスト情報と総件数を含むMap
     * @throws SpotifyWebApiException Spotify APIでエラーが発生した場合
     */
    private Map<String, Object> searchPlaylistsReal(String query, int offset, int limit) throws SpotifyWebApiException {
        logger.info("Searching playlists using real API. Query: {}, Offset: {}, Limit: {}", query, offset, limit);

        return RetryUtil.executeWithRetry(() -> {
            try {
                SearchPlaylistsRequest searchRequest = spotifyApi.searchPlaylists(query)
                        .limit(limit)
                        .offset(offset)
                        .build();
                Paging<PlaylistSimplified> searchResult = searchRequest.execute();

                // Convert PlaylistSimplified to a Map
                List<Map<String, Object>> playlists = Arrays.stream(searchResult.getItems())
                        .map(this::convertToMap)
                        .collect(Collectors.toList());

                // 検索結果をMapにまとめる
                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("playlists", playlists);
                resultMap.put("total", searchResult.getTotal());

                return resultMap;
            } catch (SpotifyWebApiException e) {
                logger.error("Spotify API エラー: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                logger.error("Spotifyプレイリストの検索中にエラーが発生しました。 query: {}, offset: {}, limit: {}", query, offset, limit, e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Spotifyプレイリストの検索中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS);
    }

    /**
     * PlaylistSimplified オブジェクトを Map に変換する。
     *
     * @param playlist 変換する PlaylistSimplified オブジェクト
     * @return PlaylistSimplified オブジェクトの情報を格納した Map
     */
    public Map<String, Object> convertToMap(PlaylistSimplified playlist) {
        Map<String, Object> playlistMap = new HashMap<>();
        playlistMap.put("id", playlist.getId());
        playlistMap.put("name", playlist.getName());
        playlistMap.put("tracks", Map.of("total", playlist.getTracks().getTotal()));
        playlistMap.put("images", Arrays.stream(playlist.getImages()).map(image -> Map.of("url", image.getUrl())).collect(Collectors.toList()));
        playlistMap.put("externalUrls", playlist.getExternalUrls() != null ? Map.of("spotify", playlist.getExternalUrls().getExternalUrls().get("spotify")) : Collections.emptyMap());
        playlistMap.put("owner", Map.of("displayName", playlist.getOwner().getDisplayName()));
        return playlistMap;
    }
}
