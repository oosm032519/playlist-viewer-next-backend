package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;
import com.github.oosm032519.playlistviewernext.service.playlist.UserFavoritePlaylistsService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
public class UserFavoritePlaylistsControllerTest {

    @Mock
    private UserFavoritePlaylistsService userFavoritePlaylistsService;

    @InjectMocks
    private UserFavoritePlaylistsController userFavoritePlaylistsController;

    @Test
    void getFavoritePlaylists_正常系() throws NoSuchAlgorithmException {
        // テストデータの準備
        String userId = "test_user_id";
        String hashedUserId = userFavoritePlaylistsController.hashUserId(userId);
        OAuth2User principal = new TestOAuth2User(Map.of("id", userId));
        List<FavoritePlaylistResponse> expectedPlaylists = Collections.emptyList();

        // モックの設定
        when(userFavoritePlaylistsService.getFavoritePlaylists(hashedUserId)).thenReturn(expectedPlaylists);

        // テスト対象メソッドの実行
        ResponseEntity<?> response = userFavoritePlaylistsController.getFavoritePlaylists(principal);

        // アサーション
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);
    }

    @Test
    void getFavoritePlaylists_異常系_userIdがnullの場合() {
        // テストデータの準備: HashMapを使用してnull値を許可
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", null);
        OAuth2User principal = new TestOAuth2User(attributes);

        // テスト対象メソッドの実行とアサーション
        assertThrows(NullPointerException.class, () -> userFavoritePlaylistsController.getFavoritePlaylists(principal));
    }


    // OAuth2User のモック実装のための内部クラス
    private static class TestOAuth2User implements OAuth2User {
        private final Map<String, Object> attributes;

        public TestOAuth2User(Map<String, Object> attributes) {
            this.attributes = attributes;
        }

        @Override
        public Map<String, Object> getAttributes() {
            return attributes;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }
    }
}
