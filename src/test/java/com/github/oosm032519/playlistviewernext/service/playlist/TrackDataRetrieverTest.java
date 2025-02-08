package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackDataRetrieverTest {

    @Mock
    public SpotifyTrackService trackService;

    @InjectMocks
    public TrackDataRetriever trackDataRetriever;

    /**
     * tracks配列がnullの場合、空のリストが返されることを確認する。
     */
    @Test
    void getTrackListData_tracks配列がnullの場合_空のリストを返す() {
        // Act: テスト対象メソッドの実行
        List<Map<String, Object>> result = trackDataRetriever.getTrackListData(null);

        // Assert: 結果の検証
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    /**
     * tracks配列が空の場合、空のリストが返されることを確認する。
     */
    @Test
    void getTrackListData_tracks配列が空の場合_空のリストを返す() {
        // Act: テスト対象メソッドの実行
        PlaylistTrack[] emptyTracks = new PlaylistTrack[0];
        List<Map<String, Object>> result = trackDataRetriever.getTrackListData(emptyTracks);

        // Assert: 結果の検証
        assertThat(result).isEqualTo(Collections.emptyList());
    }

    /**
     * PlaylistTrackのTrackがnullの場合、trackとaudioFeaturesがnullの要素を含むリストが返されることを確認する。
     */
    @Test
    void getTrackListData_PlaylistTrackのTrackがnullの場合_trackとaudioFeaturesがnullの要素を含むリストを返す() throws Exception {
        // Arrange: テストデータの準備
        PlaylistTrack mockPlaylistTrack = mock(PlaylistTrack.class);
        when(mockPlaylistTrack.getTrack()).thenReturn(null); // trackがnull
        PlaylistTrack[] tracks = {mockPlaylistTrack};

        // Act: テスト対象メソッドの実行
        List<Map<String, Object>> result = trackDataRetriever.getTrackListData(tracks);

        // Assert: 結果の検証
        verify(trackService, never()).getAudioFeaturesForTracks(anyList()); // trackServiceは呼ばれない
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().get("track")).isNull();
        assertThat(result.getFirst().get("audioFeatures")).isNull();
    }

    /**
     * trackIdsが空の場合、audioFeaturesListが空リストで初期化されることを確認する。
     */
    @Test
    void getTrackListData_trackIdsが空の場合_audioFeaturesListが空リストで初期化される() throws Exception {
        // Arrange: PlaylistTrackのtrackはnullではないが、trackIdはnull
        PlaylistTrack mockPlaylistTrack = mock(PlaylistTrack.class);
        Track mockTrack = mock(Track.class);
        when(mockPlaylistTrack.getTrack()).thenReturn(mockTrack);
        when(mockTrack.getId()).thenReturn(null); // trackIdをnullにする

        PlaylistTrack[] tracks = {mockPlaylistTrack};

        // Act: テスト対象メソッドの実行
        List<Map<String, Object>> result = trackDataRetriever.getTrackListData(tracks);

        // Assert: 結果の検証
        verify(trackService, never()).getAudioFeaturesForTracks(anyList()); // trackServiceは呼ばれない
        assertThat(result).hasSize(1);
        assertThat(result.getFirst().get("track")).isSameAs(mockTrack); // trackはnullではない
        assertThat(result.getFirst().get("audioFeatures")).isNull(); // audioFeaturesはnull
    }

    /**
     * trackService.getAudioFeaturesForTracksが例外をスローした場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void getTrackListData_例外発生時にInternalServerExceptionをスローする() throws Exception {
        // Arrange: 例外をスローするモックの設定
        PlaylistTrack mockPlaylistTrack = mock(PlaylistTrack.class);
        Track mockTrack = mock(Track.class);
        when(mockPlaylistTrack.getTrack()).thenReturn(mockTrack);
        when(mockTrack.getId()).thenReturn("validId"); // trackId は null でない
        PlaylistTrack[] tracks = {mockPlaylistTrack};

        // trackService.getAudioFeaturesForTracks が例外をスローするように設定
        when(trackService.getAudioFeaturesForTracks(anyList())).thenThrow(new SpotifyWebApiException("Test Exception"));

        // Act & Assert: InternalServerExceptionがスローされることの確認
        assertThatThrownBy(() -> trackDataRetriever.getTrackListData(tracks))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("トラックデータの取得中にエラーが発生しました。");
    }

    /**
     * PlaylistTrackのTrackが一部nullの場合、audioFeaturesがnullになる場合があることを確認する。
     */
    @Test
    void getTrackListData_PlaylistTrackのTrackが一部nullの場合_audioFeaturesがnullになる場合がある() throws Exception {
        // Arrange: テストデータの準備
        PlaylistTrack mockPlaylistTrack1 = mock(PlaylistTrack.class);
        Track mockTrack1 = mock(Track.class);
        when(mockPlaylistTrack1.getTrack()).thenReturn(mockTrack1);
        when(mockTrack1.getId()).thenReturn("track1"); // 有効なID

        PlaylistTrack mockPlaylistTrack2 = mock(PlaylistTrack.class);
        when(mockPlaylistTrack2.getTrack()).thenReturn(null); // trackがnull

        PlaylistTrack[] tracks = {mockPlaylistTrack1, mockPlaylistTrack2};

        AudioFeatures mockAudioFeatures = mock(AudioFeatures.class);
        when(trackService.getAudioFeaturesForTracks(List.of("track1"))).thenReturn(List.of(mockAudioFeatures));

        // Act: テスト対象メソッドの実行
        List<Map<String, Object>> result = trackDataRetriever.getTrackListData(tracks);

        // Assert: 結果の検証
        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("track")).isSameAs(mockTrack1);
        assertThat(result.get(0).get("audioFeatures")).isSameAs(mockAudioFeatures); // 1つ目はAudioFeaturesがある
        assertThat(result.get(1).get("track")).isNull(); // 2つ目はtrackがnull
        assertThat(result.get(1).get("audioFeatures")).isNull(); // 2つ目はAudioFeaturesもnull
    }

    /**
     * 正常なトラックデータとAudioFeaturesが与えられた場合、トラック情報とAudioFeaturesが正しくマッピングされることを確認する。
     */
    @Test
    void getTrackListData_正常系_AudioFeaturesが正しくマッピングされる() throws Exception {
        // Arrange: テストデータの準備
        PlaylistTrack mockPlaylistTrack1 = mock(PlaylistTrack.class);
        Track mockTrack1 = mock(Track.class);
        when(mockPlaylistTrack1.getTrack()).thenReturn(mockTrack1);
        when(mockTrack1.getId()).thenReturn("track1");

        PlaylistTrack mockPlaylistTrack2 = mock(PlaylistTrack.class);
        Track mockTrack2 = mock(Track.class);
        when(mockPlaylistTrack2.getTrack()).thenReturn(mockTrack2);
        when(mockTrack2.getId()).thenReturn("track2");

        PlaylistTrack[] tracks = {mockPlaylistTrack1, mockPlaylistTrack2};

        AudioFeatures mockAudioFeatures1 = mock(AudioFeatures.class);
        AudioFeatures mockAudioFeatures2 = mock(AudioFeatures.class);
        when(trackService.getAudioFeaturesForTracks(List.of("track1", "track2"))).thenReturn(List.of(mockAudioFeatures1, mockAudioFeatures2));

        // Act: テスト対象メソッドの実行
        List<Map<String, Object>> result = trackDataRetriever.getTrackListData(tracks);

        // Assert: 結果の検証
        assertThat(result).hasSize(2);
        assertThat(result.get(0).get("track")).isSameAs(mockTrack1);
        assertThat(result.get(0).get("audioFeatures")).isSameAs(mockAudioFeatures1);
        assertThat(result.get(1).get("track")).isSameAs(mockTrack2);
        assertThat(result.get(1).get("audioFeatures")).isSameAs(mockAudioFeatures2);
    }
}
