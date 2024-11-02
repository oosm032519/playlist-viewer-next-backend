package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistSearchService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaylistSearchControllerTest {

    @Mock
    private SpotifyPlaylistSearchService playlistSearchService;

    @Mock
    private SpotifyClientCredentialsAuthentication authController;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private PlaylistSearchController searchController;

    @BeforeEach
    void setUp() {
        // 各テストメソッドの前に実行される設定
    }

    @Test
    void givenValidQuery_whenSearchPlaylists_thenReturnsPlaylistsSuccessfully() throws Exception {
        // Arrange
        String query = "test query";
        int offset = 0;
        int limit = 20;
        Map<String, Object> expectedResult = new HashMap<>();
        List<PlaylistSimplified> expectedPlaylists = createMockPlaylists();
        expectedResult.put("playlists", expectedPlaylists);
        expectedResult.put("total", expectedPlaylists.size());


        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenReturn(expectedResult);

        // Act
        ResponseEntity<?> response = searchController.searchPlaylists(query, offset, limit);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedResult);
        verify(playlistSearchService).searchPlaylists(query, offset, limit);
        verify(authController).authenticate();
    }

    @Test
    void givenRequestWithParameters_whenGetRequestParams_thenReturnsFormattedString() {
        // Arrange
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("query", new String[]{"test"});
        parameterMap.put("offset", new String[]{"0"});
        parameterMap.put("limit", new String[]{"20"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        // Act
        String result = searchController.getRequestParams();

        // Assert
        assertThat(result).contains("query=test", "offset=0", "limit=20");
    }

    @Test
    void givenEmptyRequestParameters_whenGetRequestParams_thenReturnsEmptyString() {
        // Arrange
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        // Act
        String result = searchController.getRequestParams();

        // Assert
        assertThat(result).isEmpty();
    }

    private List<PlaylistSimplified> createMockPlaylists() {
        return Arrays.asList(
                new PlaylistSimplified.Builder().setName("Playlist 1").build(),
                new PlaylistSimplified.Builder().setName("Playlist 2").build()
        );
    }
}
