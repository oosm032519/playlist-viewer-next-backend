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

import java.util.HashMap;
import java.util.Map;

/**
 * アプリケーション全体で発生する例外を処理するグローバル例外ハンドラー
 */
@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final HttpServletRequest request;

    /**
     * コンストラクタ
     *
     * @param request HTTPサーブレットリクエスト
     */
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
        String errorCode = ex.getClass().getSimpleName();
        String message = ex.getMessage();
        String details = ex.getDetails();

        // 例外の種類に応じて詳細情報を追加
        if (ex instanceof ResourceNotFoundException) {
            errorCode = "RESOURCE_NOT_FOUND";
            details = (details != null) ? details : "リクエストされたリソースは存在しません。"; // 詳細情報がnullの場合はデフォルトメッセージを設定
        } else if (ex instanceof AuthenticationException) {
            errorCode = "AUTHENTICATION_ERROR";
            details = (details != null) ? details : "認証に失敗しました。";
        } else if (ex instanceof InvalidRequestException) {
            errorCode = "INVALID_REQUEST";
            details = (details != null) ? details : "無効なリクエストです。";
        } else if (ex instanceof DatabaseAccessException) {
            errorCode = "DATABASE_ACCESS_ERROR";
            details = (details != null) ? details : "データベースアクセスエラーが発生しました。";
        } else if (ex instanceof InternalServerException) {
            errorCode = "INTERNAL_SERVER_ERROR";
            details = (details != null) ? details : "内部サーバーエラーが発生しました。";
        }

        // リクエストパラメータを詳細情報に追加
        String requestParams = getRequestParams();
        details += (details != null ? " " : "") + "リクエストパラメータ: " + requestParams;

        ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * リクエストパラメータを取得するヘルパーメソッド
     */
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
     * ConstraintViolationException を処理するハンドラ
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
     * SpotifyWebApiException を処理するハンドラ
     *
     * @param ex 発生した SpotifyWebApiException
     * @return エラーレスポンスを含む ResponseEntity
     */
    @ExceptionHandler(SpotifyWebApiException.class)
    public ResponseEntity<ErrorResponse> handleSpotifyWebApiException(SpotifyWebApiException ex) {
        logger.error("SpotifyWebApiException が発生しました: {}", ex.getMessage(), ex);

        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = "SPOTIFY_API_ERROR";
        String message = "Spotify API エラーが発生しました。";
        String details = ex.getMessage();

        // リクエストパラメータを詳細情報に追加
        String requestParams = getRequestParams();
        details += " リクエストパラメータ: " + requestParams;

        ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);
        return new ResponseEntity<>(errorResponse, status);
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
        String errorCode = "UNEXPECTED_ERROR";
        String message = "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。";
        String details = ex.getMessage();

        // リクエストパラメータを詳細情報に追加
        String requestParams = getRequestParams();
        details += " リクエストパラメータ: " + requestParams;

        // スタックトレースをログに記録
        logger.error("スタックトレース:", ex);

        ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);
        return new ResponseEntity<>(errorResponse, status);
    }
}
