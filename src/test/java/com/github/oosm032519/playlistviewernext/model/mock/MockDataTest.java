package com.github.oosm032519.playlistviewernext.model.mock;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MockDataTest {

    private static ObjectMapper objectMapper;

    @BeforeAll
    static void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void getMockedPlaylistSearchResponse_shouldReturnValidResponse() {
        // Arrange
        int offset = 0;
        int limit = 20;

        // Act
        Map<String, Object> response = MockData.getMockedPlaylistSearchResponse(offset, limit);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response).containsKey("playlists");
        assertThat(response).containsKey("total");
        assertThat((List<Map<String, Object>>) response.get("playlists")).hasSize(limit);
        assertThat((int) response.get("total")).isGreaterThanOrEqualTo(limit);
    }

    @Test
    void getMockedPlaylistDetails_shouldReturnValidDetails() {
        // Arrange
        String playlistId = "mockPlaylistId001";

        // Act
        Map<String, Object> details = MockData.getMockedPlaylistDetails(playlistId);

        // Assert
        assertThat(details).isNotNull();
        assertThat(details).containsKey("id");
        assertThat(details.get("id")).isEqualTo(playlistId);
        assertThat(details).containsKey("playlistName");
        assertThat(details).containsKey("tracks");
        assertThat((Map<String, Object>) details.get("tracks")).containsKey("items");
        assertThat((List<Map<String, Object>>) ((Map<String, Object>) details.get("tracks")).get("items")).isNotEmpty();
    }

    @Test
    void getMockedRecommendations_shouldReturnValidRecommendations() {
        // Act
        List<Map<String, Object>> recommendations = MockData.getMockedRecommendations();

        // Assert
        assertThat(recommendations).isNotNull();
        assertThat(recommendations).hasSize(5);
        assertThat(recommendations.get(0)).containsKey("id");
        assertThat(recommendations.get(0)).containsKey("name");
    }

    @Test
    void getMockedFollowedPlaylists_shouldReturnValidPlaylists() {
        // Act
        List<Map<String, Object>> playlists = MockData.getMockedFollowedPlaylists();

        // Assert
        assertThat(playlists).isNotNull();
        assertThat(playlists).hasSize(5);
        assertThat(playlists.get(0)).containsKey("id");
        assertThat(playlists.get(0)).containsKey("name");
    }

    @Test
    void getMockedSessionCheckResponse_shouldReturnValidResponse() {
        // Act
        Map<String, Object> response = MockData.getMockedSessionCheckResponse();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response).containsEntry("status", "success");
        assertThat(response).containsKey("userId");
        assertThat(response).containsKey("userName");
    }

    @Test
    void getMockedLoginResponse_shouldReturnValidResponse() {
        // Act
        Map<String, Object> response = MockData.getMockedLoginResponse();

        // Assert
        assertThat(response).isNotNull();
        assertThat(response).containsEntry("status", "success");
        assertThat(response).containsKey("userId");
        assertThat(response).containsKey("userName");
    }
}
