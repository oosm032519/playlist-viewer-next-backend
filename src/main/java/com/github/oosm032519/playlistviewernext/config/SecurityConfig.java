package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.filter.JwtAuthenticationFilter;
import com.github.oosm032519.playlistviewernext.service.auth.SpotifyOAuth2UserService;
import com.github.oosm032519.playlistviewernext.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Securityの設定クラス。
 * アプリケーションのセキュリティ設定、認証・認可の制御、CORSの設定、JWTの処理などを管理する。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * CORSの設定ソース
     */
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    /**
     * JWT関連のユーティリティクラス
     */
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * SpotifyのOAuth2認証サービス
     */
    @Lazy
    @Autowired
    private SpotifyOAuth2UserService spotifyOAuth2UserService;

    /**
     * Spotifyログイン成功時のハンドラー
     */
    @Autowired
    private SpotifyLoginSuccessHandler spotifyLoginSuccessHandler;

    /**
     * セキュリティフィルターチェーンの設定を行う。(モックモード用)
     *
     * @param http HttpSecurityオブジェクト
     * @return 設定済みのSecurityFilterChain
     * @throws Exception セキュリティ設定中に例外が発生した場合
     */
    @Bean
    @ConditionalOnProperty(name = "spotify.mock.enabled", havingValue = "true")
    public SecurityFilterChain securityFilterChainMock(HttpSecurity http) throws Exception {
        http
                // CORSの設定を適用
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRFを無効化
                .csrf(AbstractHttpConfigurer::disable)
                // セッション管理をSTATELESSに設定
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URLごとのアクセス制御を設定
                .authorizeHttpRequests(authz -> authz
                        // 公開エンドポイントの設定
                        .requestMatchers("/", "/error", "/webjars/**", "/api/playlists/search",
                                "/api/playlists/{id}/details", "api/playlists/recommendations",
                                "api/session/sessionId").permitAll()
                        // その他のリクエストは認証が必要
                        .anyRequest().authenticated()
                )
                // OAuth2ログインの設定
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/spotify")
                        .userInfoEndpoint(userInfo -> userInfo.userService(spotifyOAuth2UserService))
                        .successHandler(spotifyLoginSuccessHandler)
                );

        return http.build();
    }

    /**
     * セキュリティフィルターチェーンの設定を行う。(実処理モード用)
     *
     * @param http HttpSecurityオブジェクト
     * @return 設定済みのSecurityFilterChain
     * @throws Exception セキュリティ設定中に例外が発生した場合
     */
    @Bean
    @ConditionalOnProperty(name = "spotify.mock.enabled", havingValue = "false", matchIfMissing = true)
    public SecurityFilterChain securityFilterChainReal(HttpSecurity http) throws Exception {
        http
                // CORSの設定を適用
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRFを無効化
                .csrf(AbstractHttpConfigurer::disable)
                // セッション管理をSTATELESSに設定
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URLごとのアクセス制御を設定
                .authorizeHttpRequests(authz -> authz
                        // 公開エンドポイントの設定
                        .requestMatchers("/", "/error", "/webjars/**", "/api/playlists/search",
                                "api/session/sessionId").permitAll()
                        // detailsエンドポイントはモックモードでは認証不要、実処理モードでは認証が必要
                        .requestMatchers("/api/playlists/{id}/details").permitAll()
                        .requestMatchers("api/playlists/recommendations").permitAll()
                        // その他のリクエストは認証が必要
                        .anyRequest().authenticated()
                )
                // OAuth2ログインの設定
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/oauth2/authorization/spotify")
                        .userInfoEndpoint(userInfo -> userInfo.userService(spotifyOAuth2UserService))
                        .successHandler(spotifyLoginSuccessHandler)
                );

        // JWT認証フィルターを適用
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * JWTの認証フィルターを生成する。
     *
     * @return 設定済みのJwtAuthenticationFilter
     */
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil);
    }
}
