package com.github.oosm032519.playlistviewernext.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // MockitoExtension を追加
class SpotifyLoginSuccessHandlerTest {

    private final String frontendUrl = "http://localhost:3000";
    @Mock
    private RedisTemplate<String, String> redisTemplate;
    @Mock
    private SpotifyApi spotifyApi;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;
    @Mock
    private Authentication authentication;
    @Mock
    private OAuth2User oauth2User;

    private SpotifyLoginSuccessHandler handler;

    /**
     * 認証成功時に、ユーザー情報がRedisに保存され、フロントエンドにリダイレクトされることを確認する。
     */
    @Test
    void testOnAuthenticationSuccess() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange: テストデータの準備とモックの設定
        // handler の初期化をここで行う
        handler = new SpotifyLoginSuccessHandler(frontendUrl, false);
        handler.spotifyApi = spotifyApi;
        handler.redisTemplate = redisTemplate;

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("id")).thenReturn("user123");
        when(oauth2User.getAttribute("access_token")).thenReturn("accessToken");

        User mockUser = mock(User.class);
        when(mockUser.getDisplayName()).thenReturn("SpotifyUserName");

        GetCurrentUsersProfileRequest.Builder mockBuilder = mock(GetCurrentUsersProfileRequest.Builder.class);
        GetCurrentUsersProfileRequest mockRequest = mock(GetCurrentUsersProfileRequest.class);
        when(mockRequest.execute()).thenReturn(mockUser);

        when(mockBuilder.build()).thenReturn(mockRequest);
        when(spotifyApi.getCurrentUsersProfile()).thenReturn(mockBuilder);

        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act: テスト対象メソッドの実行
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert: 期待される結果の検証
        verify(spotifyApi).setAccessToken("accessToken");
        verify(hashOperations).putAll(anyString(), any(Map.class));
        verify(valueOperations).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(response).sendRedirect(startsWith(frontendUrl + "#token="));
    }

    /**
     * SpotifyWebApiExceptionが発生した場合に、ユーザー情報がRedisに保存され、フロントエンドにリダイレクトされることを確認する。
     */
    @Test
    void testOnAuthenticationSuccessWithSpotifyWebApiException() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange: テストデータの準備とモックの設定
        // handler の初期化をここで行う
        handler = new SpotifyLoginSuccessHandler(frontendUrl, false);
        handler.spotifyApi = spotifyApi;
        handler.redisTemplate = redisTemplate;

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("id")).thenReturn("user123");
        when(oauth2User.getAttribute("access_token")).thenReturn("accessToken");

        GetCurrentUsersProfileRequest.Builder mockBuilder = mock(GetCurrentUsersProfileRequest.Builder.class);
        GetCurrentUsersProfileRequest mockRequest = mock(GetCurrentUsersProfileRequest.class);
        when(mockRequest.execute()).thenThrow(new SpotifyWebApiException("Spotify API error"));

        when(mockBuilder.build()).thenReturn(mockRequest);
        when(spotifyApi.getCurrentUsersProfile()).thenReturn(mockBuilder);

        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act: テスト対象メソッドの実行
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert: 期待される結果の検証
        verify(spotifyApi).setAccessToken("accessToken");
        verify(hashOperations).putAll(anyString(), any(Map.class));
        verify(valueOperations).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(response).sendRedirect(startsWith(frontendUrl + "#token="));
    }

    /**
     * IOExceptionが発生した場合に、ユーザー情報がRedisに保存され、フロントエンドにリダイレクトされることを確認する。
     */
    @Test
    void testOnAuthenticationSuccessWithIOException() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange: テストデータの準備とモックの設定
        // handler の初期化をここで行う
        handler = new SpotifyLoginSuccessHandler(frontendUrl, false);
        handler.spotifyApi = spotifyApi;
        handler.redisTemplate = redisTemplate;

        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("id")).thenReturn("user123");
        when(oauth2User.getAttribute("access_token")).thenReturn("accessToken");

        GetCurrentUsersProfileRequest.Builder mockBuilder = mock(GetCurrentUsersProfileRequest.Builder.class);
        GetCurrentUsersProfileRequest mockRequest = mock(GetCurrentUsersProfileRequest.class);
        when(mockRequest.execute()).thenThrow(new IOException("IO error"));

        when(mockBuilder.build()).thenReturn(mockRequest);
        when(spotifyApi.getCurrentUsersProfile()).thenReturn(mockBuilder);

        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act: テスト対象メソッドの実行
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert: 期待される結果の検証
        verify(spotifyApi).setAccessToken("accessToken");
        verify(hashOperations).putAll(anyString(), any(Map.class));
        verify(valueOperations).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(response).sendRedirect(startsWith(frontendUrl + "#token="));
    }

    /**
     * モックモードが有効な場合に、モックユーザー情報がRedisに保存され、フロントエンドにリダイレクトされることを確認する。
     */
    @Test
    void testHandleMockAuthenticationSuccess() throws IOException {
        // Arrange: テストデータの準備とモックの設定
        // handler の初期化をここで行う
        handler = new SpotifyLoginSuccessHandler(frontendUrl, true); // モックモードを有効に設定
        handler.redisTemplate = redisTemplate;

        HashOperations<String, Object, Object> hashOperations = mock(HashOperations.class);
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act: テスト対象メソッドの実行
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert: 期待される結果の検証
        verify(hashOperations).putAll(anyString(), any(Map.class));
        verify(valueOperations).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(response).sendRedirect(startsWith(frontendUrl + "#token="));
    }
}
