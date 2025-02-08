package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    /**
     * プレイリスト作成リクエストが成功した場合、HTTPステータスコード200とプレイリストIDを含むレスポンスが返されることを確認する。
     */
    @Test
    void createPlaylist_success() throws SpotifyWebApiException {
        // Arrange: テストデータの準備
        List<String> trackIds = List.of("trackId1", "trackId2");
        String accessToken = "accessToken";
        String userId = "userId";
        String userName = "userName";
        String playlistName = String.format("%s さんへのおすすめ楽曲 %s", userName, LocalDateTime.now().format(DATE_TIME_FORMATTER));
        String playlistId = "playlistId";

        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setTrackIds(trackIds);
        request.setPlaylistName(null); // 名前を指定しない場合

        // モックの設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttribute("name")).thenReturn(userName);
        when(spotifyUserPlaylistCreationService.createPlaylist(accessToken, userId, playlistName, trackIds)).thenReturn(playlistId);

        // Act: テスト対象メソッドの実行
        ResponseEntity<String> response = playlistCreationController.createPlaylist(request, principal);

        // Assert: アサーション
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(String.format("{\"playlistId\": \"%s\"}", playlistId));
    }

    /**
     * 認証エラーが発生した場合、AuthenticationExceptionがスローされることを確認する。
     */
    @Test
    void createPlaylist_authenticationError() {
        // Arrange: テストデータの準備
        List<String> trackIds = List.of("trackId1", "trackId2");

        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setTrackIds(trackIds);

        // モックの設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(null);

        // Act & Assert: テスト対象メソッドの実行とアサーション
        // JUnitのassertThrowsからAssertJのassertThatThrownByに変更
        assertThatThrownBy(() -> playlistCreationController.createPlaylist(request, principal))
                .isInstanceOf(AuthenticationException.class);
    }

    /**
     * SpotifyWebApiExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void createPlaylist_spotifyWebApiException() throws SpotifyWebApiException {
        // Arrange:
        List<String> trackIds = List.of("trackId1", "trackId2");
        String accessToken = "accessToken";
        String userId = "userId";
        String userName = "userName";
        String playlistName = String.format("%s さんへのおすすめ楽曲 %s", userName, LocalDateTime.now().format(DATE_TIME_FORMATTER));

        CreatePlaylistRequest request = new CreatePlaylistRequest();
        request.setTrackIds(trackIds);
        request.setPlaylistName(null); // 名前を指定しない場合

        // モックの設定
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttribute("name")).thenReturn(userName);
        // SpotifyWebApiExceptionをInternalServerExceptionでラップするように変更
        when(spotifyUserPlaylistCreationService.createPlaylist(accessToken, userId, playlistName, trackIds))
                .thenThrow(new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Spotify API error"));

        // Act & Assert:
        assertThatThrownBy(() -> playlistCreationController.createPlaylist(request, principal))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Spotify API error");
    }

    /**
     * ユーザー名に基づいてプレイリスト名が生成されることを確認する。
     */
    @Test
    void generatePlaylistName() {
        // Arrange: テストデータの準備
        String userName = "testUser";

        // Act: テスト対象メソッドの実行
        String actualPlaylistName = playlistCreationController.generatePlaylistName(userName);

        // Assert: AssertJを使用してアサーション
        assertThat(actualPlaylistName).startsWith(userName + " さんへのおすすめ楽曲 ");
    }
}
