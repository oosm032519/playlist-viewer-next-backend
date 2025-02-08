package com.github.oosm032519.playlistviewernext;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class PlaylistViewerNextApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private Environment environment;

    /**
     * Spring Bootアプリケーションのコンテキストが正常にロードされることを確認する。
     */
    @Test
    void contextLoads() {
        // Arrange & Act: コンテキストのロードはSpring Bootによって自動的に行われる

        // Assert: ApplicationContextがnullでないことを確認する
        assertThat(applicationContext).isNotNull();
    }

    /**
     * mainメソッドがアプリケーションを正常に起動することを確認する。
     */
    @Test
    void mainMethodStartsApplication() {
        // Arrange & Act: mainメソッドを実行する
        PlaylistViewerNextApplication.main(new String[]{});

        // Assert: ApplicationContextがnullでないことを確認する
        assertThat(applicationContext).isNotNull();
    }

    /**
     * アプリケーションに必要なBeanが期待通りに存在することを確認する。
     */
    @Test
    void applicationHasExpectedBeans() {
        // Arrange & Act: Bean定義名を取得する
        String[] beanNames = applicationContext.getBeanDefinitionNames();

        // Assert: Bean定義名が空でなく、アプリケーションクラスのBeanが存在することを確認する
        assertThat(beanNames).isNotEmpty();
        assertThat(applicationContext.getBean(PlaylistViewerNextApplication.class)).isNotNull();
    }

    /**
     * アプリケーションIDが期待通りに設定されていることを確認する。
     */
    @Test
    void applicationNameIsCorrect() {
        // Arrange & Act: アプリケーションIDを取得する
        String applicationId = applicationContext.getId();

        // Assert: アプリケーションIDが期待値と一致することを確認する
        assertThat(applicationId).isEqualTo("playlist-viewer-next-backend");
    }

    /**
     * デフォルトのプロファイルが設定されていることを確認する。
     */
    @Test
    void defaultProfileIsSet() {
        // Arrange & Act: アクティブなプロファイルとデフォルトのプロファイルを取得する
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();
        String[] profiles = activeProfiles.length == 0 ? defaultProfiles : activeProfiles;

        // Assert: プロファイルが空でないことを確認する
        assertThat(profiles).isNotEmpty();
    }
}
