package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.exception.DatabaseAccessException;
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

/**
 * セッションIDを管理するコントローラークラス
 * 一時トークンを使用してセッションIDを取得し、Redisから削除する機能を提供する
 */
@RestController
@RequestMapping("/api/session")
@Validated
public class SessionIdController {

    private static final Logger logger = LoggerFactory.getLogger(SessionIdController.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * 一時トークンを使用してセッションIDを取得し、Redisから削除するエンドポイント
     *
     * @param body リクエストボディ。"temporaryToken"キーを含むMap
     * @return セッションIDを含むResponseEntity
     * @throws InvalidRequestException 一時トークンが提供されていない場合
     * @throws DatabaseAccessException Redisアクセス中にエラーが発生した場合
     */
    @PostMapping("/sessionId")
    public ResponseEntity<?> getSessionId(@Valid @RequestBody Map<String, @NotBlank String> body) {
        logger.info("セッションID取得処理を開始します。リクエストボディ: {}", body);

        String temporaryToken = body.get("temporaryToken");
        if (temporaryToken == null) {
            logger.warn("一時トークンが提供されていません。リクエストボディ: {}", body);
            throw new InvalidRequestException(
                    HttpStatus.BAD_REQUEST,
                    "ログイン処理中にエラーが発生しました。再度ログインしてください。"
            );
        }

        logger.debug("Redisから一時トークンに対応するセッションIDを取得します。一時トークン: {}", temporaryToken);
        String sessionId = redisTemplate.opsForValue().get("temp:" + temporaryToken);

        if (sessionId == null) {
            logger.warn("セッションIDが見つかりません。一時トークン: {}", temporaryToken);
            throw new DatabaseAccessException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "ログイン処理中にエラーが発生しました。再度ログインしてください。",
                    null
            );
        }

        logger.info("セッションIDが正常に取得されました。セッションID: {}", sessionId);

        logger.debug("Redisから一時トークンを削除します。一時トークン: {}", temporaryToken);
        Boolean deleteResult = redisTemplate.delete("temp:" + temporaryToken);
        logger.info("一時トークンの削除結果: {}", deleteResult);

        logger.info("セッションID取得処理が完了しました。セッションID: {}", sessionId);
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }
}
