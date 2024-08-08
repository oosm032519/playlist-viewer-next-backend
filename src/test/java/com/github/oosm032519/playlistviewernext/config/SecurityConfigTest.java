package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.filter.JwtAuthenticationFilter;
import com.github.oosm032519.playlistviewernext.service.auth.SpotifyOAuth2UserService;
import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfigurationSource;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.users_profile.GetCurrentUsersProfileRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SecurityConfigTest {

    private SecurityConfig securityConfig;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private SpotifyOAuth2UserService spotifyOAuth2UserService;

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @Mock
    private SpotifyApi spotifyApi;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityConfig = new SecurityConfig();
        ReflectionTestUtils.setField(securityConfig, "jwtUtil", jwtUtil);
        ReflectionTestUtils.setField(securityConfig, "spotifyOAuth2UserService", spotifyOAuth2UserService);
        ReflectionTestUtils.setField(securityConfig, "authorizedClientService", authorizedClientService);
        ReflectionTestUtils.setField(securityConfig, "spotifyApi", spotifyApi);
        ReflectionTestUtils.setField(securityConfig, "frontendUrl", "http://localhost:3000");
    }

    @Test
    void jwtAuthenticationFilter_ShouldReturnJwtAuthenticationFilter() {
        JwtAuthenticationFilter result = securityConfig.jwtAuthenticationFilter();
        assertThat(result).isNotNull();
    }

    @Test
    void corsConfigurationSource_ShouldReturnCorsConfigurationSource() {
        CorsConfigurationSource result = securityConfig.corsConfigurationSource();
        assertThat(result).isNotNull();
    }

    @Test
    void spotifyOAuth2UserService_ShouldReturnSpotifyOAuth2UserService() {
        SpotifyOAuth2UserService result = securityConfig.spotifyOAuth2UserService();
        assertThat(result).isNotNull();
    }

    @Test
    void getSpotifyUserName_ShouldReturnUserName() throws Exception {
        String userId = "testUser";
        String accessToken = "testAccessToken";

        // Mocking Spotify API call
        when(authorizedClientService.loadAuthorizedClient("spotify", userId)).thenReturn(null);
        doNothing().when(spotifyApi).setAccessToken(accessToken);

        // Mocking getCurrentUsersProfile request
        GetCurrentUsersProfileRequest.Builder requestBuilder = mock(GetCurrentUsersProfileRequest.Builder.class);
        GetCurrentUsersProfileRequest request = mock(GetCurrentUsersProfileRequest.class);
        User mockUser = mock(User.class);

        when(spotifyApi.getCurrentUsersProfile()).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
        when(request.execute()).thenReturn(mockUser);
        when(mockUser.getDisplayName()).thenReturn("Test User");

        String result = securityConfig.getSpotifyUserName(userId, accessToken);
        assertThat(result).isEqualTo("Test User");
    }
}
