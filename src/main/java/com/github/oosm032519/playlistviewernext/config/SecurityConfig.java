package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.filter.JwtAuthenticationFilter;
import com.github.oosm032519.playlistviewernext.service.auth.SpotifyOAuth2UserService;
import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private JwtUtil jwtUtil;

    @Lazy
    @Autowired
    private SpotifyOAuth2UserService spotifyOAuth2UserService;

    @Autowired
    private SpotifyLoginSuccessHandler spotifyLoginSuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/", "/error", "/webjars/**", "/api/playlists/search", "/api/playlists/{id}/details", "api/playlists/recommendations", "api/session/sessionId").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/spotify")
                        .userInfoEndpoint(userInfo -> userInfo.userService(spotifyOAuth2UserService))
                        .successHandler(spotifyLoginSuccessHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }
}
