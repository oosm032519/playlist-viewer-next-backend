package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.util.*;

/**
 * Spotifyのプレイリスト検索機能を提供するサービスクラス。
 * SpotifyAPIを使用してプレイリストの検索を行い、結果をキャッシュする。
 */
@Service
public class SpotifyPlaylistSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistSearchService.class);

    private final SpotifyApi spotifyApi;

    /**
     * SpotifyPlaylistSearchServiceのコンストラクタ。
     *
     * @param spotifyApi Spotify APIクライアントインスタンス
     */
    public SpotifyPlaylistSearchService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたクエリに基づいてSpotifyのプレイリストを検索する。
     * 結果はキャッシュされ、同じパラメータでの再検索時にはキャッシュから返される。
     *
     * @param query  検索クエリ文字列
     * @param offset 検索結果の開始位置（ページネーション用）
     * @param limit  取得する結果の最大数
     * @return プレイリスト情報と総件数を含むMap。キー"playlists"にプレイリストのリスト、キー"total"に総件数が格納される
     * @throws SpotifyWebApiException Spotify APIでエラーが発生した場合
     */
    @Cacheable(value = "playlistSearch", key = "{#query, #offset, #limit}")
    public Map<String, Object> searchPlaylists(String query, int offset, int limit) throws SpotifyWebApiException {
        return RetryUtil.executeWithRetry(() -> {
            try {
                SearchPlaylistsRequest searchRequest = buildSearchRequest(query, offset, limit);
                Paging<PlaylistSimplified> searchResult = executeSearch(searchRequest);

                List<PlaylistSimplified> playlists = getPlaylistsFromResult(searchResult);

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
     * 検索リクエストを構築する。
     *
     * @param query  検索クエリ
     * @param offset 検索開始位置
     * @param limit  取得件数
     * @return 構築されたSearchPlaylistsRequestオブジェクト
     */
    private SearchPlaylistsRequest buildSearchRequest(String query, int offset, int limit) {
        return spotifyApi.searchPlaylists(query)
                .limit(limit)
                .offset(offset)
                .build();
    }

    /**
     * 検索リクエストを実行する。
     *
     * @param request 実行する検索リクエスト
     * @return 検索結果のPagingオブジェクト
     * @throws Exception 検索実行中にエラーが発生した場合
     */
    private Paging<PlaylistSimplified> executeSearch(SearchPlaylistsRequest request) throws Exception {
        return request.execute();
    }

    /**
     * 検索結果からプレイリストのリストを取得する。
     * 結果がnullの場合は空のリストを返す。
     *
     * @param result 検索結果のPagingオブジェクト
     * @return プレイリストのリスト
     */
    private List<PlaylistSimplified> getPlaylistsFromResult(Paging<PlaylistSimplified> result) {
        return Optional.ofNullable(result.getItems())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }
}
