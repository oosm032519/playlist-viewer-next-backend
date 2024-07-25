//package com.github.oosm032519.playlistviewernext.service.analytics;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.mockito.InjectMocks;
//import org.mockito.MockitoAnnotations;
//import org.springframework.boot.test.context.SpringBootTest;
//import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.assertj.core.api.AssertionsForClassTypes.within;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//@SpringBootTest
//public class AverageAudioFeaturesCalculatorTest {
//
//    @InjectMocks
//    private AverageAudioFeaturesCalculator calculator;
//
//    @BeforeEach
//    public void setUp() {
//        MockitoAnnotations.openMocks(this);
//    }
//
//    @Test
//    public void testCalculateAverageAudioFeatures() {
//        // Mock AudioFeatures
//        AudioFeatures audioFeatures1 = mock(AudioFeatures.class);
//        AudioFeatures audioFeatures2 = mock(AudioFeatures.class);
//
//        // Define mock behavior
//        when(audioFeatures1.getDanceability()).thenReturn(0.5f);
//        when(audioFeatures1.getEnergy()).thenReturn(0.6f);
//        when(audioFeatures1.getValence()).thenReturn(0.7f);
//        when(audioFeatures1.getTempo()).thenReturn(120.0f);
//        when(audioFeatures1.getAcousticness()).thenReturn(0.1f);
//        when(audioFeatures1.getInstrumentalness()).thenReturn(0.2f);
//        when(audioFeatures1.getLiveness()).thenReturn(0.3f);
//        when(audioFeatures1.getSpeechiness()).thenReturn(0.4f);
//
//        when(audioFeatures2.getDanceability()).thenReturn(0.7f);
//        when(audioFeatures2.getEnergy()).thenReturn(0.8f);
//        when(audioFeatures2.getValence()).thenReturn(0.9f);
//        when(audioFeatures2.getTempo()).thenReturn(130.0f);
//        when(audioFeatures2.getAcousticness()).thenReturn(0.2f);
//        when(audioFeatures2.getInstrumentalness()).thenReturn(0.3f);
//        when(audioFeatures2.getLiveness()).thenReturn(0.4f);
//        when(audioFeatures2.getSpeechiness()).thenReturn(0.5f);
//
//        // Create track list
//        Map<String, Object> track1 = new HashMap<>();
//        track1.put("audioFeatures", audioFeatures1);
//
//        Map<String, Object> track2 = new HashMap<>();
//        track2.put("audioFeatures", audioFeatures2);
//
//        List<Map<String, Object>> trackList = List.of(track1, track2);
//
//        // Calculate average audio features
//        Map<String, Float> result = calculator.calculateAverageAudioFeatures(trackList);
//
//        // Assert results
//        assertThat(result).isNotNull();
//        assertThat(result.get("danceability")).isCloseTo(0.6f, within(0.00001f));
//        assertThat(result.get("energy")).isCloseTo(0.7f, within(0.00001f));
//        assertThat(result.get("valence")).isCloseTo(0.8f, within(0.00001f));
//        assertThat(result.get("tempo")).isCloseTo(125.0f, within(0.00001f));
//        assertThat(result.get("acousticness")).isCloseTo(0.15f, within(0.00001f));
//        assertThat(result.get("instrumentalness")).isCloseTo(0.25f, within(0.00001f));
//        assertThat(result.get("liveness")).isCloseTo(0.35f, within(0.00001f));
//        assertThat(result.get("speechiness")).isCloseTo(0.45f, within(0.00001f));
//    }
//}
