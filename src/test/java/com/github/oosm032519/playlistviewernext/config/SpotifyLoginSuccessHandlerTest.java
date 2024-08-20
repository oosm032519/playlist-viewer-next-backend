package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class SpotifyLoginSuccessHandlerTest {

    private final String frontendUrl = "http://localhost:3000";
    @Mock
    private JwtUtil jwtUtil;
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
    @InjectMocks
    private SpotifyLoginSuccessHandler handler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        handler = new SpotifyLoginSuccessHandler(jwtUtil);
        handler.frontendUrl = frontendUrl;
        handler.spotifyApi = spotifyApi; // SpotifyApiのモックを設定
        handler.redisTemplate = redisTemplate; // RedisTemplateのモックを設定
    }

    @Test
    void testOnAuthenticationSuccess() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        when(oauth2User.getAttribute("id")).thenReturn("user123");
        when(oauth2User.getAttribute("access_token")).thenReturn("accessToken");
        when(jwtUtil.generateToken(any(Map.class))).thenReturn("jwtToken");

        // Spotify APIのモック設定
        User mockUser = mock(User.class);
        when(mockUser.getDisplayName()).thenReturn("SpotifyUserName");

        GetCurrentUsersProfileRequest.Builder mockBuilder = mock(GetCurrentUsersProfileRequest.Builder.class);
        GetCurrentUsersProfileRequest mockRequest = mock(GetCurrentUsersProfileRequest.class);
        when(mockRequest.execute()).thenReturn(mockUser);

        when(mockBuilder.build()).thenReturn(mockRequest);
        when(spotifyApi.getCurrentUsersProfile()).thenReturn(mockBuilder);

        // RedisTemplateのモック設定
        ValueOperations<String, String> valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        // Act
        handler.onAuthenticationSuccess(request, response, authentication);

        // Assert
        verify(spotifyApi).setAccessToken("accessToken");
        verify(jwtUtil).generateToken(any(Map.class));
        verify(valueOperations).set(anyString(), anyString(), eq(5L), eq(TimeUnit.MINUTES));
        verify(response).sendRedirect(startsWith(frontendUrl + "#token="));
    }
}
