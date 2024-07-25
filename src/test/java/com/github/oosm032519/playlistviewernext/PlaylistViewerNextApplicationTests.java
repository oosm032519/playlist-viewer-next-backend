//// PlaylistViewerNextApplicationTests.java
//
//package com.github.oosm032519.playlistviewernext;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.context.ApplicationContext;
//import org.springframework.core.env.Environment;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//@SpringBootTest
//class PlaylistViewerNextApplicationTests {
//
//    @Autowired
//    private ApplicationContext applicationContext;
//
//    @Autowired
//    private Environment environment;
//
//    @Test
//    void contextLoads() {
//        // Arrange & Act are implicit in this case
//        // Assert
//        assertThat(applicationContext).isNotNull();
//    }
//
//    @Test
//    void mainMethodStartsApplication() {
//        // Arrange & Act
//        PlaylistViewerNextApplication.main(new String[]{});
//        // Assert
//        assertThat(applicationContext).isNotNull();
//    }
//
//    @Test
//    void applicationHasExpectedBeans() {
//        // Arrange & Act
//        String[] beanNames = applicationContext.getBeanDefinitionNames();
//        // Assert
//        assertThat(beanNames).isNotEmpty();
//        assertThat(applicationContext.getBean(PlaylistViewerNextApplication.class)).isNotNull();
//    }
//
//    @Test
//    void applicationNameIsCorrect() {
//        // Arrange & Act
//        String applicationId = applicationContext.getId();
//        // Assert
//        assertThat(applicationId).isEqualTo("playlist-viewer-next-backend");
//    }
//
//    @Test
//    void defaultProfileIsSet() {
//        // Arrange & Act
//        String[] activeProfiles = environment.getActiveProfiles();
//        String[] defaultProfiles = environment.getDefaultProfiles();
//        String[] profiles = activeProfiles.length == 0 ? defaultProfiles : activeProfiles;
//        // Assert
//        assertThat(profiles).isNotEmpty();
//    }
//}
