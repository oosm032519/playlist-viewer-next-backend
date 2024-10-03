package com.github.oosm032519.playlistviewernext.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.exceptions.detailed.TooManyRequestsException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final HttpServletRequest request;

    public GlobalExceptionHandler(HttpServletRequest request) {
        this.request = request;
    }

    /**
     * PlaylistViewerNextException を処理するハンドラ
     *
     * @param ex 発生した PlaylistViewerNextException
     * @return エラーレスポンスを含む ResponseEntity
     */
    @ExceptionHandler(PlaylistViewerNextException.class)
    public ResponseEntity<ErrorResponse> handlePlaylistViewerNextException(PlaylistViewerNextException ex) {
        logger.error("PlaylistViewerNextException が発生しました: {} - 詳細: {}", ex.getMessage(), ex.getDetails(), ex);

        HttpStatus status = ex.getHttpStatus();
        String message = ex.getMessage();
        String details = ex.getDetails();

        // 例外の種類に応じて詳細情報を追加
        if (ex instanceof ResourceNotFoundException) {
            details = "リクエストされたリソースは存在しません。";
        } else if (ex instanceof AuthenticationException) {
            details = "認証に失敗しました。";
        }

        // リクエストパラメータを詳細情報に追加
        String requestParams = getRequestParams();
        if (details == null) {
            details = "リクエストパラメータ: " + requestParams;
        } else {
            details += " リクエストパラメータ: " + requestParams;
        }

        ErrorResponse errorResponse = new ErrorResponse(status, message, details);
        return new ResponseEntity<>(errorResponse, status);
    }

    // リクエストパラメータを取得するヘルパーメソッド
    private String getRequestParams() {
        StringBuilder params = new StringBuilder();
        request.getParameterMap().forEach((key, values) -> params.append(key).append("=").append(String.join(",", values)).append("&"));
        if (!params.isEmpty()) {
            params.deleteCharAt(params.length() - 1);
        }
        return params.toString();
    }

    /**
     * バリデーションエラーを処理するハンドラ
     *
     * @param ex      発生した MethodArgumentNotValidException
     * @param headers HTTPヘッダー
     * @param status  HTTPステータス
     * @param request リクエスト情報
     * @return エラーレスポンスを含む ResponseEntity
     */
    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {
        logger.warn("バリデーションエラーが発生しました: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "入力値が不正です。", errors.toString());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * SpotifyWebApiException を処理するハンドラ
     *
     * @param ex 発生した SpotifyWebApiException
     * @return エラーレスポンスを含む ResponseEntity
     */
    @ExceptionHandler(SpotifyWebApiException.class)
    public ResponseEntity<ErrorResponse> handleSpotifyWebApiException(SpotifyWebApiException ex) {
        logger.error("SpotifyWebApiException が発生しました: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR; // デフォルトのステータスコード
        if (ex instanceof final TooManyRequestsException tooManyRequestsEx) {
            status = HttpStatus.TOO_MANY_REQUESTS; // 429 ステータスコード

            // Retry-After の値を取得してヘッダーに追加
            int retryAfter = tooManyRequestsEx.getRetryAfter();
            HttpHeaders headers = new HttpHeaders();
            headers.add("Retry-After", String.valueOf(retryAfter));

            ErrorResponse errorResponse = new ErrorResponse(status, ex.getMessage(), "リクエストが多すぎます。しばらくしてから再度お試しください。");
            return new ResponseEntity<>(errorResponse, headers, status);
        }

        ErrorResponse errorResponse = new ErrorResponse(status, ex.getMessage(), "Spotify API エラーが発生しました。");
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * ConstraintViolationException を処理するハンドラ
     *
     * @param ex 発生した ConstraintViolationException
     * @return エラーレスポンスを含む ResponseEntity
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException ex) {
        logger.warn("バリデーションエラーが発生しました: {}", ex.getMessage());

        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(violation -> {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            errors.put(fieldName, errorMessage);
        });

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "入力値が不正です。", errors.toString());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * 全ての例外を処理するハンドラ
     *
     * @param ex 発生した Exception
     * @return エラーレスポンスを含む ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception ex) {
        logger.error("予期しないエラーが発生しました: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String message = "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。";
        String details = ex.getMessage(); // 例外メッセージを詳細情報に追加

        // 例外の種類に応じてステータスコードとメッセージを変更
        if (ex instanceof SpotifyWebApiException) {
            status = HttpStatus.BAD_REQUEST;
            message = "Spotify API エラーが発生しました。";
        } else if (ex instanceof ResourceNotFoundException) {
            status = HttpStatus.NOT_FOUND;
            message = "リクエストされたリソースは存在しません。";
        } else if (ex instanceof AuthenticationException) {
            status = HttpStatus.UNAUTHORIZED;
            message = "認証に失敗しました。";
        }

        // リクエストパラメータを詳細情報に追加
        String requestParams = getRequestParams();
        details += " リクエストパラメータ: " + requestParams;

        ErrorResponse errorResponse = new ErrorResponse(status, message, details);
        return new ResponseEntity<>(errorResponse, status);
    }
}
