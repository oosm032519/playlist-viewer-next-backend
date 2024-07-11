// SpotifyPlaylistSearchService.java

package com.github.oosm032519.playlistviewernext.service.playlist;

import org.springframework.beans.factory.annotation.Autowired;
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

@Service
public class SpotifyPlaylistSearchService {

    // Spotify APIのインスタンスを保持するフィールド
    private final SpotifyApi spotifyApi;

    /**
     * コンストラクタ
     *
     * @param spotifyApi Spotify APIのインスタンス
     */
    @Autowired
    public SpotifyPlaylistSearchService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたクエリに基づいてSpotifyのプレイリストを検索するメソッド
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
        // プレイリスト検索リクエストを構築
        SearchPlaylistsRequest searchPlaylistsRequest = spotifyApi.searchPlaylists(query)
                .limit(limit)
                .offset(offset)
                .build();

        // リクエストを実行して検索結果を取得
        Paging<PlaylistSimplified> playlistSimplifiedPaging = searchPlaylistsRequest.execute();

        // 検索結果のプレイリストをリストとして返す
        return playlistSimplifiedPaging.getItems() != null ? Arrays.asList(playlistSimplifiedPaging.getItems()) : Collections.emptyList();
    }
}
