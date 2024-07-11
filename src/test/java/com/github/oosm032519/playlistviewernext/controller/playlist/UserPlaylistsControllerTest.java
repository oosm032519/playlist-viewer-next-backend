// UserPlaylistsControllerTest.java

package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistsService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * UserPlaylistsControllerTestクラスは、UserPlaylistsControllerのユニットテストを行います。
 */
@ExtendWith(MockitoExtension.class)
class UserPlaylistsControllerTest {

    @Mock
    private SpotifyUserPlaylistsService userPlaylistsService;

    @InjectMocks
    private UserPlaylistsController userPlaylistsController;

    /**
     * 各テストメソッドの前に実行される設定メソッド。
     */
    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    /**
     * getFollowedPlaylistsメソッドが正常にプレイリストを返すことをテストします。
     *
     * @throws IOException            入出力例外
     * @throws ParseException         パース例外
     * @throws SpotifyWebApiException Spotify API例外
     */
    @Test
    void getFollowedPlaylists_ReturnsPlaylistsSuccessfully() throws IOException, ParseException, SpotifyWebApiException {
        // Given: モックされたOAuth2AuthenticationTokenと期待されるプレイリストのリストを設定
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        List<PlaylistSimplified> expectedPlaylists = Arrays.asList(
                new PlaylistSimplified.Builder().setName("Followed Playlist 1").build(),
                new PlaylistSimplified.Builder().setName("Followed Playlist 2").build()
        );

        // モックされたサービスが期待されるプレイリストを返すように設定
        when(userPlaylistsService.getCurrentUsersPlaylists(authToken)).thenReturn(expectedPlaylists);

        // When: コントローラのメソッドを呼び出す
        ResponseEntity<?> response = userPlaylistsController.getFollowedPlaylists(authToken);

        // Then: ステータスコードとボディが期待通りであることを検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);
        verify(userPlaylistsService).getCurrentUsersPlaylists(authToken);
    }

    /**
     * getFollowedPlaylistsメソッドが例外を適切に処理することをテストします。
     *
     * @throws IOException            入出力例外
     * @throws ParseException         パース例外
     * @throws SpotifyWebApiException Spotify API例外
     */
    @Test
    void getFollowedPlaylists_HandlesExceptionGracefully() throws IOException, ParseException, SpotifyWebApiException {
        // Given: モックされたOAuth2AuthenticationTokenと例外をスローするサービスを設定
        OAuth2AuthenticationToken authToken = mock(OAuth2AuthenticationToken.class);
        when(userPlaylistsService.getCurrentUsersPlaylists(authToken)).thenThrow(new RuntimeException("Authentication error"));

        // When: コントローラのメソッドを呼び出す
        ResponseEntity<?> response = userPlaylistsController.getFollowedPlaylists(authToken);

        // Then: ステータスコードとボディが期待通りであることを検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isEqualTo("Error: Authentication error");
        verify(userPlaylistsService).getCurrentUsersPlaylists(authToken);
    }
}
