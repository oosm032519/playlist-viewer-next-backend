package com.github.oosm032519.playlistviewernext.controller.session;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/session")
public class SessionIdController {

    private static final Logger logger = LoggerFactory.getLogger(SessionIdController.class);

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @PostMapping("/sessionId")
    public ResponseEntity<?> getSessionId(@RequestBody Map<String, String> body) {
        logger.info("セッションID取得処理を開始します。リクエストボディ: {}", body);

        String temporaryToken = body.get("temporaryToken");
        if (temporaryToken == null) {
            // 一時トークンがない場合は PlaylistViewerNextException をスロー
            logger.warn("一時トークンが提供されていません。リクエストボディ: {}", body);
            throw new PlaylistViewerNextException(
                    HttpStatus.BAD_REQUEST,
                    "TEMPORARY_TOKEN_MISSING",
                    "一時トークンが提供されていません。"
            );
        }

        try {
            logger.debug("Redisから一時トークンに対応するセッションIDを取得します。一時トークン: {}", temporaryToken);
            String sessionId = redisTemplate.opsForValue().get("temp:" + temporaryToken);

            if (sessionId == null) {
                // セッションIDが見つからない場合は PlaylistViewerNextException をスロー
                logger.warn("セッションIDが見つかりません。一時トークン: {}", temporaryToken);
                throw new PlaylistViewerNextException(
                        HttpStatus.NOT_FOUND,
                        "SESSION_ID_NOT_FOUND",
                        "セッションIDが見つかりません。"
                );
            }

            logger.info("セッションIDが正常に取得されました。セッションID: {}", sessionId);

            logger.debug("Redisから一時トークンを削除します。一時トークン: {}", temporaryToken);
            Boolean deleteResult = redisTemplate.delete("temp:" + temporaryToken);
            logger.info("一時トークンの削除結果: {}", deleteResult);

            logger.info("セッションID取得処理が完了しました。セッションID: {}", sessionId);
            return ResponseEntity.ok(Map.of("sessionId", sessionId));
        } catch (Exception e) {
            // セッションID取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("セッションIDの取得中にエラーが発生しました。", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "SESSION_ID_RETRIEVAL_ERROR",
                    "セッションIDの取得中にエラーが発生しました。",
                    e
            );
        }
    }
}
