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
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
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

    /**
     * PlaylistViewerNextExceptionが発生した場合に、適切なエラーレスポンスが返されることを確認する。
     */
    @Test
    void handlePlaylistViewerNextException_shouldReturnErrorResponse() {
        // Arrange: PlaylistViewerNextExceptionを発生させる
        PlaylistViewerNextException exception = new PlaylistViewerNextException(HttpStatus.NOT_FOUND, "Not Found", "NOT_FOUND_ERROR", "Details");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act: 例外ハンドラを呼び出す
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePlaylistViewerNextException(exception);

        // Assert: レスポンスの内容を検証する
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("NOT_FOUND_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Not Found");
        assertThat(response.getBody().getDetails()).contains("Details");
    }

    /**
     * PlaylistViewerNextExceptionが発生し、詳細情報がnullの場合に、リクエストパラメータを含むエラーレスポンスが返されることを確認する。
     */
    @Test
    void handlePlaylistViewerNextException_withNullDetails_shouldReturnErrorResponseWithRequestParams() {
        // Arrange: PlaylistViewerNextExceptionを発生させる（詳細情報はnull）
        PlaylistViewerNextException exception = new PlaylistViewerNextException(HttpStatus.NOT_FOUND, "Not Found", "NOT_FOUND_ERROR");

        // ServletRequestAttributesとRequestContextHolderをモックして、リクエストパラメータを提供する
        ServletRequestAttributes attributes = mock(ServletRequestAttributes.class);
        when(attributes.getRequest()).thenReturn(request);
        RequestContextHolder.setRequestAttributes(attributes);

        when(request.getParameterMap()).thenReturn(Collections.singletonMap("param", new String[]{"value"}));

        // Act: 例外ハンドラを呼び出す
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePlaylistViewerNextException(exception);

        // Assert: レスポンスの内容を検証する（詳細情報にリクエストパラメータが含まれる）
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("NOT_FOUND_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Not Found");
        assertThat(response.getBody().getDetails()).contains("リクエストパラメータ: param=value");
    }

    /**
     * ConstraintViolationExceptionが発生した場合に、BadRequestエラーレスポンスが返されることを確認する。
     */
    @Test
    void handleConstraintViolationException_shouldReturnBadRequest() {
        // Arrange: ConstraintViolationExceptionを発生させる
        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", new HashSet<>());
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act: 例外ハンドラを呼び出す
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleConstraintViolationException(exception);

        // Assert: レスポンスの内容を検証する
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("VALIDATION_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("入力値が不正です。");
    }

    /**
     * SpotifyWebApiExceptionが発生した場合に、InternalServerErrorエラーレスポンスが返されることを確認する。
     */
    @Test
    void handleSpotifyWebApiException_shouldReturnInternalServerError() {
        // Arrange: SpotifyWebApiExceptionを発生させる
        SpotifyWebApiException exception = new SpotifyWebApiException("Spotify API Error");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act: 例外ハンドラを呼び出す
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleSpotifyWebApiException(exception);

        // Assert: レスポンスの内容を検証する
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("SPOTIFY_API_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Spotify API エラーが発生しました。");
        assertThat(response.getBody().getDetails()).contains("Spotify API Error");
    }

    /**
     * 予期しない例外が発生した場合に、InternalServerErrorエラーレスポンスが返されることを確認する。
     */
    @Test
    void handleUnexpectedException_shouldReturnInternalServerError() {
        // Arrange: 予期しない例外を発生させる
        Exception exception = new Exception("Unexpected error");
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act: 例外ハンドラを呼び出す
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handleException(exception);

        // Assert: レスポンスの内容を検証する
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("UNEXPECTED_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。");
        assertThat(response.getBody().getDetails()).contains("Unexpected error");
    }

    /**
     * getRequestParamsメソッドで例外が発生した場合に、空文字列が返されることを確認する。
     */
    @Test
    void getRequestParams_shouldReturnEmptyString_whenExceptionOccurs() {
        // Arrange: RequestContextHolderをクリアして影響を受けないようにする
        RequestContextHolder.resetRequestAttributes();

        // `request.getParameterMap()` が例外をスローするようにモック
        when(request.getParameterMap()).thenThrow(new RuntimeException("Request error"));

        // Act: getRequestParamsメソッドを呼び出す
        String requestParams = globalExceptionHandler.getRequestParams();

        // Assert: 空文字列が返されることを確認する
        assertThat(requestParams).isEmpty();
    }

    /**
     * PlaylistViewerNextExceptionの他のコンストラクタで例外が発生した場合に、適切なエラーレスポンスが返されることを確認する。
     */
    @Test
    void handlePlaylistViewerNextException_withCause_shouldReturnErrorResponse() {
        // Arrange: 原因となる例外を含むPlaylistViewerNextExceptionを発生させる
        Throwable cause = new Exception("Cause message");
        PlaylistViewerNextException exception = new PlaylistViewerNextException(HttpStatus.INTERNAL_SERVER_ERROR, "Error with cause", "CAUSE_ERROR", cause);
        when(request.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act: 例外ハンドラを呼び出す
        ResponseEntity<ErrorResponse> response = globalExceptionHandler.handlePlaylistViewerNextException(exception);

        // Assert: レスポンスの内容を検証する
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(Objects.requireNonNull(response.getBody()).getErrorCode()).isEqualTo("CAUSE_ERROR");
        assertThat(response.getBody().getMessage()).isEqualTo("Error with cause");
        assertThat(response.getBody().getDetails()).isEqualTo("Cause message");
    }
}
