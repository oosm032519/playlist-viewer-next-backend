package com.github.oosm032519.playlistviewernext.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenAPIの設定を管理するコンフィグレーションクラス。
 * SpringDocを使用してAPIドキュメンテーションを生成するための設定を提供する。
 *
 * @since 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * カスタマイズされたOpenAPI設定を提供するBeanを定義する。
     * APIのタイトル、バージョン、説明などの基本情報を設定する。
     *
     * @return カスタマイズされたOpenAPI設定を含むOpenAPIオブジェクト
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // OpenAPIの基本情報を設定
        return new OpenAPI()
                .info(new Info()
                        .title("Sample API")          // APIのタイトルを設定
                        .version("1.0.0")            // APIのバージョンを設定
                        .description("Sample API for demonstrating OpenAPI with SpringDoc")); // APIの説明を設定
    }
}
