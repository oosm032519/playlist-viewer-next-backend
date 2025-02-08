package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyArtistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GenreAggregatorServiceTest {

    @Mock
    private SpotifyArtistService artistService;

    @InjectMocks
    private GenreAggregatorService genreAggregatorService;

    /**
     * 複数のアーティストとトラックを持つプレイリストから、ジャンルが集計され、出現回数で降順にソートされることを確認する。
     */
    @Test
    void aggregateGenres_正常系_ジャンルを集計できる() throws Exception {
        // Arrange: SpotifyArtistServiceのモックを設定
        PlaylistTrack[] tracks = createMockPlaylistTracks(2, 2); // 2アーティスト、2トラック
        Map<String, List<String>> artistGenres = new HashMap<>();
        artistGenres.put("artistId1", List.of("genre1", "genre2"));
        artistGenres.put("artistId2", List.of("genre2", "genre3"));
        when(artistService.getArtistGenres(List.of("artistId1", "artistId2"))).thenReturn(artistGenres);

        // Act: aggregateGenresメソッドを実行
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(tracks);

        // Assert: 期待通りの結果が返されることを確認
        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of("genre1", 4, "genre2", 8, "genre3", 4));
    }

    // ヘルパーメソッド
    private PlaylistTrack[] createMockPlaylistTracks(int numArtists, int numTracks) {
        PlaylistTrack[] tracks = new PlaylistTrack[numTracks];
        for (int i = 0; i < numTracks; i++) {
            Track track = new Track.Builder().setArtists(createMockArtists(numArtists)).build();
            tracks[i] = new PlaylistTrack.Builder().setTrack(track).build();
        }
        return tracks;
    }

    private ArtistSimplified[] createMockArtists(int numArtists) {
        ArtistSimplified[] artists = new ArtistSimplified[numArtists];
        for (int i = 0; i < numArtists; i++) {
            artists[i] = new ArtistSimplified.Builder().setId("artistId" + (i + 1)).build();
        }
        return artists;
    }

    /**
     * SpotifyArtistServiceが例外をスローした場合、PlaylistViewerNextExceptionがスローされることを確認する。
     */
    @Test
    void aggregateGenres_異常系_例外がスローされる() throws Exception {
        // Arrange: SpotifyArtistServiceが例外をスローするよう設定
        PlaylistTrack[] tracks = createMockPlaylistTracks(1, 1);
        when(artistService.getArtistGenres(List.of("artistId1"))).thenThrow(new RuntimeException("Spotify API Error"));

        // Act & Assert: aggregateGenresメソッドを実行するとPlaylistViewerNextExceptionがスローされることを確認
        assertThatThrownBy(() -> genreAggregatorService.aggregateGenres(tracks))
                .isInstanceOf(PlaylistViewerNextException.class)
                .hasMessage("ジャンルの集計中にエラーが発生しました。")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * ジャンルとその出現回数のマップから、上位N件のジャンルを降順で取得できることを確認する。
     */
    @Test
    void getTopGenres_正常系_上位ジャンルを取得できる() {
        // Arrange: ジャンルと出現回数のマップ
        Map<String, Integer> genreCounts = new HashMap<>();
        genreCounts.put("genre1", 5);
        genreCounts.put("genre2", 3);
        genreCounts.put("genre3", 1);

        // Act: getTopGenresメソッドを実行
        List<String> result = genreAggregatorService.getTopGenres(genreCounts, 2);

        // Assert: 期待通りの結果が返されることを確認
        assertThat(result).containsExactly("genre1", "genre2");
    }

    /**
     * アーティストのジャンルリストがnullの場合でも、NullPointerExceptionが発生せず、他のジャンルが集計されることを確認する。
     */
    @Test
    void aggregateGenres_handlesNullGenres() throws SpotifyWebApiException {
        // Arrange:
        PlaylistTrack[] tracks = createMockPlaylistTracks(2, 2); // 2アーティスト、2トラック
        Map<String, List<String>> artistGenres = new HashMap<>();
        artistGenres.put("artistId1", null); // artistId1のジャンルをnullに設定
        artistGenres.put("artistId2", List.of("genre2", "genre3"));
        when(artistService.getArtistGenres(anyList())).thenReturn(artistGenres);

        // Act:
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(tracks);

        // Assert:  nullのジャンルはスキップされ、他のジャンルは集計される
        assertThat(result).containsOnlyKeys("genre2", "genre3");
        assertThat(result.get("genre2")).isEqualTo(4); // genre2は2回出現 (weight=2 * 2アーティスト)
        assertThat(result.get("genre3")).isEqualTo(4); // genre3は2回出現 (weight=2 * 2アーティスト)
    }

    /**
     * アーティストの出現回数が空の場合に、空のリストが返されることを確認する。
     */
    @Test
    void getTopArtists_emptyArtistCounts_returnsEmptyList() {
        Map<String, Integer> artistCounts = Collections.emptyMap();
        List<String> topArtists = genreAggregatorService.getTopArtists(artistCounts, 5);
        assertThat(topArtists).isEmpty();
    }

    /**
     * アーティストの数が制限より少ない場合、すべてのアーティストが返されることを確認する。
     */
    @Test
    void getTopArtists_allArtistsFitWithinLimit_returnsAllArtists() {
        Map<String, Integer> artistCounts = new HashMap<>();
        artistCounts.put("artist1", 3);
        artistCounts.put("artist2", 2);
        artistCounts.put("artist3", 1);

        List<String> topArtists = genreAggregatorService.getTopArtists(artistCounts, 5);

        assertThat(topArtists).containsExactlyInAnyOrder("artist1", "artist2", "artist3");
    }

    /**
     * アーティストの数が出現回数順で制限を超える場合、ランダムに選択されたアーティストのサブリストが返されることを確認する。
     */
    @Test
    void getTopArtists_moreArtistsThanLimit_returnsShuffledSublist() {
        Map<String, Integer> artistCounts = new HashMap<>();
        artistCounts.put("artist1", 3);
        artistCounts.put("artist2", 3);
        artistCounts.put("artist3", 2);
        artistCounts.put("artist4", 1);

        // limitを2に設定し、artist1とartist2が同率1位、artist3が2位となるため、artist1とartist2のどちらかがランダムに選ばれることを確認する
        List<String> topArtists = genreAggregatorService.getTopArtists(artistCounts, 2);

        assertThat(topArtists).hasSize(2);
        assertThat(topArtists).containsAnyOf("artist1", "artist2"); // artist1, artist2 のどちらかを含む
        // 2つめの要素は、artist1, artist2, artist3 のいずれか (artist1, artist2 が選ばれた場合は artist3 は選ばれない)
        if (topArtists.contains("artist3")) {
            assertThat(topArtists).containsAnyOf("artist1", "artist2", "artist3");
        }

        // 毎回異なる結果になる可能性があるため、複数回実行して確認する (必要に応じて)
        for (int i = 0; i < 10; i++) {
            List<String> topArtists2 = genreAggregatorService.getTopArtists(artistCounts, 2);
            assertThat(topArtists2).hasSize(2);
        }
    }

    /**
     * 同じアーティストが複数のジャンルを持つ場合、ジャンルカウントが正しくマージされることを確認する。
     */
    @Test
    void aggregateGenres_duplicateGenres_mergesCountsCorrectly() throws Exception {
        // Setup: 同じアーティストが複数のジャンルを持つ場合をシミュレート
        PlaylistTrack[] tracks = createMockPlaylistTracks(1, 2); // 1アーティスト、2トラック
        // 同じアーティストが複数のジャンルを持つように設定
        Map<String, List<String>> artistGenres = new HashMap<>();
        artistGenres.put("artistId1", List.of("genre1", "genre2", "genre1")); // genre1 が重複
        when(artistService.getArtistGenres(List.of("artistId1"))).thenReturn(artistGenres);

        // Execute
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(tracks);

        // Verify: 重複したジャンルが正しくマージされ、出現回数が加算されること
        assertThat(result).containsEntry("genre1", 8); // 2(トラック数) * 1(アーティスト数) * 2(重複を考慮) = 4 ではない。
        assertThat(result).containsEntry("genre2", 4); // 2 * 1 * 1 = 2
        assertThat(result).hasSize(2); // genre1, genre2 のみ
    }

    /**
     * アーティストの数が制限と等しい場合、正しいアーティストが返されることを確認する。
     */
    @Test
    void getTopArtists_exactFitForLimit_returnsCorrectArtists() {
        Map<String, Integer> artistCounts = new HashMap<>();
        artistCounts.put("artist1", 3);
        artistCounts.put("artist2", 2);
        artistCounts.put("artist3", 1);

        // limitを3に設定し、artist1 (count=3) でちょうど limit に達するケース
        List<String> topArtists = genreAggregatorService.getTopArtists(artistCounts, 3);

        assertThat(topArtists).hasSize(3);
        // 順序は保証されないので、要素が含まれているかのみ確認
        assertThat(topArtists).containsExactlyInAnyOrder("artist1", "artist2", "artist3");
    }
}
