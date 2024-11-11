package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import com.github.oosm032519.playlistviewernext.repository.UserFavoritePlaylistRepository;
import com.github.oosm032519.playlistviewernext.util.HashUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.NoSuchAlgorithmException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class PlaylistFavoriteControllerTest {

    @MockBean
    private HashUtil hashUtil;

    @MockBean
    private UserFavoritePlaylistRepository userFavoritePlaylistRepository;

    @Autowired
    private PlaylistFavoriteController playlistFavoriteController;

    @Mock
    private OAuth2User principal;

    @Test
    void favoritePlaylist_shouldReturnSuccess_whenPlaylistIsNotFavorited() throws NoSuchAlgorithmException {
        // given: プレイリストがまだお気に入りに登録されていない状態
        String playlistId = "testPlaylistId";
        String playlistName = "testPlaylistName";
        int totalTracks = 10;
        String playlistOwnerName = "testOwnerName";
        String userId = "testUserId";
        String hashedUserId = "hashedTestUserId";

        when(principal.getAttribute("id")).thenReturn(userId);
        when(hashUtil.hashUserId(userId)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(false);
        when(userFavoritePlaylistRepository.save(any())).thenReturn(new UserFavoritePlaylist());

        // when: プレイリストをお気に入りに登録する
        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.favoritePlaylist(
                principal, playlistId, playlistName, totalTracks, playlistOwnerName
        );

        // then: 正常に登録され、成功レスポンスが返される
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "success");
        assertThat(response.getBody()).containsEntry("message", "プレイリストをお気に入りに登録しました。");
    }

    @Test
    void favoritePlaylist_shouldReturnWarning_whenPlaylistIsAlreadyFavorited() throws NoSuchAlgorithmException {
        // given: プレイリストが既にお気に入りに登録されている状態
        String playlistId = "testPlaylistId";
        String playlistName = "testPlaylistName";
        int totalTracks = 10;
        String playlistOwnerName = "testOwnerName";
        String userId = "testUserId";
        String hashedUserId = "hashedTestUserId";

        when(principal.getAttribute("id")).thenReturn(userId);
        when(hashUtil.hashUserId(userId)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(true);

        // when: プレイリストをお気に入りに登録する
        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.favoritePlaylist(
                principal, playlistId, playlistName, totalTracks, playlistOwnerName
        );

        // then: 既に登録済みのため、警告レスポンスが返される
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "warning");
        assertThat(response.getBody()).containsEntry("message", "このプレイリストは既にお気に入りに登録されています。");
    }

    @Test
    void unfavoritePlaylist_shouldReturnSuccess_whenPlaylistIsFavorited() throws NoSuchAlgorithmException {
        // given: プレイリストがお気に入りに登録されている状態
        String playlistId = "testPlaylistId";
        String userId = "testUserId";
        String hashedUserId = "hashedTestUserId";

        when(principal.getAttribute("id")).thenReturn(userId);
        when(hashUtil.hashUserId(userId)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(1L);

        // when: プレイリストのお気に入りを解除する
        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.unfavoritePlaylist(principal, playlistId);

        // then: 正常に解除され、成功レスポンスが返される
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "success");
        assertThat(response.getBody()).containsEntry("message", "プレイリストをお気に入りから解除しました。");
    }

    @Test
    void unfavoritePlaylist_shouldReturnWarning_whenPlaylistIsNotFavorited() throws NoSuchAlgorithmException {
        // given: プレイリストがお気に入りに登録されていない状態
        String playlistId = "testPlaylistId";
        String userId = "testUserId";
        String hashedUserId = "hashedTestUserId";

        when(principal.getAttribute("id")).thenReturn(userId);
        when(hashUtil.hashUserId(userId)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(0L);

        // when: プレイリストのお気に入りを解除する
        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.unfavoritePlaylist(principal, playlistId);

        // then: 登録されていないため、警告レスポンスが返される
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsEntry("status", "warning");
        assertThat(response.getBody()).containsEntry("message", "このプレイリストはお気に入りに登録されていません。");
    }

    @Test
    void checkFavorite_shouldReturnTrue_whenPlaylistIsFavorited() throws NoSuchAlgorithmException {
        // given: プレイリストがお気に入りに登録されている状態
        String playlistId = "testPlaylistId";
        String userId = "testUserId";
        String hashedUserId = "hashedTestUserId";

        when(principal.getAttribute("id")).thenReturn(userId);
        when(hashUtil.hashUserId(userId)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(true);

        // when: プレイリストがお気に入りに登録されているか確認する
        ResponseEntity<Boolean> response = playlistFavoriteController.checkFavorite(principal, playlistId);

        // then: trueが返される
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isTrue();
    }

    @Test
    void checkFavorite_shouldReturnFalse_whenPlaylistIsNotFavorited() throws NoSuchAlgorithmException {
        // given: プレイリストがお気に入りに登録されていない状態
        String playlistId = "testPlaylistId";
        String userId = "testUserId";
        String hashedUserId = "hashedTestUserId";

        when(principal.getAttribute("id")).thenReturn(userId);
        when(hashUtil.hashUserId(userId)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(false);

        // when: プレイリストがお気に入りに登録されているか確認する
        ResponseEntity<Boolean> response = playlistFavoriteController.checkFavorite(principal, playlistId);

        // then: falseが返される
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isFalse();
    }
}
