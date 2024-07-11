// SpotifyPlaylistSearchServiceTest.java

package com.github.oosm032519.playlistviewernext.service.playlist;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistSearchServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @InjectMocks
    private SpotifyPlaylistSearchService playlistSearchService;

    /**
     * 各テストの前に実行されるセットアップメソッド
     */
    @BeforeEach
    void setUp() {
    }

    /**
     * 正常系: 検索結果がある場合のテスト
     *
     * @throws IOException            入出力例外
     * @throws SpotifyWebApiException Spotify API例外
     * @throws ParseException         パース例外
     */
    @Test
    void testSearchPlaylists_正常系_検索結果あり() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange: テストデータとモックの設定
        String query = "test query";
        int offset = 0;
        int limit = 20;
        SearchPlaylistsRequest.Builder builder = mock(SearchPlaylistsRequest.Builder.class);
        SearchPlaylistsRequest searchPlaylistsRequest = mock(SearchPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistSimplifiedPaging = mock(Paging.class);
        PlaylistSimplified[] playlistSimplifieds = new PlaylistSimplified[]{mock(PlaylistSimplified.class)};

        when(spotifyApi.searchPlaylists(query)).thenReturn(builder);
        when(builder.limit(limit)).thenReturn(builder);
        when(builder.offset(offset)).thenReturn(builder);
        when(builder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenReturn(playlistSimplifiedPaging);
        when(playlistSimplifiedPaging.getItems()).thenReturn(playlistSimplifieds);

        // Act: メソッドの実行
        List<PlaylistSimplified> result = playlistSearchService.searchPlaylists(query, offset, limit);

        // Assert: 結果の検証
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(playlistSimplifieds[0]);
    }

    /**
     * 正常系: 検索結果がない場合のテスト
     *
     * @throws IOException            入出力例外
     * @throws SpotifyWebApiException Spotify API例外
     * @throws ParseException         パース例外
     */
    @Test
    void testSearchPlaylists_正常系_検索結果なし() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange: テストデータとモックの設定
        String query = "empty query";
        int offset = 0;
        int limit = 20;
        SearchPlaylistsRequest.Builder builder = mock(SearchPlaylistsRequest.Builder.class);
        SearchPlaylistsRequest searchPlaylistsRequest = mock(SearchPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistSimplifiedPaging = mock(Paging.class);

        when(spotifyApi.searchPlaylists(query)).thenReturn(builder);
        when(builder.limit(limit)).thenReturn(builder);
        when(builder.offset(offset)).thenReturn(builder);
        when(builder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenReturn(playlistSimplifiedPaging);
        when(playlistSimplifiedPaging.getItems()).thenReturn(null);

        // Act: メソッドの実行
        List<PlaylistSimplified> result = playlistSearchService.searchPlaylists(query, offset, limit);

        // Assert: 結果の検証
        assertThat(result).isEmpty();
    }

    /**
     * 異常系: APIエラーが発生した場合のテスト
     *
     * @throws IOException            入出力例外
     * @throws SpotifyWebApiException Spotify API例外
     * @throws ParseException         パース例外
     */
    @Test
    void testSearchPlaylists_異常系_APIエラー() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange: テストデータとモックの設定
        String query = "test query";
        int offset = 0;
        int limit = 20;
        SearchPlaylistsRequest.Builder builder = mock(SearchPlaylistsRequest.Builder.class);
        SearchPlaylistsRequest searchPlaylistsRequest = mock(SearchPlaylistsRequest.class);

        when(spotifyApi.searchPlaylists(query)).thenReturn(builder);
        when(builder.limit(limit)).thenReturn(builder);
        when(builder.offset(offset)).thenReturn(builder);
        when(builder.build()).thenReturn(searchPlaylistsRequest);
        when(searchPlaylistsRequest.execute()).thenThrow(new IOException("API error"));

        // Act & Assert: メソッドの実行と例外の検証
        assertThatThrownBy(() -> playlistSearchService.searchPlaylists(query, offset, limit))
                .isInstanceOf(IOException.class)
                .hasMessage("API error");
    }
}
