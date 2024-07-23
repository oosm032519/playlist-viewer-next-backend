package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import com.github.oosm032519.playlistviewernext.repository.UserFavoritePlaylistRepository;
import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistFavoriteControllerTest {

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private UserFavoritePlaylistRepository userFavoritePlaylistRepository;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private PlaylistFavoriteController playlistFavoriteController;

    @BeforeEach
    void setUp() {
        when(principal.getAttribute("id")).thenReturn("testUserId");
    }

    @Test
    void favoritePlaylist_Success() {
        // Arrange
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(anyString(), anyString())).thenReturn(false);
        when(userFavoritePlaylistRepository.save(any(UserFavoritePlaylist.class))).thenReturn(new UserFavoritePlaylist());

        // Act
        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.favoritePlaylist(
                principal, "playlistId", "playlistName", 10, "ownerName");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "success");
        assertThat(response.getBody()).containsEntry("message", "プレイリストをお気に入りに登録しました。");

        verify(userFavoritePlaylistRepository).save(any(UserFavoritePlaylist.class));
    }

    @Test
    void favoritePlaylist_AlreadyFavorited() {
        // Arrange
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(anyString(), anyString())).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.favoritePlaylist(
                principal, "playlistId", "playlistName", 10, "ownerName");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "warning");
        assertThat(response.getBody()).containsEntry("message", "このプレイリストは既にお気に入りに登録されています。");

        verify(userFavoritePlaylistRepository, never()).save(any(UserFavoritePlaylist.class));
    }

    @Test
    void favoritePlaylist_Error() {
        // Arrange
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(anyString(), anyString())).thenReturn(false);
        when(userFavoritePlaylistRepository.save(any(UserFavoritePlaylist.class))).thenThrow(new RuntimeException("DB error"));

        // Act
        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.favoritePlaylist(
                principal, "playlistId", "playlistName", 10, "ownerName");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", "error");
        assertThat(response.getBody()).containsEntry("message", "プレイリストのお気に入り登録中にエラーが発生しました。");
    }

    @Test
    void unfavoritePlaylist_Success() {
        // Arrange
        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(anyString(), anyString())).thenReturn(1L);

        // Act
        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.unfavoritePlaylist(principal, "playlistId");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "success");
        assertThat(response.getBody()).containsEntry("message", "プレイリストをお気に入りから解除しました。");

        verify(userFavoritePlaylistRepository).deleteByUserIdAndPlaylistId(anyString(), anyString());
    }

    @Test
    void unfavoritePlaylist_NotFavorited() {
        // Arrange
        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(anyString(), anyString())).thenReturn(0L);

        // Act
        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.unfavoritePlaylist(principal, "playlistId");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "warning");
        assertThat(response.getBody()).containsEntry("message", "このプレイリストはお気に入りに登録されていません。");
    }

    @Test
    void unfavoritePlaylist_Error() {
        // Arrange
        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(anyString(), anyString())).thenThrow(new RuntimeException("DB error"));

        // Act
        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.unfavoritePlaylist(principal, "playlistId");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).containsEntry("status", "error");
        assertThat(response.getBody()).containsEntry("message", "プレイリストのお気に入り解除中にエラーが発生しました。");
    }

    @Test
    void getFavoritePlaylists_Success() {
        // Arrange
        UserFavoritePlaylist playlist1 = new UserFavoritePlaylist();
        playlist1.setPlaylistId("id1");
        playlist1.setPlaylistName("name1");
        playlist1.setTotalTracks(10);
        playlist1.setPlaylistOwnerName("owner1");

        UserFavoritePlaylist playlist2 = new UserFavoritePlaylist();
        playlist2.setPlaylistId("id2");
        playlist2.setPlaylistName("name2");
        playlist2.setTotalTracks(20);
        playlist2.setPlaylistOwnerName("owner2");

        when(userFavoritePlaylistRepository.findByUserId(anyString())).thenReturn(Arrays.asList(playlist1, playlist2));

        // Act
        ResponseEntity<List<Map<String, Object>>> response = playlistFavoriteController.getFavoritePlaylists(principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0)).containsEntry("playlistId", "id1");
        assertThat(response.getBody().get(1)).containsEntry("playlistId", "id2");

        verify(userFavoritePlaylistRepository).findByUserId(anyString());
    }

    @Test
    void getFavoritePlaylists_Error() {
        // Arrange
        when(userFavoritePlaylistRepository.findByUserId(anyString())).thenThrow(new RuntimeException("DB error"));

        // Act
        ResponseEntity<List<Map<String, Object>>> response = playlistFavoriteController.getFavoritePlaylists(principal);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void checkFavorite_Favorited() {
        // Arrange
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(anyString(), anyString())).thenReturn(true);

        // Act
        ResponseEntity<Boolean> response = playlistFavoriteController.checkFavorite(principal, "playlistId");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();

        verify(userFavoritePlaylistRepository).existsByUserIdAndPlaylistId(anyString(), anyString());
    }

    @Test
    void checkFavorite_NotFavorited() {
        // Arrange
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(anyString(), anyString())).thenReturn(false);

        // Act
        ResponseEntity<Boolean> response = playlistFavoriteController.checkFavorite(principal, "playlistId");

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isFalse();

        verify(userFavoritePlaylistRepository).existsByUserIdAndPlaylistId(anyString(), anyString());
    }

    @Test
    void hashUserId_ThrowsRuntimeException() {
        // Arrange
        PlaylistFavoriteController controller = new PlaylistFavoriteController(userAuthenticationService, userFavoritePlaylistRepository);

        // Act & Assert
        assertThatThrownBy(() -> {
            // MessageDigestをモックして例外をスローするように設定
            try (MockedStatic<MessageDigest> mockedMessageDigest = mockStatic(MessageDigest.class)) {
                mockedMessageDigest.when(() -> MessageDigest.getInstance("SHA-256"))
                        .thenThrow(new NoSuchAlgorithmException("Test exception"));

                controller.favoritePlaylist(principal, "playlistId", "playlistName", 10, "ownerName");
            }
        })
                .isInstanceOf(RuntimeException.class)
                .hasMessage("ハッシュアルゴリズムが見つかりません。")
                .hasCauseInstanceOf(NoSuchAlgorithmException.class);
    }

}
