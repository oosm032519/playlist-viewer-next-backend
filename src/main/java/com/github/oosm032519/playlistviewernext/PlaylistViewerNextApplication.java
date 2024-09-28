package com.github.oosm032519.playlistviewernext;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * PlaylistViewerNextアプリケーションのメインクラス。
 * このクラスはSpring Bootアプリケーションのエントリーポイントとして機能します。
 */
@SpringBootApplication
@EnableCaching
@EnableAspectJAutoProxy
public class PlaylistViewerNextApplication {

    /**
     * アプリケーションのメインメソッド。
     * Spring Bootアプリケーションを起動します。
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        SpringApplication.run(PlaylistViewerNextApplication.class, args);
    }
}
