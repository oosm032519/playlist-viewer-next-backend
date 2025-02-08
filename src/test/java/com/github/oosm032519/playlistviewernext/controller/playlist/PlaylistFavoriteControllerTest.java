package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import com.github.oosm032519.playlistviewernext.repository.UserFavoritePlaylistRepository;
import com.github.oosm032519.playlistviewernext.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PlaylistFavoriteControllerTest {

    private static final String MOCK_USER_ID = "mock-user-id";

    @Mock
    private OAuth2User principal;
    @Mock
    private UserFavoritePlaylistRepository userFavoritePlaylistRepository;
    @Mock
    private HashUtil hashUtil;
    @InjectMocks
    private PlaylistFavoriteController playlistFavoriteController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(playlistFavoriteController, "mockEnabled", true);
        ReflectionTestUtils.setField(playlistFavoriteController, "hashUtil", hashUtil);
    }

    /**
     * プレイリストがお気に入りに正常に登録されることを確認する。
     */
    @Test
    void favoritePlaylist_success() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        String playlistId = "testPlaylistId";
        String playlistName = "Test Playlist";
        int totalTracks = 10;
        String playlistOwnerName = "Test Owner";

        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(MOCK_USER_ID, playlistId)).thenReturn(false);

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = playlistFavoriteController.favoritePlaylist(principal, playlistId, playlistName, totalTracks, playlistOwnerName);

        // Assert: 結果の検証
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo("success");
        assertThat(responseBody.get("message")).isEqualTo("プレイリストをお気に入りに登録しました。");

        verify(userFavoritePlaylistRepository, times(1)).save(any(UserFavoritePlaylist.class));
    }

    /**
     * 既に登録済みのプレイリストをお気に入りに登録しようとした場合、警告メッセージが返されることを確認する。
     */
    @Test
    void favoritePlaylist_alreadyFavorited() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        String playlistId = "testPlaylistId";
        String playlistName = "Test Playlist";
        int totalTracks = 10;
        String playlistOwnerName = "Test Owner";

        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(MOCK_USER_ID, playlistId)).thenReturn(true);

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = playlistFavoriteController.favoritePlaylist(principal, playlistId, playlistName, totalTracks, playlistOwnerName);

        // Assert: 結果の検証
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo("warning");
        assertThat(responseBody.get("message")).isEqualTo("このプレイリストは既にお気に入りに登録されています。");

        verify(userFavoritePlaylistRepository, never()).save(any(UserFavoritePlaylist.class));
    }

    /**
     * プレイリストのお気に入り登録解除が正常に行われることを確認する。
     */
    @Test
    void unfavoritePlaylist_success() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        String playlistId = "testPlaylistId";

        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(MOCK_USER_ID, playlistId)).thenReturn(1L);

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = playlistFavoriteController.unfavoritePlaylist(principal, playlistId);

        // Assert: 結果の検証
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo("success");
        assertThat(responseBody.get("message")).isEqualTo("プレイリストをお気に入りから解除しました。");

        verify(userFavoritePlaylistRepository, times(1)).deleteByUserIdAndPlaylistId(MOCK_USER_ID, playlistId);
    }

    /**
     * お気に入りに登録されていないプレイリストを解除しようとした場合、警告メッセージが返されることを確認する。
     */
    @Test
    void unfavoritePlaylist_notFavorited() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        String playlistId = "testPlaylistId";

        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(MOCK_USER_ID, playlistId)).thenReturn(0L);

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> responseEntity = playlistFavoriteController.unfavoritePlaylist(principal, playlistId);

        // Assert: 結果の検証
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Map<String, Object> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody.get("status")).isEqualTo("warning");
        assertThat(responseBody.get("message")).isEqualTo("このプレイリストはお気に入りに登録されていません。");

        verify(userFavoritePlaylistRepository, times(1)).deleteByUserIdAndPlaylistId(MOCK_USER_ID, playlistId);
    }

    /**
     * ユーザーのお気に入りプレイリスト一覧が正常に取得できることを確認する。
     */
    @Test
    void getFavoritePlaylists_success() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        List<UserFavoritePlaylist> favoritePlaylists = Collections.singletonList(createUserFavoritePlaylist());
        when(userFavoritePlaylistRepository.findByUserId(MOCK_USER_ID)).thenReturn(favoritePlaylists);

        // Act: テスト対象メソッドの実行
        ResponseEntity<List<Map<String, Object>>> responseEntity = playlistFavoriteController.getFavoritePlaylists(principal);

        // Assert: 結果の検証
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<Map<String, Object>> responseBody = responseEntity.getBody();
        assertThat(responseBody).isNotNull();
        assertThat(responseBody).hasSize(1);
        assertThat(responseBody.getFirst().get("playlistId")).isEqualTo("testPlaylistId");
        assertThat(responseBody.getFirst().get("playlistName")).isEqualTo("Test Playlist");
    }

    private UserFavoritePlaylist createUserFavoritePlaylist() {
        UserFavoritePlaylist playlist = new UserFavoritePlaylist();
        playlist.setPlaylistId("testPlaylistId");
        playlist.setPlaylistName("Test Playlist");
        playlist.setTotalTracks(10);
        playlist.setAddedAt(LocalDateTime.now());
        playlist.setPlaylistOwnerName("Test Owner");
        playlist.setUserId(MOCK_USER_ID);
        return playlist;
    }

    /**
     * 指定されたプレイリストがお気に入りに登録されているか確認し、登録されていればtrueを返すことを確認する。
     */
    @Test
    void checkFavorite_favorited() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        String playlistId = "testPlaylistId";

        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(MOCK_USER_ID, playlistId)).thenReturn(true);

        // Act: テスト対象メソッドの実行
        ResponseEntity<Boolean> responseEntity = playlistFavoriteController.checkFavorite(principal, playlistId);

        // Assert: 結果の検証
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Boolean responseBody = responseEntity.getBody();
        assertThat(responseBody).isTrue();
    }

    /**
     * 指定されたプレイリストがお気に入りに登録されていない場合、falseを返すことを確認する。
     */
    @Test
    void checkFavorite_notFavorited() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        String playlistId = "testPlaylistId";

        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(MOCK_USER_ID, playlistId)).thenReturn(false);

        // Act: テスト対象メソッドの実行
        ResponseEntity<Boolean> responseEntity = playlistFavoriteController.checkFavorite(principal, playlistId);

        // Assert: 結果の検証
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        Boolean responseBody = responseEntity.getBody();
        assertThat(responseBody).isFalse();
    }

    /**
     * ユーザーIDのハッシュ化中にNoSuchAlgorithmExceptionが発生した場合、例外がスローされることを確認する。
     */
    @Test
    void favoritePlaylist_NoSuchAlgorithmException() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        ReflectionTestUtils.setField(playlistFavoriteController, "mockEnabled", false);
        String playlistId = "testPlaylistId";
        String playlistName = "Test Playlist";
        int totalTracks = 10;
        String playlistOwnerName = "Test Owner";

        when(principal.getAttribute("id")).thenReturn("testUserId");
        when(hashUtil.hashUserId(anyString())).thenThrow(new NoSuchAlgorithmException("ハッシュアルゴリズムが見つかりません"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> playlistFavoriteController.favoritePlaylist(principal, playlistId, playlistName, totalTracks, playlistOwnerName))
                .isInstanceOf(NoSuchAlgorithmException.class);
    }

    /**
     * プレイリストのお気に入り解除時にNoSuchAlgorithmExceptionが発生した場合、例外がスローされることを確認する。
     */
    @Test
    void unfavoritePlaylist_NoSuchAlgorithmException() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        ReflectionTestUtils.setField(playlistFavoriteController, "mockEnabled", false);
        String playlistId = "testPlaylistId";

        when(principal.getAttribute("id")).thenReturn("testUserId");
        when(hashUtil.hashUserId(anyString())).thenThrow(new NoSuchAlgorithmException("ハッシュアルゴリズムが見つかりません"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> playlistFavoriteController.unfavoritePlaylist(principal, playlistId))
                .isInstanceOf(NoSuchAlgorithmException.class);
    }

    /**
     * お気に入りプレイリスト一覧取得時にNoSuchAlgorithmExceptionが発生した場合、例外がスローされることを確認する。
     */
    @Test
    void getFavoritePlaylists_NoSuchAlgorithmException() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        ReflectionTestUtils.setField(playlistFavoriteController, "mockEnabled", false);

        when(principal.getAttribute("id")).thenReturn("testUserId");
        when(hashUtil.hashUserId(anyString())).thenThrow(new NoSuchAlgorithmException("ハッシュアルゴリズムが見つかりません"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> playlistFavoriteController.getFavoritePlaylists(principal))
                .isInstanceOf(NoSuchAlgorithmException.class);
    }

    /**
     * お気に入り確認時にNoSuchAlgorithmExceptionが発生した場合、例外がスローされることを確認する。
     */
    @Test
    void checkFavorite_NoSuchAlgorithmException() throws Exception {
        // Arrange: テストデータの準備とモックの設定
        ReflectionTestUtils.setField(playlistFavoriteController, "mockEnabled", false);
        String playlistId = "testPlaylistId";

        when(principal.getAttribute("id")).thenReturn("testUserId");
        when(hashUtil.hashUserId(anyString())).thenThrow(new NoSuchAlgorithmException("ハッシュアルゴリズムが見つかりません"));

        // Act & Assert: 例外がスローされることの確認
        assertThatThrownBy(() -> playlistFavoriteController.checkFavorite(principal, playlistId))
                .isInstanceOf(NoSuchAlgorithmException.class);
    }
}
