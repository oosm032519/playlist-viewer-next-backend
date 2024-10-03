package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
import com.github.oosm032519.playlistviewernext.service.analytics.PlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.playlist.PlaylistDetailsRetrievalService;
import com.github.oosm032519.playlistviewernext.service.recommendation.TrackRecommendationService;
import jakarta.servlet.http.HttpServletRequest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistDetailsControllerTest {

    @Mock
    private PlaylistDetailsRetrievalService playlistDetailsRetrievalService;

    @Mock
    private PlaylistAnalyticsService playlistAnalyticsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private TrackRecommendationService trackRecommendationService;

    @InjectMocks
    private PlaylistDetailsController detailsController;

    @BeforeEach
    void setUp() {
        reset(playlistDetailsRetrievalService, playlistAnalyticsService, trackRecommendationService);
    }

    @Test
    void shouldReturnPlaylistDetailsSuccessfully() throws Exception {
        // Given
        String playlistId = "testPlaylistId";
        Map<String, Object> playlistDetails = createTestPlaylistDetails();
        Map<String, Integer> genreCounts = Map.of("pop", 2, "rock", 1);
        List<String> top5Genres = List.of("pop", "rock");
        List<Track> recommendations = List.of(mock(Track.class), mock(Track.class));

        when(playlistDetailsRetrievalService.getPlaylistDetails(playlistId)).thenReturn(playlistDetails);
        when(playlistAnalyticsService.getGenreCountsForPlaylist(playlistId)).thenReturn(genreCounts);
        when(playlistAnalyticsService.getTop5GenresForPlaylist(playlistId)).thenReturn(top5Genres);
        when(trackRecommendationService.getRecommendations(
                eq(top5Genres),
                eq(Map.of("feature1", 1.0f)),
                eq(Map.of("feature1", 0.1f)),
                eq(Map.of("feature1", 0.5f)),
                eq(Map.of("feature1", 0.5f))
        )).thenReturn(recommendations);

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
        verify(trackRecommendationService).getRecommendations(
                eq(top5Genres),
                eq(Map.of("feature1", 1.0f)),
                eq(Map.of("feature1", 0.1f)),
                eq(Map.of("feature1", 0.5f)),
                eq(Map.of("feature1", 0.5f))
        );
    }

    @Test
    void shouldHandleResourceNotFoundException() {
        // Given
        String playlistId = "testPlaylistId";
        when(playlistDetailsRetrievalService.getPlaylistDetails(playlistId)).thenThrow(new ResourceNotFoundException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "プレイリストが見つかりません"));

        // When & Then
        assertThatThrownBy(() -> detailsController.getPlaylistById(playlistId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.NOT_FOUND)
                .hasFieldOrPropertyWithValue("errorCode", "RESOURCE_NOT_FOUND")
                .hasMessage("プレイリストが見つかりません");

        verify(playlistDetailsRetrievalService).getPlaylistDetails(playlistId);
    }

    @Test
    void shouldHandleSpotifyApiException() {
        // Given
        String playlistId = "testPlaylistId";
        when(playlistDetailsRetrievalService.getPlaylistDetails(playlistId)).thenThrow(new SpotifyApiException(HttpStatus.BAD_REQUEST, "SPOTIFY_API_ERROR", "Spotify API エラー", new RuntimeException("API error")));

        // When & Then
        assertThatThrownBy(() -> detailsController.getPlaylistById(playlistId))
                .isInstanceOf(SpotifyApiException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.BAD_REQUEST)
                .hasFieldOrPropertyWithValue("errorCode", "SPOTIFY_API_ERROR")
                .hasMessage("Spotify API エラー");

        verify(playlistDetailsRetrievalService).getPlaylistDetails(playlistId);
    }

    @Test
    void shouldHandleGenericException() {
        // Given
        String playlistId = "testPlaylistId";
        when(playlistDetailsRetrievalService.getPlaylistDetails(playlistId)).thenThrow(new RuntimeException("API error"));

        // When & Then
        assertThatThrownBy(() -> detailsController.getPlaylistById(playlistId))
                .isInstanceOf(PlaylistViewerNextException.class)
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR)
                .hasFieldOrPropertyWithValue("errorCode", "PLAYLIST_DETAILS_ERROR")
                .hasMessage("プレイリスト情報の取得中にエラーが発生しました。URLが正しいか確認し、しばらく時間をおいてから再度お試しください。");

        verify(playlistDetailsRetrievalService).getPlaylistDetails(playlistId);
    }

    private Map<String, Object> createTestPlaylistDetails() {
        return Map.of(
                "tracks", Map.of("items", List.of(Map.of("track", "track1"), Map.of("track", "track2"))),
                "playlistName", "Test Playlist",
                "ownerId", "ownerId",
                "ownerName", "Owner Name",
                "maxAudioFeatures", Map.of("feature1", 1.0f),
                "minAudioFeatures", Map.of("feature1", 0.1f),
                "medianAudioFeatures", Map.of("feature1", 0.5f),
                "modeValues", Map.of("feature1", 0.5f)
        );
    }
}
