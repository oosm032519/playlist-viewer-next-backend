package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.UserFavoritePlaylistsService;
import com.github.oosm032519.playlistviewernext.util.HashUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserFavoritePlaylistsControllerTest {

    @Mock
    private UserFavoritePlaylistsService userFavoritePlaylistsService;

    @Mock
    private HashUtil hashUtil;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private UserFavoritePlaylistsController userFavoritePlaylistsController;

    @BeforeEach
    public void setup() {
        userFavoritePlaylistsController = new UserFavoritePlaylistsController(userFavoritePlaylistsService, hashUtil);
    }

    @Test
    public void testGetFavoritePlaylists_Success() throws NoSuchAlgorithmException {
        // テストデータの準備
        String userId = "testUserId";
        String hashedUserId = "hashedTestUserId";
        List<FavoritePlaylistResponse> expectedPlaylists = List.of(
                new FavoritePlaylistResponse("playlistId1", "playlistName1", "ownerName1", 10, (LocalDateTime) null),
                new FavoritePlaylistResponse("playlistId2", "playlistName2", "ownerName2", 20, (LocalDateTime) null)
        );

        // モックの設定
        when(principal.getAttribute("id")).thenReturn(userId);
        when(hashUtil.hashUserId(Objects.requireNonNull(userId))).thenReturn(hashedUserId);
        when(userFavoritePlaylistsService.getFavoritePlaylists(hashedUserId)).thenReturn(expectedPlaylists);

        // テスト対象メソッドの実行
        ResponseEntity<?> responseEntity = userFavoritePlaylistsController.getFavoritePlaylists(principal);

        // アサーション
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(responseEntity.getBody()).isEqualTo(expectedPlaylists);
    }


    @Test
    public void testGetFavoritePlaylists_NoPlaylists() throws NoSuchAlgorithmException {
        // テストデータの準備
        String userId = "testUserId";
        String hashedUserId = "hashedTestUserId";
        List<FavoritePlaylistResponse> expectedPlaylists = List.of(); // 空のリスト

        // モックの設定
        when(principal.getAttribute("id")).thenReturn(userId);
        when(hashUtil.hashUserId(Objects.requireNonNull(userId))).thenReturn(hashedUserId);
        when(userFavoritePlaylistsService.getFavoritePlaylists(hashedUserId)).thenReturn(expectedPlaylists);

        // テスト対象メソッドの実行
        ResponseEntity<?> responseEntity = userFavoritePlaylistsController.getFavoritePlaylists(principal);

        // アサーション
        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(responseEntity.getBody()).isEqualTo(expectedPlaylists); // 空のリストが返されることを確認
    }
}
