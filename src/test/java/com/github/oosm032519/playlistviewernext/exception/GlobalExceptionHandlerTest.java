package com.github.oosm032519.playlistviewernext.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handlePlaylistViewerNextException_shouldReturnErrorResponse() {
        // Arrange
        PlaylistViewerNextException exception = new PlaylistViewerNextException(HttpStatus.NOT_FOUND, "Not Found", "NOT_FOUND_ERROR", "Details");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePlaylistViewerNextException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("NOT_FOUND_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Not Found");
        assertThat(response.getBody().getDetails()).contains("Details");
    }

    @Test
    void handleConstraintViolationException_shouldReturnBadRequest() {
        // Arrange
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", new HashSet<>());
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConstraintViolationException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("入力値が不正です。");
    }

    @Test
    void handleSpotifyWebApiException_shouldReturnInternalServerError() {
        // Arrange
        SpotifyWebApiException exception = new SpotifyWebApiException("Spotify API Error");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleSpotifyWebApiException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("SPOTIFY_API_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Spotify API エラーが発生しました。");
        assertThat(response.getBody().getDetails()).contains("Spotify API Error");
    }

    @Test
    void handleUnexpectedException_shouldReturnInternalServerError() {
        // Arrange
        Exception exception = new Exception("Unexpected error");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(exception);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("UNEXPECTED_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。");
        assertThat(response.getBody().getDetails()).contains("Unexpected error");
    }
}
