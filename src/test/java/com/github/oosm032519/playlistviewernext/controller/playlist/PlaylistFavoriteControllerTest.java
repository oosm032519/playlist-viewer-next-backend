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

import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    void favoritePlaylist_success() throws Exception {
        // Arrange
        String playlistId = "testPlaylistId";
        String playlistName = "Test Playlist";
        int totalTracks = 10;
        String playlistOwnerName = "Test Owner";
        String hashedUserId = "hashedMockUserId";

        when(hashUtil.hashUserId(MOCK_USER_ID)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(false);

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = playlistFavoriteController.favoritePlaylist(principal, playlistId, playlistName, totalTracks, playlistOwnerName);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.get("status"));
        assertEquals("プレイリストをお気に入りに登録しました。", responseBody.get("message"));

        verify(userFavoritePlaylistRepository, times(1)).save(any(UserFavoritePlaylist.class));
    }

    @Test
    void favoritePlaylist_alreadyFavorited() throws Exception {
        // Arrange
        String playlistId = "testPlaylistId";
        String playlistName = "Test Playlist";
        int totalTracks = 10;
        String playlistOwnerName = "Test Owner";
        String hashedUserId = "hashedMockUserId";

        when(hashUtil.hashUserId(MOCK_USER_ID)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(true);

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = playlistFavoriteController.favoritePlaylist(principal, playlistId, playlistName, totalTracks, playlistOwnerName);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("warning", responseBody.get("status"));
        assertEquals("このプレイリストは既にお気に入りに登録されています。", responseBody.get("message"));

        verify(userFavoritePlaylistRepository, never()).save(any(UserFavoritePlaylist.class));
    }

    @Test
    void unfavoritePlaylist_success() throws Exception {
        // Arrange
        String playlistId = "testPlaylistId";
        String hashedUserId = "hashedMockUserId";

        when(hashUtil.hashUserId(MOCK_USER_ID)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(1L);

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = playlistFavoriteController.unfavoritePlaylist(principal, playlistId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("success", responseBody.get("status"));
        assertEquals("プレイリストをお気に入りから解除しました。", responseBody.get("message"));

        verify(userFavoritePlaylistRepository, times(1)).deleteByUserIdAndPlaylistId(hashedUserId, playlistId);
    }

    @Test
    void unfavoritePlaylist_notFavorited() throws Exception {
        // Arrange
        String playlistId = "testPlaylistId";
        String hashedUserId = "hashedMockUserId";

        when(hashUtil.hashUserId(MOCK_USER_ID)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(0L);

        // Act
        ResponseEntity<Map<String, Object>> responseEntity = playlistFavoriteController.unfavoritePlaylist(principal, playlistId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Map<String, Object> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals("warning", responseBody.get("status"));
        assertEquals("このプレイリストはお気に入りに登録されていません。", responseBody.get("message"));

        verify(userFavoritePlaylistRepository, times(1)).deleteByUserIdAndPlaylistId(hashedUserId, playlistId);
    }

    @Test
    void getFavoritePlaylists_success() throws Exception {
        // Arrange
        String hashedUserId = "hashedMockUserId";
        when(hashUtil.hashUserId(MOCK_USER_ID)).thenReturn(hashedUserId);

        List<UserFavoritePlaylist> favoritePlaylists = Collections.singletonList(createUserFavoritePlaylist());
        when(userFavoritePlaylistRepository.findByUserId(hashedUserId)).thenReturn(favoritePlaylists);

        // Act
        ResponseEntity<List<Map<String, Object>>> responseEntity = playlistFavoriteController.getFavoritePlaylists(principal);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        List<Map<String, Object>> responseBody = responseEntity.getBody();
        assertNotNull(responseBody);
        assertEquals(1, responseBody.size());
        assertEquals("testPlaylistId", responseBody.get(0).get("playlistId"));
        assertEquals("Test Playlist", responseBody.get(0).get("playlistName"));
    }

    private UserFavoritePlaylist createUserFavoritePlaylist() {
        UserFavoritePlaylist playlist = new UserFavoritePlaylist();
        playlist.setPlaylistId("testPlaylistId");
        playlist.setPlaylistName("Test Playlist");
        playlist.setTotalTracks(10);
        playlist.setAddedAt(LocalDateTime.now());
        playlist.setPlaylistOwnerName("Test Owner");
        playlist.setUserId("hashedMockUserId");
        return playlist;
    }

    @Test
    void checkFavorite_favorited() throws Exception {
        // Arrange
        String playlistId = "testPlaylistId";
        String hashedUserId = "hashedMockUserId";

        when(hashUtil.hashUserId(MOCK_USER_ID)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(true);

        // Act
        ResponseEntity<Boolean> responseEntity = playlistFavoriteController.checkFavorite(principal, playlistId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Boolean responseBody = responseEntity.getBody();
        assertTrue(responseBody);
    }

    @Test
    void checkFavorite_notFavorited() throws Exception {
        // Arrange
        String playlistId = "testPlaylistId";
        String hashedUserId = "hashedMockUserId";

        when(hashUtil.hashUserId(MOCK_USER_ID)).thenReturn(hashedUserId);
        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(hashedUserId, playlistId)).thenReturn(false);

        // Act
        ResponseEntity<Boolean> responseEntity = playlistFavoriteController.checkFavorite(principal, playlistId);

        // Assert
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        Boolean responseBody = responseEntity.getBody();
        assertFalse(responseBody);
    }

    @Test
    void favoritePlaylist_NoSuchAlgorithmException() throws Exception {
        // Arrange
        String playlistId = "testPlaylistId";
        String playlistName = "Test Playlist";
        int totalTracks = 10;
        String playlistOwnerName = "Test Owner";

        when(hashUtil.hashUserId(anyString())).thenThrow(new NoSuchAlgorithmException("ハッシュアルゴリズムが見つかりません"));

        // Act & Assert
        assertThrows(NoSuchAlgorithmException.class, () -> {
            playlistFavoriteController.favoritePlaylist(principal, playlistId, playlistName, totalTracks, playlistOwnerName);
        });
    }

    @Test
    void unfavoritePlaylist_NoSuchAlgorithmException() throws Exception {
        // Arrange
        String playlistId = "testPlaylistId";

        when(hashUtil.hashUserId(anyString())).thenThrow(new NoSuchAlgorithmException("ハッシュアルゴリズムが見つかりません"));

        // Act & Assert
        assertThrows(NoSuchAlgorithmException.class, () -> {
            playlistFavoriteController.unfavoritePlaylist(principal, playlistId);
        });
    }

    @Test
    void getFavoritePlaylists_NoSuchAlgorithmException() throws Exception {
        // Arrange
        when(hashUtil.hashUserId(anyString())).thenThrow(new NoSuchAlgorithmException("ハッシュアルゴリズムが見つかりません"));

        // Act & Assert
        assertThrows(NoSuchAlgorithmException.class, () -> {
            playlistFavoriteController.getFavoritePlaylists(principal);
        });
    }

    @Test
    void checkFavorite_NoSuchAlgorithmException() throws Exception {
        // Arrange
        String playlistId = "testPlaylistId";

        when(hashUtil.hashUserId(anyString())).thenThrow(new NoSuchAlgorithmException("ハッシュアルゴリズムが見つかりません"));

        // Act & Assert
        assertThrows(NoSuchAlgorithmException.class, () -> {
            playlistFavoriteController.checkFavorite(principal, playlistId);
        });
    }
}
