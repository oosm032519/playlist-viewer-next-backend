package com.github.oosm032519.playlistviewernext.exception;

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

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * PlaylistViewerNextException を処理するハンドラー
     *
     * @param ex      発生した PlaylistViewerNextException
     * @param request リクエスト情報
     * @return エラーレスポンスを含む ResponseEntity
     */
    @ExceptionHandler(PlaylistViewerNextException.class)
    public ResponseEntity<ErrorResponse> handlePlaylistViewerNextException(PlaylistViewerNextException ex, WebRequest request) {
        logger.error("PlaylistViewerNextException が発生しました: {}", ex.getMessage(), ex);

        HttpStatus status = ex.getHttpStatus();
        String errorCode = ex.getErrorCode();
        String message = ex.getMessage();
        String details = ex.getDetails();

        // 例外の種類に応じて詳細情報を追加
        switch (ex) {
            case ResourceNotFoundException resourceNotFoundException ->
                    details = "リクエストされたリソースは存在しません。";
            case AuthenticationException authenticationException -> details = "認証に失敗しました。";
            case SpotifyApiException spotifyApiException when ex.getErrorCode().equals("SPOTIFY_API_RATE_LIMIT_EXCEEDED") -> {
                // レート制限を超過した場合の処理
                message = "Spotify API のレート制限を超過しました。しばらく時間をおいてから再度お試しください。";
                details = "リクエストが頻繁に行われています。しばらく時間をおいてから再度お試しください。";
            }
            default -> {
            }
        }

        ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * バリデーションエラーを処理するハンドラー
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
     * その他の例外を処理するハンドラー
     *
     * @param ex      発生した Exception
     * @param request リクエスト情報
     * @return エラーレスポンスを含む ResponseEntity
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, WebRequest request) {
        logger.error("予期しないエラーが発生しました: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR", "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
