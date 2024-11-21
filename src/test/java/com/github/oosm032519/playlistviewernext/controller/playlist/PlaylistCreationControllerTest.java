package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.model.CreatePlaylistRequest;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistCreationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistCreationControllerTest {

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private PlaylistCreationController playlistCreationController;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm:ss");

    @Test
    void createPlaylist_success() throws SpotifyWebApiException {
        // テストデータの準備
        List<String> trackIds = List.of("trackId1", "trackId2");
        String accessToken = "accessToken";
        String userId = "userId";
        String userName = "userName";
        String playlistName = String.format("%s さんへのおすすめ楽曲 %s", userName, LocalDateTime.now().format(DATE_TIME_FORMATTER));
        String playlistId = "playlistId";

        // CreatePlaylistRequestオブジェクトを作成
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setTrackIds(trackIds);
        request.setPlaylistName(null); // 名前を指定しない場合

        // モックの設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttribute("name")).thenReturn(userName);
        when(spotifyUserPlaylistCreationService.createPlaylist(accessToken, userId, playlistName, trackIds)).thenReturn(playlistId);

        // テスト対象メソッドの実行
        ResponseEntity<String> response = playlistCreationController.createPlaylist(request, principal);

        // アサーション
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(String.format("{\"playlistId\": \"%s\"}", playlistId));
    }

    @Test
    void createPlaylist_authenticationError() {
        // テストデータの準備
        List<String> trackIds = List.of("trackId1", "trackId2");

        // CreatePlaylistRequestオブジェクトを作成
        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setTrackIds(trackIds);

        // モックの設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(null);

        // テスト対象メソッドの実行とアサーション
        assertThrows(AuthenticationException.class, () -> playlistCreationController.createPlaylist(request, principal));
    }

    @Test
    void generatePlaylistName() {
        String userName = "testUser";

        String actualPlaylistName = playlistCreationController.generatePlaylistName(userName);

        // AssertJを使用してアサーション
        assertThat(actualPlaylistName).startsWith(userName + " さんへのおすすめ楽曲 ");
    }
}
