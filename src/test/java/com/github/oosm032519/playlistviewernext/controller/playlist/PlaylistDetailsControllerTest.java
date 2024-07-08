package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.service.playlist.PlaylistDetailsRetrievalService;
import com.github.oosm032519.playlistviewernext.service.analytics.PlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.recommendation.TrackRecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistDetailsControllerTest {

    @Mock
    private PlaylistDetailsRetrievalService playlistDetailsRetrievalService;

    @Mock
    private PlaylistAnalyticsService playlistAnalyticsService;

    @Mock
    private TrackRecommendationService trackRecommendationService;

    @InjectMocks
    private PlaylistDetailsController detailsController;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void getPlaylistById_ReturnsPlaylistDetailsSuccessfully() throws Exception {
        // Given
        String playlistId = "testPlaylistId";
        Map<String, Object> playlistDetails = Map.of(
                "tracks", Map.of("items", List.of(Map.of("track", "track1"), Map.of("track", "track2"))),
                "playlistName", "Test Playlist",
                "ownerId", "ownerId",
                "ownerName", "Owner Name"
        );
        Map<String, Integer> genreCounts = Map.of("pop", 2, "rock", 1);
        List<String> top5Genres = List.of("pop", "rock");
        List<Track> recommendations = List.of(
                mock(Track.class), // モックオブジェクトを使用
                mock(Track.class)
        );

        when(playlistDetailsRetrievalService.getPlaylistDetails(playlistId)).thenReturn(playlistDetails);
        when(playlistAnalyticsService.getGenreCountsForPlaylist(playlistId)).thenReturn(genreCounts);
        when(playlistAnalyticsService.getTop5GenresForPlaylist(playlistId)).thenReturn(top5Genres);
        when(trackRecommendationService.getRecommendations(top5Genres)).thenReturn(recommendations);

        // When
        ResponseEntity<Map<String, Object>> response = detailsController.getPlaylistById(playlistId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("genreCounts")).isEqualTo(genreCounts);
        assertThat(response.getBody().get("recommendations")).isEqualTo(recommendations);
        assertThat(response.getBody().get("tracks")).isEqualTo(Map.of("items", List.of(Map.of("track", "track1"), Map.of("track", "track2"))));
        assertThat(response.getBody().get("playlistName")).isEqualTo("Test Playlist");
        assertThat(response.getBody().get("ownerId")).isEqualTo("ownerId");
        assertThat(response.getBody().get("ownerName")).isEqualTo("Owner Name");

        verify(playlistDetailsRetrievalService).getPlaylistDetails(playlistId);
        verify(playlistAnalyticsService).getGenreCountsForPlaylist(playlistId);
        verify(playlistAnalyticsService).getTop5GenresForPlaylist(playlistId);
        verify(trackRecommendationService).getRecommendations(top5Genres);
    }

    @Test
    void getPlaylistById_HandlesExceptionGracefully() throws Exception {
        // Given
        String playlistId = "testPlaylistId";
        when(playlistDetailsRetrievalService.getPlaylistDetails(playlistId)).thenThrow(new RuntimeException("API error"));

        // When
        ResponseEntity<Map<String, Object>> response = detailsController.getPlaylistById(playlistId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("API error");

        verify(playlistDetailsRetrievalService).getPlaylistDetails(playlistId);
    }
}
