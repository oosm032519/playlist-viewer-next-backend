package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.entity.UserFavoritePlaylist;
import com.github.oosm032519.playlistviewernext.repository.UserFavoritePlaylistRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PlaylistFavoriteControllerTest {

    @InjectMocks
    private PlaylistFavoriteController playlistFavoriteController;

    @Mock
    private UserFavoritePlaylistRepository userFavoritePlaylistRepository;

    private MockHttpSession mockSession;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockSession = new MockHttpSession();
        mockSession.setAttribute("userId", "testUserId");
    }

    @Test
    void favoritePlaylist_Success() {
        String playlistId = "testPlaylistId";
        String playlistName = "Test Playlist";
        int totalTracks = 10;
        String playlistOwnerName = "Test Owner";

        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(anyString(), eq(playlistId))).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.favoritePlaylist(
                mockSession, playlistId, playlistName, totalTracks, playlistOwnerName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "success");
        assertThat(response.getBody()).containsEntry("message", "プレイリストをお気に入りに登録しました。");

        verify(userFavoritePlaylistRepository).save(any(UserFavoritePlaylist.class));
    }

    @Test
    void favoritePlaylist_AlreadyFavorited() {
        String playlistId = "testPlaylistId";
        String playlistName = "Test Playlist";
        int totalTracks = 10;
        String playlistOwnerName = "Test Owner";

        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(anyString(), eq(playlistId))).thenReturn(true);

        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.favoritePlaylist(
                mockSession, playlistId, playlistName, totalTracks, playlistOwnerName);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "warning");
        assertThat(response.getBody()).containsEntry("message", "このプレイリストは既にお気に入りに登録されています。");

        verify(userFavoritePlaylistRepository, never()).save(any(UserFavoritePlaylist.class));
    }

    @Test
    void favoritePlaylist_Unauthorized() {
        MockHttpSession unauthorizedSession = new MockHttpSession();

        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.favoritePlaylist(
                unauthorizedSession, "playlistId", "playlistName", 10, "ownerName");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("status", "error");
        assertThat(response.getBody()).containsEntry("message", "認証が必要です。");
    }

    @Test
    void unfavoritePlaylist_Success() {
        String playlistId = "testPlaylistId";

        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(anyString(), eq(playlistId))).thenReturn(1L);

        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.unfavoritePlaylist(mockSession, playlistId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "success");
        assertThat(response.getBody()).containsEntry("message", "プレイリストをお気に入りから解除しました。");

        verify(userFavoritePlaylistRepository).deleteByUserIdAndPlaylistId(anyString(), eq(playlistId));
    }

    @Test
    void unfavoritePlaylist_NotFavorited() {
        String playlistId = "testPlaylistId";

        when(userFavoritePlaylistRepository.deleteByUserIdAndPlaylistId(anyString(), eq(playlistId))).thenReturn(0L);

        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.unfavoritePlaylist(mockSession, playlistId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("status", "warning");
        assertThat(response.getBody()).containsEntry("message", "このプレイリストはお気に入りに登録されていません。");

        verify(userFavoritePlaylistRepository).deleteByUserIdAndPlaylistId(anyString(), eq(playlistId));
    }

    @Test
    void unfavoritePlaylist_Unauthorized() {
        MockHttpSession unauthorizedSession = new MockHttpSession();

        ResponseEntity<Map<String, Object>> response = playlistFavoriteController.unfavoritePlaylist(unauthorizedSession, "playlistId");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).containsEntry("status", "error");
        assertThat(response.getBody()).containsEntry("message", "認証が必要です。");
    }

    @Test
    void getFavoritePlaylists_Success() {
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

        List<UserFavoritePlaylist> favoritePlaylists = Arrays.asList(playlist1, playlist2);

        when(userFavoritePlaylistRepository.findByUserId(anyString())).thenReturn(favoritePlaylists);

        ResponseEntity<List<Map<String, Object>>> response = playlistFavoriteController.getFavoritePlaylists(mockSession);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0)).containsEntry("playlistId", "id1");
        assertThat(response.getBody().get(1)).containsEntry("playlistId", "id2");

        verify(userFavoritePlaylistRepository).findByUserId(anyString());
    }

    @Test
    void getFavoritePlaylists_Unauthorized() {
        MockHttpSession unauthorizedSession = new MockHttpSession();

        ResponseEntity<List<Map<String, Object>>> response = playlistFavoriteController.getFavoritePlaylists(unauthorizedSession);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void checkFavorite_IsFavorited() {
        String playlistId = "testPlaylistId";

        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(anyString(), eq(playlistId))).thenReturn(true);

        ResponseEntity<Boolean> response = playlistFavoriteController.checkFavorite(mockSession, playlistId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isTrue();

        verify(userFavoritePlaylistRepository).existsByUserIdAndPlaylistId(anyString(), eq(playlistId));
    }

    @Test
    void checkFavorite_NotFavorited() {
        String playlistId = "testPlaylistId";

        when(userFavoritePlaylistRepository.existsByUserIdAndPlaylistId(anyString(), eq(playlistId))).thenReturn(false);

        ResponseEntity<Boolean> response = playlistFavoriteController.checkFavorite(mockSession, playlistId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isFalse();

        verify(userFavoritePlaylistRepository).existsByUserIdAndPlaylistId(anyString(), eq(playlistId));
    }

    @Test
    void checkFavorite_Unauthorized() {
        MockHttpSession unauthorizedSession = new MockHttpSession();

        ResponseEntity<Boolean> response = playlistFavoriteController.checkFavorite(unauthorizedSession, "playlistId");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isFalse();
    }
}
