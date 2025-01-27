package com.github.oosm032519.playlistviewernext.config;

import com.github.oosm032519.playlistviewernext.filter.SessionAuthenticationFilter;
import com.github.oosm032519.playlistviewernext.service.auth.SpotifyOAuth2UserService;
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
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Spring Securityの設定クラス。
 * アプリケーションのセキュリティ設定、認証・認可の制御、CORSの設定などを管理する。
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    /**
     * CORSの設定ソース
     */
    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    /**
     * SpotifyのOAuth2認証サービス
     */
    @Lazy
    @Autowired
    private SpotifyOAuth2UserService spotifyOAuth2UserService;

    @Value("${spotify.mock.enabled}")
    private boolean mockEnabled;

    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * セキュリティフィルターチェーンの設定を行う。(モックモード用と実処理モード共通化)
     *
     * @param http HttpSecurityオブジェクト
     * @return 設定済みのSecurityFilterChain
     * @throws Exception セキュリティ設定中に例外が発生した場合
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("securityFilterChainが呼び出されました。");
        logger.info("モックモード: {}", mockEnabled);

        // SpotifyLoginSuccessHandler をここでインスタンス化
        SpotifyLoginSuccessHandler spotifyLoginSuccessHandler = new SpotifyLoginSuccessHandler(frontendUrl, mockEnabled);

        http
                // CORSの設定を適用
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                // CSRFを無効化
                .csrf(AbstractHttpConfigurer::disable)
                // セッション管理をSTATELESSに設定 (JWTを使用しないため、任意の設定に変更可能)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // URLごとのアクセス制御を設定
                .authorizeHttpRequests(authz -> authz
                        // 公開エンドポイントの設定 (モックモード、実処理モード共通)
                        .requestMatchers("/", "/error", "/webjars/**", "/api/playlists/search",
                                "api/session/sessionId", "/api/playlists/{id}/details", "api/playlists/recommendations", "/api/session/mock-login").permitAll()
                        .anyRequest().authenticated()
                )
                // OAuth2ログイン設定 (モックモードが無効の場合のみ適用)
                .oauth2Login(oauth2 -> {
                    if (!mockEnabled) {
                        logger.info("OAuth2ログイン設定を適用します。");
                        oauth2
                                .loginPage("/oauth2/authorization/spotify")
                                .userInfoEndpoint(userInfo -> userInfo.userService(spotifyOAuth2UserService))
                                .successHandler(spotifyLoginSuccessHandler);
                    } else {
                        logger.info("モックモードが有効なため、OAuth2ログイン設定は適用しません。");
                    }
                });

        // セッション認証フィルターを適用 (モックモード、実処理モード共通)
        http.addFilterAfter(sessionAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * セッション認証フィルターを生成する。
     *
     * @return 設定済みのSessionAuthenticationFilter
     */
    @Bean
    public SessionAuthenticationFilter sessionAuthenticationFilter() {
        return new SessionAuthenticationFilter();
    }
}
