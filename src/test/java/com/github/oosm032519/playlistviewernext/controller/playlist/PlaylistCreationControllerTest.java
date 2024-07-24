package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistCreationService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlaylistCreationControllerTest {

    @Mock
    private SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService;

    @InjectMocks
    private PlaylistCreationController playlistCreationController;

    private MockHttpSession mockSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockSession = new MockHttpSession();
    }

    @Test
    void createPlaylist_Success() throws IOException, ParseException, SpotifyWebApiException {
        // テストデータの準備
        List<String> trackIds = Arrays.asList("track1", "track2", "track3");
        String accessToken = "testAccessToken";
        String userId = "testUserId";
        String displayName = "testUser";
        String expectedPlaylistId = "newPlaylistId";

        // セッション属性の設定
        mockSession.setAttribute("accessToken", accessToken);
        mockSession.setAttribute("userId", userId);
        mockSession.setAttribute("displayName", displayName);

        // モックの設定
        when(spotifyUserPlaylistCreationService.createPlaylist(eq(accessToken), eq(userId), anyString(), eq(trackIds)))
                .thenReturn(expectedPlaylistId);

        // メソッドの実行
        ResponseEntity<String> response = playlistCreationController.createPlaylist(trackIds, mockSession);

        // 検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(expectedPlaylistId);
        verify(spotifyUserPlaylistCreationService).createPlaylist(eq(accessToken), eq(userId), anyString(), eq(trackIds));
    }

    @Test
    void createPlaylist_Unauthorized() {
        // テストデータの準備
        List<String> trackIds = Arrays.asList("track1", "track2", "track3");

        // セッション属性を設定しない（認証されていない状態をシミュレート）

        // メソッドの実行
        ResponseEntity<String> response = playlistCreationController.createPlaylist(trackIds, mockSession);

        // 検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).contains("認証が必要です。");
        verifyNoInteractions(spotifyUserPlaylistCreationService);
    }

    @Test
    void createPlaylist_ServiceException() throws IOException, ParseException, SpotifyWebApiException {
        // テストデータの準備
        List<String> trackIds = Arrays.asList("track1", "track2", "track3");
        String accessToken = "testAccessToken";
        String userId = "testUserId";
        String displayName = "testUser";

        // セッション属性の設定
        mockSession.setAttribute("accessToken", accessToken);
        mockSession.setAttribute("userId", userId);
        mockSession.setAttribute("displayName", displayName);

        // モックの設定
        when(spotifyUserPlaylistCreationService.createPlaylist(eq(accessToken), eq(userId), anyString(), eq(trackIds)))
                .thenThrow(new RuntimeException("Service error"));

        // メソッドの実行
        ResponseEntity<String> response = playlistCreationController.createPlaylist(trackIds, mockSession);

        // 検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("エラー: Service error");
        verify(spotifyUserPlaylistCreationService).createPlaylist(eq(accessToken), eq(userId), anyString(), eq(trackIds));
    }

    @Test
    void createPlaylist_NullDisplayName() throws IOException, ParseException, SpotifyWebApiException {
        // テストデータの準備
        List<String> trackIds = Arrays.asList("track1", "track2", "track3");
        String accessToken = "testAccessToken";
        String userId = "testUserId";
        String expectedPlaylistId = "newPlaylistId";

        // セッション属性の設定（displayNameを設定しない）
        mockSession.setAttribute("accessToken", accessToken);
        mockSession.setAttribute("userId", userId);

        // モックの設定
        when(spotifyUserPlaylistCreationService.createPlaylist(eq(accessToken), eq(userId), anyString(), eq(trackIds)))
                .thenReturn(expectedPlaylistId);

        // メソッドの実行
        ResponseEntity<String> response = playlistCreationController.createPlaylist(trackIds, mockSession);

        // 検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains(expectedPlaylistId);
        verify(spotifyUserPlaylistCreationService).createPlaylist(eq(accessToken), eq(userId), argThat(arg -> arg.contains("あなた")), eq(trackIds));
    }
}
