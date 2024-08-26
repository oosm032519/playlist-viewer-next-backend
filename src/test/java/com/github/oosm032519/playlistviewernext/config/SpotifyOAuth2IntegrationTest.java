package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * SpotifyOAuth2 ログイン成功時のハンドラーの統合テスト
 * このテストでは、SpotifyLoginSuccessHandler が正しく動作し、
 * 認証成功時に適切なリダイレクトとセッション情報の設定が行われることを検証します。
 */
@SpringBootTest
public class SpotifyOAuth2IntegrationTest {

    @Autowired
    private SpotifyLoginSuccessHandler spotifyLoginSuccessHandler;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private SpotifyApi spotifyApi;

    @MockBean
    private RedisTemplate<String, String> redisTemplate;

    @MockBean
    private ValueOperations<String, String> valueOperations;

    @MockBean
    private GetCurrentUsersProfileRequest.Builder getCurrentUsersProfileRequestBuilder;

    @MockBean
    private GetCurrentUsersProfileRequest getCurrentUsersProfileRequest;

    @MockBean
    private User user;

    @BeforeEach
    void setUp() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        // RedisTemplate のモック設定
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        // 1回目の呼び出し: 一時トークンとセッションIDを関連付ける
        doNothing().when(valueOperations).set(startsWith("temp:"), anyString(), anyLong(), any(TimeUnit.class));
        // 2回目の呼び出し: セッションIDとJWTトークンを関連付ける
        doNothing().when(valueOperations).set(startsWith("session:"), anyString()); // 有効期限と TimeUnit を指定しない

        // Spotify API のモック設定
        when(spotifyApi.getCurrentUsersProfile()).thenReturn(getCurrentUsersProfileRequestBuilder);
        when(getCurrentUsersProfileRequestBuilder.build()).thenReturn(getCurrentUsersProfileRequest);
        when(getCurrentUsersProfileRequest.execute()).thenReturn(user);
        when(user.getDisplayName()).thenReturn("Test User");

        // SpotifyLoginSuccessHandler の frontendUrl を設定
        spotifyLoginSuccessHandler.frontendUrl = "http://localhost:3000";
    }

    /**
     * 認証成功時のリダイレクトとセッション情報の設定をテストします。
     *
     * @throws Exception テスト中に例外が発生した場合
     */
    @Test
    void testOnAuthenticationSuccess() throws Exception {
        // Arrange
        // OAuth2User の属性を設定
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", "user123");
        attributes.put("access_token", "accessToken");
        OAuth2User oauth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "id");

        // 認証オブジェクトを作成
        Authentication authentication = new OAuth2AuthenticationToken(oauth2User, oauth2User.getAuthorities(), "spotify");

        // Mock リクエストとレスポンスを作成
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // JWT トークンの生成をモック
        when(jwtUtil.generateToken(any(Map.class))).thenReturn("jwtToken");

        // Act
        // 認証成功ハンドラーを実行
        spotifyLoginSuccessHandler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        // リダイレクト URL を検証
        assertThat(response.getRedirectedUrl()).startsWith("http://localhost:3000#token=");

        // Redis へのセッション情報の設定を検証
        verify(valueOperations).set(startsWith("temp:"), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(valueOperations).set(startsWith("session:"), anyString()); // 有効期限と TimeUnit を指定しない
    }
}
