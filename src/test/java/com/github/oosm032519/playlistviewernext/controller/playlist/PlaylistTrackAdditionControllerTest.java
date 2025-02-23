package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.model.PlaylistTrackAdditionRequest;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackAdditionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlaylistTrackAdditionControllerTest {

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private SpotifyPlaylistTrackAdditionService spotifyService;

    @InjectMocks
    private PlaylistTrackAdditionController controller;

    /**
     * トラックがプレイリストに正常に追加された場合、成功メッセージとスナップショットIDを含むレスポンスが返されることを確認する。
     */
    @Test
    void addTrackToPlaylist_success() throws SpotifyWebApiException {
        // Arrange: テストデータの準備
        String playlistId = "playlist123";
        String trackId = "track456";
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId(playlistId);
        request.setTrackId(trackId);

        String accessToken = "accessToken789";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("spotify_access_token", accessToken);
        attributes.put("name", "testuser"); // name 属性を追加
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "name");
        Authentication authentication = new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), "spotify");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        se.michaelthelin.spotify.model_objects.special.SnapshotResult mockSnapshotResult =
                new se.michaelthelin.spotify.model_objects.special.SnapshotResult.Builder().setSnapshotId("snapshotId123").build();

        // モックの設定
        when(userAuthenticationService.getAccessToken(any(OAuth2User.class))).thenReturn(accessToken);
        when(spotifyService.addTrackToPlaylist(accessToken, playlistId, trackId)).thenReturn(mockSnapshotResult);

        // Act: コントローラーの呼び出し
        controller = new PlaylistTrackAdditionController(userAuthenticationService, spotifyService);
        ResponseEntity<Map<String, String>> response = controller.addTrackToPlaylist(request, oAuth2User);

        // Assert: レスポンスの検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Objects.requireNonNull(response.getBody()).get("message")).isEqualTo("トラックが正常に追加されました。");
        assertThat(response.getBody().get("snapshot_id")).isEqualTo("snapshotId123");
    }

    /**
     * 認証に失敗した場合（アクセストークンがない場合）、AuthenticationExceptionがスローされることを確認する。
     */
    @Test
    void addTrackToPlaylist_authenticationFailure() {
        // Arrange: テストデータの準備
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId("testPlaylistId");
        request.setTrackId("testTrackId");

        // Act & Assert: コントローラーの呼び出し、OAuth2Userをnullにすることで認証エラーを発生させる
        controller = new PlaylistTrackAdditionController(userAuthenticationService, spotifyService);
        OAuth2User oAuth2User = null;

        // JUnitのassertThrowsからAssertJのassertThatThrownByに変更
        assertThatThrownBy(() -> controller.addTrackToPlaylist(request, oAuth2User))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("ユーザーが認証されていないか、アクセストークンが見つかりません。");
    }

    /**
     * SpotifyWebApiExceptionが発生した場合、InternalServerExceptionがスローされることを確認する。
     */
    @Test
    void addTrackToPlaylist_spotifyWebApiException() throws Exception {
        // Arrange: テストデータの準備
        String playlistId = "playlist123";
        String trackId = "track456";
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId(playlistId);
        request.setTrackId(trackId);

        String accessToken = "accessToken789";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("spotify_access_token", accessToken);
        attributes.put("name", "testuser"); // name 属性を追加
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "name");
        Authentication authentication = new OAuth2AuthenticationToken(oAuth2User, oAuth2User.getAuthorities(), "spotify");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // モックの設定
        when(userAuthenticationService.getAccessToken(any(OAuth2User.class))).thenReturn(accessToken);
        when(spotifyService.addTrackToPlaylist(accessToken, playlistId, trackId))
                .thenThrow(new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Spotify API error"));

        // Act & Assert: コントローラーの呼び出し
        controller = new PlaylistTrackAdditionController(userAuthenticationService, spotifyService);

        // JUnitのassertThrowsからAssertJのassertThatThrownByに変更
        assertThatThrownBy(() -> controller.addTrackToPlaylist(request, oAuth2User))
                .isInstanceOf(InternalServerException.class)
                .hasMessageContaining("Spotify API error");
    }
}
