package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.filter.JwtAuthenticationFilter;
import com.github.oosm032519.playlistviewernext.service.auth.SpotifyOAuth2UserService;
import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import jakarta.servlet.http.Cookie;
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
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtUtil jwtUtil;

    @Lazy
    @Autowired
    private SpotifyOAuth2UserService spotifyOAuth2UserService;

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
                        .requestMatchers("/", "/error", "/webjars/**", "/api/playlists/search", "/api/playlists/{id}", "/loginSuccess", "/api/session/check", "/api/playlists/favorite", "api/playlists/followed", "api/playlist/add-track", "api/playlist/remove-track").permitAll()
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
                            String username = oauth2User.getAttribute("id");
                            String spotifyAccessToken = oauth2User.getAttribute("access_token");

                            // JWTトークンの生成
                            Map<String, Object> claims = new HashMap<>();
                            claims.put("sub", username);
                            claims.put("name", username);
                            claims.put("spotify_access_token", spotifyAccessToken);

                            String token = jwtUtil.generateToken(claims);

                            // JWTをCookieに設定
                            Cookie jwtCookie = new Cookie("JWT", token);
                            jwtCookie.setHttpOnly(true);
                            jwtCookie.setSecure(true);
                            jwtCookie.setPath("/");
                            jwtCookie.setMaxAge(3600); // 1時間

                            response.addCookie(jwtCookie);
                            response.sendRedirect(frontendUrl);
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class); // フィルターチェーンに追加

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(frontendUrl)); // 許可するオリジンを厳密に指定
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Value("${frontend.url}")
    private String frontendUrl;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }

    @Bean
    public SpotifyOAuth2UserService spotifyOAuth2UserService() {
        return new SpotifyOAuth2UserService();
    }
}
