package com.github.oosm032519.playlistviewernext.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.ArrayList;
import java.util.List;

/**
 * CORS（Cross-Origin Resource Sharing）設定を行うクラス。
 * フロントエンドアプリケーションとの通信を許可するために、CORSを設定する。
 */
@Configuration
public class CorsConfig {

    /**
     * フロントエンドのURLを設定ファイルから取得する。
     * `@Value`アノテーションを使用して、プロパティファイルから値を注入する。
     */
    @Value("${frontend.url}")
    private String frontendUrl;

    /**
     * モックサーバーのURLを設定ファイルから取得する。
     */
    @Value("${spotify.mock-api.url}")
    private String mockApiUrl;

    /**
     * モックサーバーが有効かどうかを設定ファイルから取得する。
     */
    @Value("${spotify.mock.enabled}")
    private boolean mockEnabled;

    /**
     * CORS設定を構成するメソッド。
     * 特定のオリジン、HTTPメソッド、およびヘッダーを許可する設定を行う。
     *
     * @return CorsConfigurationSource CORS設定が適用されたソース
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // CORSの設定を保持するオブジェクトを作成
        CorsConfiguration configuration = new CorsConfiguration();

        // 許可するオリジンのリストを作成
        List<String> allowedOrigins = new ArrayList<>();
        allowedOrigins.add(frontendUrl);

        // モックサーバーが有効な場合、モックサーバーのURLも許可するオリジンに追加
        if (mockEnabled) {
            allowedOrigins.add(mockApiUrl);
        }

        // 許可するオリジンを設定
        configuration.setAllowedOrigins(allowedOrigins);

        // 許可するHTTPメソッドのリストを設定
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        // 全てのリクエストヘッダーを許可
        configuration.setAllowedHeaders(List.of("*"));

        // クッキーや認証情報の送信を許可する
        configuration.setAllowCredentials(true);

        // URLパターンに基づいたCORS設定を適用するためのソースを作成
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();

        // 全てのエンドポイントに対して、上記のCORS設定を適用
        source.registerCorsConfiguration("/**", configuration);

        // 設定したCORSソースを返す
        return source;
    }
}
