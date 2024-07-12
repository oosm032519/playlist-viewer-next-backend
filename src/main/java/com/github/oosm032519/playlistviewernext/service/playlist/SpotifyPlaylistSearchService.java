package com.github.oosm032519.playlistviewernext.service.playlist;

import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.io.IOException;
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
     * @throws IOException                             入出力例外
     * @throws SpotifyWebApiException                  Spotify API例外
     * @throws org.apache.hc.core5.http.ParseException パース例外
     */
    public List<PlaylistSimplified> searchPlaylists(String query, int offset, int limit) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        SearchPlaylistsRequest searchRequest = buildSearchRequest(query, offset, limit);
        Paging<PlaylistSimplified> searchResult = executeSearch(searchRequest);
        return getPlaylistsFromResult(searchResult);
    }

    private SearchPlaylistsRequest buildSearchRequest(String query, int offset, int limit) {
        return spotifyApi.searchPlaylists(query)
                .limit(limit)
                .offset(offset)
                .build();
    }

    private Paging<PlaylistSimplified> executeSearch(SearchPlaylistsRequest request) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        return request.execute();
    }

    private List<PlaylistSimplified> getPlaylistsFromResult(Paging<PlaylistSimplified> result) {
        return Optional.ofNullable(result.getItems())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }
}
