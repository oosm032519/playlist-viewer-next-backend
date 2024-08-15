package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
import com.github.oosm032519.playlistviewernext.exception.ErrorResponse;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/session")
@Validated
public class SessionIdController {

    private static final Logger logger = LoggerFactory.getLogger(SessionIdController.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/sessionId")
    public ResponseEntity<?> getSessionId(@Valid @RequestBody Map<String, @NotBlank String> body) {
        logger.info("セッションID取得処理を開始します。リクエストボディ: {}", body);

        String temporaryToken = body.get("temporaryToken");
        if (temporaryToken == null) {
            // 一時トークンがない場合は InvalidRequestException をスロー
            logger.warn("一時トークンが提供されていません。リクエストボディ: {}", body);
            throw new InvalidRequestException(
                    HttpStatus.BAD_REQUEST,
                    "TEMPORARY_TOKEN_MISSING",
                    "ログイン処理中にエラーが発生しました。再度ログインしてください。"
            );
        }

        try {
            logger.debug("Redisから一時トークンに対応するセッションIDを取得します。一時トークン: {}", temporaryToken);
            String sessionId = redisTemplate.opsForValue().get("temp:" + temporaryToken);

            if (sessionId == null) {
                // セッションIDが見つからない場合は DatabaseAccessException をスロー
                logger.warn("セッションIDが見つかりません。一時トークン: {}", temporaryToken);
                throw new DatabaseAccessException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "SESSION_ID_NOT_FOUND",
                        "ログイン処理中にエラーが発生しました。再度ログインしてください。",
                        null // DatabaseAccessException の原因はここでは特定できないため null を設定
                );
            }

            logger.info("セッションIDが正常に取得されました。セッションID: {}", sessionId);

            logger.debug("Redisから一時トークンを削除します。一時トークン: {}", temporaryToken);
            Boolean deleteResult = redisTemplate.delete("temp:" + temporaryToken);
            logger.info("一時トークンの削除結果: {}", deleteResult);

            logger.info("セッションID取得処理が完了しました。セッションID: {}", sessionId);
            return ResponseEntity.ok(Map.of("sessionId", sessionId));
        } catch (DatabaseAccessException e) {
            // DatabaseAccessException はそのまま再スロー
            HttpStatus status = e.getHttpStatus();
            String errorCode = e.getErrorCode();
            String message = e.getMessage();
            String details = e.getDetails();

            // エラーログに記録
            logger.error("Database access error occurred while retrieving session ID: {} - {} - {}", status, errorCode, message, e);

            // エラーレスポンスを返す
            ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);
            return new ResponseEntity<>(errorResponse, status);
        } catch (Exception e) {
            // Redisアクセス中に予期しないエラーが発生した場合は DatabaseAccessException をスロー
            logger.error("Redisアクセス中に予期しないエラーが発生しました。", e);
            throw new DatabaseAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "REDIS_ACCESS_ERROR",
                    "ログイン処理中にエラーが発生しました。再度ログインしてください。",
                    e
            );
        }
    }
}
