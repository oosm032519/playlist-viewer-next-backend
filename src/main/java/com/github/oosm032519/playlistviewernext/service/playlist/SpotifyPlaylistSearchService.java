package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.util.*;

@Service
public class SpotifyPlaylistSearchService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistSearchService.class);

    private final SpotifyApi spotifyApi;

    public SpotifyPlaylistSearchService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたクエリに基づいてSpotifyのプレイリストを検索する
     *
     * @param query  検索クエリ
     * @param offset 検索結果のオフセット
     * @param limit  検索結果の最大数
     * @return 検索結果のプレイリストと総数を含むマップ
     * @throws SpotifyApiException プレイリストの検索中にエラーが発生した場合
     */
    public Map<String, Object> searchPlaylists(String query, int offset, int limit) {
        return RetryUtil.executeWithRetry(() -> {
            try {
                SearchPlaylistsRequest searchRequest = buildSearchRequest(query, offset, limit);
                Paging<PlaylistSimplified> searchResult = executeSearch(searchRequest);

                List<PlaylistSimplified> playlists = getPlaylistsFromResult(searchResult);

                Map<String, Object> resultMap = new HashMap<>();
                resultMap.put("playlists", playlists);
                resultMap.put("total", searchResult.getTotal());

                return resultMap;
            } catch (Exception e) {
                // プレイリストの検索中にエラーが発生した場合は SpotifyApiException をスロー
                logger.error("Spotifyプレイリストの検索中にエラーが発生しました。 query: {}, offset: {}, limit: {}", query, offset, limit, e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "Spotifyプレイリストの検索中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }

    private SearchPlaylistsRequest buildSearchRequest(String query, int offset, int limit) {
        return spotifyApi.searchPlaylists(query)
                .limit(limit)
                .offset(offset)
                .build();
    }

    private Paging<PlaylistSimplified> executeSearch(SearchPlaylistsRequest request) throws Exception {
        return request.execute();
    }

    private List<PlaylistSimplified> getPlaylistsFromResult(Paging<PlaylistSimplified> result) {
        return Optional.ofNullable(result.getItems())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }
}
