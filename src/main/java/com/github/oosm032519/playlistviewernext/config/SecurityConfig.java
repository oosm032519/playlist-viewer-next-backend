package com.github.oosm032519.playlistviewernext.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private static final String[] PERMIT_ALL_URLS = {
            "/", "/error", "/webjars/**", "/api/session/check", "/api/playlists/search", "/api/playlists/{id}", "/api/logout", "/api/playlists/favoriteCheck"
    };

    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${frontend.url}")
    private String frontendUrl;

    public SecurityConfig(OAuth2AuthorizedClientService authorizedClientService) {
        this.authorizedClientService = authorizedClientService;
        logger.info("SecurityConfig が初期化されました。authorizedClientService: {}", authorizedClientService);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.debug("securityFilterChain の設定を開始します");
        http
                .cors(cors -> {
                    logger.debug("CORS の設定を適用します");
                    cors.configurationSource(corsConfigurationSource());
                })
                .csrf(csrf -> {
                    logger.debug("CSRF 保護を無効化します");
                    csrf.disable();
                })
                .authorizeHttpRequests(authz -> {
                    logger.debug("HTTP リクエストの認可ルールを設定します");
                    authz
                            .requestMatchers(PERMIT_ALL_URLS).permitAll()
                            .anyRequest().authenticated();
                    logger.info("認可ルールが設定されました。許可されたURL: {}", String.join(", ", PERMIT_ALL_URLS));
                })
                .oauth2Login(oauth2 -> {
                    logger.debug("OAuth2 ログインの設定を適用します");
                    oauth2.successHandler(authenticationSuccessHandler());
                });

        logger.info("SecurityFilterChain の設定が完了しました");
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        logger.debug("CORS 設定を開始します");
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> allowedOrigins = List.of(frontendUrl, "http://localhost:3000", "https://playlist-viewer-next-frontend.vercel.app");
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        logger.info("CORS 設定が完了しました。許可されたオリジン: {}", allowedOrigins);
        return source;
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        logger.debug("認証成功ハンドラを設定します");
        return (request, response, authentication) -> {
            try {
                logger.info("認証が成功しました。ユーザー: {}", authentication.getName());
                OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
                OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
                OAuth2AuthorizedClient client = authorizedClientService
                        .loadAuthorizedClient("spotify", oauthToken.getName());

                String accessToken = client.getAccessToken().getTokenValue();
                String userId = oAuth2User.getAttribute("id");
                String displayName = oAuth2User.getAttribute("display_name");

                logger.debug("セッションにユーザー情報を保存します。userId: {}, displayName: {}", userId, displayName);
                request.getSession().setAttribute("accessToken", accessToken);
                request.getSession().setAttribute("userId", userId);
                request.getSession().setAttribute("displayName", displayName);

                logger.info("認証成功後、フロントエンドにリダイレクトします。URL: {}", frontendUrl);
                response.sendRedirect(frontendUrl);
            } catch (Exception e) {
                logger.error("セッションにユーザー情報を保存中にエラーが発生しました", e);
                response.sendRedirect("/error");
            }
        };
    }
}
