package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class SpotifyPlaylistSearchService {

    private final SpotifyApi spotifyApi;

    public SpotifyPlaylistSearchService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたクエリに基づいてSpotifyのプレイリストを検索します。
     *
     * @param query  検索クエリ
     * @param offset 検索結果のオフセット
     * @param limit  検索結果の最大数
     * @return 検索結果のプレイリストのリスト
     * @throws PlaylistViewerNextException プレイリストの検索中にエラーが発生した場合
     */
    public List<PlaylistSimplified> searchPlaylists(String query, int offset, int limit) {
        try {
            SearchPlaylistsRequest searchRequest = buildSearchRequest(query, offset, limit);
            Paging<PlaylistSimplified> searchResult = executeSearch(searchRequest);
            return getPlaylistsFromResult(searchResult);
        } catch (Exception e) {
            // プレイリストの検索中にエラーが発生した場合は PlaylistViewerNextException をスロー
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PLAYLIST_SEARCH_ERROR",
                    "プレイリストの検索中にエラーが発生しました。",
                    e
            );
        }
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
