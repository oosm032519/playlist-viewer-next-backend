package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.exception.ErrorResponse;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
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
import static org.mockito.Mockito.*;

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
        List<PlaylistSimplified> expectedPlaylists = createMockPlaylists();

        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenReturn(expectedPlaylists);

        // Act
        ResponseEntity<?> response = searchController.searchPlaylists(query, offset, limit);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(expectedPlaylists);
        verify(playlistSearchService).searchPlaylists(query, offset, limit);
        verify(authController).authenticate();
    }

    @Test
    void givenServiceThrowsException_whenSearchPlaylists_thenReturnsErrorResponse() throws Exception {
        // Arrange
        String query = "test query";
        int offset = 0;
        int limit = 20;

        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenThrow(new RuntimeException("API error"));
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        // Act
        ResponseEntity<?> responseEntity = searchController.searchPlaylists(query, offset, limit);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
        assertThat(errorResponse.getErrorCode()).isEqualTo("SYSTEM_UNEXPECTED_ERROR");
        assertThat(errorResponse.getMessage()).isEqualTo("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。");

        verify(playlistSearchService).searchPlaylists(query, offset, limit);
        verify(authController).authenticate();
    }

    @Test
    void givenSpotifyApiException_whenSearchPlaylists_thenReturnsErrorResponse() throws Exception {
        // Arrange
        String query = "test query";
        int offset = 0;
        int limit = 20;

        SpotifyApiException spotifyApiException = new SpotifyApiException(HttpStatus.BAD_REQUEST, "SPOTIFY_API_ERROR", "Spotify API error", "Error details");
        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenThrow(spotifyApiException);

        // Act
        ResponseEntity<?> responseEntity = searchController.searchPlaylists(query, offset, limit);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
        assertThat(errorResponse.getErrorCode()).isEqualTo("SPOTIFY_API_ERROR");
        assertThat(errorResponse.getMessage()).isEqualTo("Spotify API error");
        assertThat(errorResponse.getDetails()).isEqualTo("Error details");

        verify(playlistSearchService).searchPlaylists(query, offset, limit);
        verify(authController).authenticate();
    }

    @Test
    void givenRateLimitException_whenSearchPlaylists_thenRetriesAndReturnsErrorResponse() throws Exception {
        // Arrange
        String query = "test query";
        int offset = 0;
        int limit = 20;

        SpotifyApiException rateLimitException = new SpotifyApiException(HttpStatus.TOO_MANY_REQUESTS, "SPOTIFY_API_RATE_LIMIT_EXCEEDED", "Rate limit exceeded", "Error details");
        when(playlistSearchService.searchPlaylists(query, offset, limit)).thenThrow(rateLimitException);

        // Act
        ResponseEntity<?> responseEntity = searchController.searchPlaylists(query, offset, limit);

        // Assert
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        ErrorResponse errorResponse = (ErrorResponse) responseEntity.getBody();
        assertThat(errorResponse.getErrorCode()).isEqualTo("SPOTIFY_API_RATE_LIMIT_EXCEEDED");
        assertThat(errorResponse.getMessage()).isEqualTo("Spotify API のレート制限を超過しました。しばらく時間をおいてから再度お試しください。");

        verify(playlistSearchService, times(4)).searchPlaylists(query, offset, limit);
        verify(authController, times(4)).authenticate();
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
