package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyPlaylistTrackAdditionServiceTest {

    @InjectMocks
    private SpotifyPlaylistTrackAdditionService spotifyPlaylistTrackAdditionService;

    @Mock
    private SpotifyApi spotifyApi;

    @Test
    void addTrackToPlaylist_正常系() throws Exception {
        // 準備
        String accessToken = "accessToken";
        String playlistId = "playlistId";
        String trackId = "trackId";
        SnapshotResult snapshotResult = mock(SnapshotResult.class);
        AddItemsToPlaylistRequest.Builder builder = mock(AddItemsToPlaylistRequest.Builder.class);
        AddItemsToPlaylistRequest request = mock(AddItemsToPlaylistRequest.class);

        doReturn(builder).when(spotifyApi).addItemsToPlaylist(eq(playlistId), any(String[].class));
        doReturn(request).when(builder).build();
        doReturn(snapshotResult).when(request).execute();

        // 実行
        SnapshotResult result = spotifyPlaylistTrackAdditionService.addTrackToPlaylist(accessToken, playlistId, trackId);

        // 検証
        assertThat(result).isEqualTo(snapshotResult);
    }

    @Test
    void addTrackToPlaylist_SpotifyWebApiException発生時_例外がそのままスローされる() throws Exception {
        // 準備
        String accessToken = "accessToken";
        String playlistId = "playlistId";
        String trackId = "trackId";
        SpotifyWebApiException exception = new SpotifyWebApiException();
        AddItemsToPlaylistRequest.Builder builder = mock(AddItemsToPlaylistRequest.Builder.class);
        AddItemsToPlaylistRequest request = mock(AddItemsToPlaylistRequest.class);

        doReturn(builder).when(spotifyApi).addItemsToPlaylist(eq(playlistId), any(String[].class));
        doReturn(request).when(builder).build();
        doThrow(exception).when(request).execute();

        // 実行・検証
        assertThatThrownBy(() -> spotifyPlaylistTrackAdditionService.addTrackToPlaylist(accessToken, playlistId, trackId))
                .isInstanceOf(SpotifyWebApiException.class)
                .isSameAs(exception);
    }

    @Test
    void addTrackToPlaylist_その他例外発生時_InternalServerExceptionにラップされてスローされる() throws Exception {
        // 準備
        String accessToken = "accessToken";
        String playlistId = "playlistId";
        String trackId = "trackId";
        RuntimeException exception = new RuntimeException("テスト例外");
        AddItemsToPlaylistRequest.Builder builder = mock(AddItemsToPlaylistRequest.Builder.class);
        AddItemsToPlaylistRequest request = mock(AddItemsToPlaylistRequest.class);

        doReturn(builder).when(spotifyApi).addItemsToPlaylist(eq(playlistId), any(String[].class));
        doReturn(request).when(builder).build();
        doThrow(exception).when(request).execute();


        // 実行・検証
        assertThatThrownBy(() -> spotifyPlaylistTrackAdditionService.addTrackToPlaylist(accessToken, playlistId, trackId))
                .isInstanceOf(InternalServerException.class)
                .hasCause(exception)
                .hasMessage("トラックの追加中にエラーが発生しました。")
                .extracting("httpStatus").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void addTrackToPlaylist_リトライ内でSpotifyWebApiExceptionが発生し続ける場合_最終的にSpotifyWebApiExceptionがスローされる() throws Exception {
        // 準備
        String accessToken = "accessToken";
        String playlistId = "playlistId";
        String trackId = "trackId";
        SpotifyWebApiException exception = new SpotifyWebApiException();
        AddItemsToPlaylistRequest.Builder builder = mock(AddItemsToPlaylistRequest.Builder.class);
        AddItemsToPlaylistRequest request = mock(AddItemsToPlaylistRequest.class);

        doReturn(builder).when(spotifyApi).addItemsToPlaylist(eq(playlistId), any(String[].class));
        doReturn(request).when(builder).build();
        doThrow(exception).when(request).execute();

        // 実行・検証
        assertThatThrownBy(() -> spotifyPlaylistTrackAdditionService.addTrackToPlaylist(accessToken, playlistId, trackId))
                .isInstanceOf(SpotifyWebApiException.class)
                .isSameAs(exception);
    }
}
