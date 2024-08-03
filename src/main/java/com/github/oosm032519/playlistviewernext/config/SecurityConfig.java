package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.filter.JwtAuthenticationFilter;
import com.github.oosm032519.playlistviewernext.service.auth.SpotifyOAuth2UserService;
import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.RequestCacheConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);
    private static final String SPOTIFY_CLIENT_ID = "spotify";

    @Autowired
    private JwtUtil jwtUtil;

    @Lazy
    @Autowired
    private SpotifyOAuth2UserService spotifyOAuth2UserService;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    private SpotifyApi spotifyApi;
    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                        .disable())
                .requestCache(RequestCacheConfigurer::disable)
                .securityContext(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/error", "/webjars/**", "/api/playlists/search", "/api/playlists/{id}", "/loginSuccess", "/api/session/check", "/api/playlists/favorites", "api/playlists/followed", "api/playlist/add-track", "api/playlist/remove-track").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/spotify")
                        .defaultSuccessUrl("/loginSuccess", true)
                        .userInfoEndpoint(userInfo -> userInfo
                                .userService(spotifyOAuth2UserService)
                        )
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                            String userId = oauth2User.getAttribute("id");
                            String spotifyAccessToken = oauth2User.getAttribute("access_token");

                            // Spotify API を使用してユーザー名を取得
                            String userName = getSpotifyUserName(userId, spotifyAccessToken);

                            // JWTトークンの生成
                            Map<String, Object> claims = new HashMap<>();
                            claims.put("sub", userId);
                            claims.put("name", userName); // Spotify 上のユーザー名を設定
                            claims.put("spotify_access_token", spotifyAccessToken);

                            String token = jwtUtil.generateToken(claims);

                            // JWT トークンをハッシュに含めてリダイレクト
                            response.sendRedirect(frontendUrl + "#token=" + token);
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private String getSpotifyUserName(String userId, String spotifyAccessToken) {
        try {
            OAuth2AuthorizedClient authorizedClient = authorizedClientService.loadAuthorizedClient(SPOTIFY_CLIENT_ID, userId);
            if (authorizedClient != null) {
                spotifyApi.setAccessToken(authorizedClient.getAccessToken().getTokenValue());
            } else {
                spotifyApi.setAccessToken(spotifyAccessToken);
            }
            User user = spotifyApi.getCurrentUsersProfile().build().execute();
            return user.getDisplayName();
        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            logger.error("Spotify APIからユーザー名を取得できませんでした", e);
            return userId; // エラーが発生した場合はユーザーIDを返す
        }
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SpotifyOAuth2UserService spotifyOAuth2UserService() {
        return new SpotifyOAuth2UserService();
    }
}
