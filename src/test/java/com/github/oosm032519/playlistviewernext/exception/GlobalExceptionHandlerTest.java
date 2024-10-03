package com.github.oosm032519.playlistviewernext.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private WebRequest mockWebRequest;

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        exceptionHandler = new GlobalExceptionHandler(mockRequest);
    }

    @Test
    void handlePlaylistViewerNextException_ResourceNotFoundException() {
        ResourceNotFoundException ex = new ResourceNotFoundException(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", "Resource not found");
        when(mockRequest.getParameterMap()).thenReturn(Collections.emptyMap());

        ResponseEntity<ErrorResponse> response = exceptionHandler.handlePlaylistViewerNextException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Resource not found");
        assertThat(response.getBody().getDetails()).contains("リクエストされたリソースは存在しません。");
    }

    @Test
    void handlePlaylistViewerNextException_AuthenticationException() {
        AuthenticationException ex = new AuthenticationException(HttpStatus.UNAUTHORIZED, "AUTH_FAILED", "Authentication failed");
        when(mockRequest.getParameterMap()).thenReturn(Collections.emptyMap());

        ResponseEntity<ErrorResponse> response = exceptionHandler.handlePlaylistViewerNextException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Authentication failed");
        assertThat(response.getBody().getDetails()).contains("認証に失敗しました。");
    }

    @Test
    void handlePlaylistViewerNextException_OtherException() {
        PlaylistViewerNextException ex = new PlaylistViewerNextException(HttpStatus.BAD_REQUEST, "Other error", "Other details");
        Map<String, String[]> paramMap = new HashMap<>();
        paramMap.put("param1", new String[]{"value1"});
        when(mockRequest.getParameterMap()).thenReturn(paramMap);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handlePlaylistViewerNextException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Other error");
        assertThat(response.getBody().getDetails()).contains("Other details");
        assertThat(response.getBody().getDetails()).contains("param1=value1");
    }

    @Test
    void handleGenericException() {
        Exception ex = new Exception("Unexpected error");
        when(mockRequest.getParameterMap()).thenReturn(Collections.emptyMap());

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。");
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
    }

    @Test
    void handleMethodArgumentNotValid() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "field", "defaultMessage");
        when(bindingResult.getAllErrors()).thenReturn(Collections.singletonList(fieldError));
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(mockRequest.getParameterMap()).thenReturn(Collections.emptyMap());

        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mockWebRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertThat(errorResponse.getMessage()).isEqualTo("入力値が不正です。");
        assertThat(errorResponse.getDetails()).contains("field=defaultMessage");
    }

    @Test
    void handleMethodArgumentNotValid_MultipleFieldErrors() {
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError1 = new FieldError("object", "field1", "error1");
        FieldError fieldError2 = new FieldError("object", "field2", "error2");
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));
        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(mockRequest.getParameterMap()).thenReturn(Collections.emptyMap());

        ResponseEntity<Object> response = exceptionHandler.handleMethodArgumentNotValid(ex, new HttpHeaders(), HttpStatus.BAD_REQUEST, mockWebRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(ErrorResponse.class);
        ErrorResponse errorResponse = (ErrorResponse) response.getBody();
        assertThat(errorResponse.getMessage()).isEqualTo("入力値が不正です。");
        assertThat(errorResponse.getDetails()).contains("field1=error1");
        assertThat(errorResponse.getDetails()).contains("field2=error2");
    }

    @Test
    void handleGenericException_WithRequestParams() {
        Exception ex = new Exception("Unexpected error");
        Map<String, String[]> paramMap = new HashMap<>();
        paramMap.put("param1", new String[]{"value1"});
        paramMap.put("param2", new String[]{"value2"});
        when(mockRequest.getParameterMap()).thenReturn(paramMap);

        ResponseEntity<ErrorResponse> response = exceptionHandler.handleGenericException(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。");
        assertThat(response.getBody().getErrorCode()).isEqualTo("INTERNAL_SERVER_ERROR");
        assertThat(response.getBody().getDetails()).contains("param1=value1");
        assertThat(response.getBody().getDetails()).contains("param2=value2");
    }

    @Test
    void handlePlaylistViewerNextException_WithNullDetails() {
        // Arrange
        PlaylistViewerNextException ex = new PlaylistViewerNextException(HttpStatus.BAD_REQUEST, "Test error", (String) null);
        Map<String, String[]> paramMap = new HashMap<>();
        paramMap.put("param1", new String[]{"value1"});
        when(mockRequest.getParameterMap()).thenReturn(paramMap);

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handlePlaylistViewerNextException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Test error");
        assertThat(response.getBody().getDetails()).isEqualTo("リクエストパラメータ: param1=value1");
    }

    @Test
    void handlePlaylistViewerNextException_SpotifyApiRateLimitException() {
        // Arrange
        SpotifyApiException ex = new SpotifyApiException(HttpStatus.TOO_MANY_REQUESTS, "SPOTIFY_API_RATE_LIMIT_EXCEEDED", "Rate limit exceeded", "Original details");
        when(mockRequest.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handlePlaylistViewerNextException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Spotify API のレート制限を超過しました。しばらく時間をおいてから再度お試しください。");
        assertThat(response.getBody().getDetails()).isEqualTo("リクエストが頻繁に行われています。しばらく時間をおいてから再度お試しください。 リクエストパラメータ: ");
    }

    @Test
    void handlePlaylistViewerNextException_SpotifyApiExceptionWithDifferentErrorCode() {
        // Arrange
        SpotifyApiException ex = new SpotifyApiException(HttpStatus.BAD_REQUEST, "OTHER_SPOTIFY_ERROR", "Other Spotify error", "Original details");
        when(mockRequest.getParameterMap()).thenReturn(Collections.emptyMap());

        // Act
        ResponseEntity<ErrorResponse> response = exceptionHandler.handlePlaylistViewerNextException(ex);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Other Spotify error");
        assertThat(response.getBody().getDetails()).isEqualTo("Original details リクエストパラメータ: ");
    }
}
