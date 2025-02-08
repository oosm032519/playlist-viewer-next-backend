package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
import com.github.oosm032519.playlistviewernext.service.analytics.PlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.playlist.PlaylistDetailsRetrievalService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistDetailsControllerTest {

    @Mock
    private PlaylistDetailsRetrievalService playlistDetailsRetrievalService;

    @Mock
    private PlaylistAnalyticsService playlistAnalyticsService;

    @InjectMocks
    private PlaylistDetailsController detailsController;

    /**
     * プレイリストの詳細情報とジャンル分析結果が正常に取得できることを確認する。
     */
    @Test
    void shouldReturnPlaylistDetailsSuccessfully() {
        // Arrange: テストデータの準備
        String playlistId = "testPlaylistId";
        Map<String, Object> playlistDetails = new HashMap<>(createTestPlaylistDetails());
        Map<String, Integer> genreCounts = new HashMap<>(Map.of("pop", 2, "rock", 1));

        when(playlistDetailsRetrievalService.getPlaylistDetails(playlistId)).thenReturn(playlistDetails);
        when(playlistAnalyticsService.getGenreCountsForPlaylist(playlistId)).thenReturn(genreCounts);

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = detailsController.getPlaylistDetails(playlistId);

        // Assert: 結果の検証
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("genreCounts")).isEqualTo(genreCounts);
        assertThat(response.getBody().get("tracks")).isEqualTo(Map.of("items", List.of(Map.of("track", "track1"), Map.of("track", "track2"))));
        assertThat(response.getBody().get("playlistName")).isEqualTo("Test Playlist");
        assertThat(response.getBody().get("ownerId")).isEqualTo("ownerId");
        assertThat(response.getBody().get("ownerName")).isEqualTo("Owner Name");

        verify(playlistDetailsRetrievalService).getPlaylistDetails(playlistId);
        verify(playlistAnalyticsService).getGenreCountsForPlaylist(playlistId);
    }

    private Map<String, Object> createTestPlaylistDetails() {
        Map<String, Object> details = new HashMap<>();
        details.put("tracks", Map.of("items", List.of(Map.of("track", "track1"), Map.of("track", "track2"))));
        details.put("playlistName", "Test Playlist");
        details.put("ownerId", "ownerId");
        details.put("ownerName", "Owner Name");
        details.put("maxAudioFeatures", new HashMap<>(Map.of("feature1", 1.0f)));
        details.put("minAudioFeatures", new HashMap<>(Map.of("feature1", 0.1f)));
        details.put("averageAudioFeatures", new HashMap<>(Map.of("feature1", 0.5f)));
        return details;
    }

    /**
     * プレイリストが見つからない場合に、ResourceNotFoundExceptionがスローされることを確認する。
     */
    @Test
    void shouldHandleResourceNotFoundException() {
        // Arrange: モックの設定
        String playlistId = "testPlaylistId";
        ResourceNotFoundException exception = new ResourceNotFoundException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
        when(playlistDetailsRetrievalService.getPlaylistDetails(playlistId)).thenThrow(exception);

        // Act & Assert: 例外がスローされることの確認
        Throwable thrown = catchThrowable(() -> detailsController.getPlaylistDetails(playlistId));
        assertThat(thrown).isSameAs(exception);

        verify(playlistDetailsRetrievalService).getPlaylistDetails(playlistId);
    }
}
