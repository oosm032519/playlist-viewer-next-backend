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

    @Test
    void contextLoads() {
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void mainMethodStartsApplication() {
        PlaylistViewerNextApplication.main(new String[]{});
        assertThat(applicationContext).isNotNull();
    }

    @Test
    void applicationHasExpectedBeans() {
        assertThat(applicationContext.getBeanDefinitionNames()).isNotEmpty();
        assertThat(applicationContext.getBean(PlaylistViewerNextApplication.class)).isNotNull();
    }

    @Test
    void applicationNameIsCorrect() {
        assertThat(applicationContext.getId()).isEqualTo("playlist-viewer-next-backend");
    }

    @Test
    void defaultProfileIsSet() {
        String[] activeProfiles = environment.getActiveProfiles();
        String[] defaultProfiles = environment.getDefaultProfiles();

        assertThat(activeProfiles.length == 0 ? defaultProfiles : activeProfiles).isNotEmpty();
    }
}
