package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyArtistService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import se.michaelthelin.spotify.model_objects.specification.ArtistSimplified;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class GenreAggregatorServiceTest {

    @Mock
    private SpotifyArtistService artistService;

    @InjectMocks
    private GenreAggregatorService genreAggregatorService;

    @Test
    void aggregateGenres_正常系_ジャンルを集計できる() throws Exception {
        // given: SpotifyArtistServiceのモックを設定
        PlaylistTrack[] tracks = createMockPlaylistTracks(2, 2); // 2アーティスト、2トラック
        Map<String, List<String>> artistGenres = new HashMap<>();
        artistGenres.put("artistId1", List.of("genre1", "genre2"));
        artistGenres.put("artistId2", List.of("genre2", "genre3"));
        when(artistService.getArtistGenres(List.of("artistId1", "artistId2"))).thenReturn(artistGenres);

        // when: aggregateGenresメソッドを実行
        Map<String, Integer> result = genreAggregatorService.aggregateGenres(tracks);

        // then: 期待通りの結果が返されることを確認
        assertThat(result).containsExactlyInAnyOrderEntriesOf(Map.of("genre1", 4, "genre2", 8, "genre3", 4));
    }

    // --- Helper methods ---
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

    @Test
    void aggregateGenres_異常系_例外がスローされる() throws Exception {
        // given: SpotifyArtistServiceが例外をスローするよう設定
        PlaylistTrack[] tracks = createMockPlaylistTracks(1, 1);
        when(artistService.getArtistGenres(List.of("artistId1"))).thenThrow(new RuntimeException("Spotify API Error"));

        // when & then: aggregateGenresメソッドを実行するとPlaylistViewerNextExceptionがスローされることを確認
        assertThatThrownBy(() -> genreAggregatorService.aggregateGenres(tracks))
                .isInstanceOf(PlaylistViewerNextException.class)
                .hasMessage("ジャンルの集計中にエラーが発生しました。")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void getTopGenres_正常系_上位ジャンルを取得できる() {
        // given: ジャンルと出現回数のマップ
        Map<String, Integer> genreCounts = new HashMap<>();
        genreCounts.put("genre1", 5);
        genreCounts.put("genre2", 3);
        genreCounts.put("genre3", 1);

        // when: getTopGenresメソッドを実行
        List<String> result = genreAggregatorService.getTopGenres(genreCounts, 2);

        // then: 期待通りの結果が返されることを確認
        assertThat(result).containsExactly("genre1", "genre2");
    }
}
