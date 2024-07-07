package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.GetPlaylistDetails;
import com.github.oosm032519.playlistviewernext.service.AnalyticsService;
import com.github.oosm032519.playlistviewernext.service.RecommendationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistDetailsControllerTest {

    @Mock
    private GetPlaylistDetails getPlaylistDetails;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private RecommendationService recommendationService;

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
                "genreCounts", Map.of("pop", 2, "rock", 1),
                "recommendations", List.of("Recommended Track 1", "Recommended Track 2"),
                "playlistName", "Test Playlist",
                "ownerId", "ownerId",
                "ownerName", "Owner Name"
        );

        when(getPlaylistDetails.getPlaylistDetails(playlistId)).thenReturn(playlistDetails);

        // When
        ResponseEntity<Map<String, Object>> response = detailsController.getPlaylistById(playlistId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody()).isEqualTo(playlistDetails);

        verify(getPlaylistDetails).getPlaylistDetails(playlistId);
    }

    @Test
    void getPlaylistById_HandlesExceptionGracefully() throws Exception {
        // Given
        String playlistId = "testPlaylistId";
        when(getPlaylistDetails.getPlaylistDetails(playlistId)).thenThrow(new RuntimeException("API error"));

        // When
        ResponseEntity<Map<String, Object>> response = detailsController.getPlaylistById(playlistId);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("error")).isEqualTo("API error");

        verify(getPlaylistDetails).getPlaylistDetails(playlistId);
    }
}
