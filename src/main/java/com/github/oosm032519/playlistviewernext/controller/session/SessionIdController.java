package com.github.oosm032519.playlistviewernext.controller.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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
            logger.warn("一時トークンが提供されていません。リクエストボディ: {}", body);
            return ResponseEntity.badRequest().body("一時トークンが提供されていません");
        }

        logger.debug("Redisから一時トークンに対応するセッションIDを取得します。一時トークン: {}", temporaryToken);
        String sessionId = redisTemplate.opsForValue().get("temp:" + temporaryToken);

        if (sessionId == null) {
            logger.warn("セッションIDが見つかりません。一時トークン: {}", temporaryToken);
            return ResponseEntity.notFound().build();
        }

        logger.info("セッションIDが正常に取得されました。セッションID: {}", sessionId);

        logger.debug("Redisから一時トークンを削除します。一時トークン: {}", temporaryToken);
        Boolean deleteResult = redisTemplate.delete("temp:" + temporaryToken);
        logger.info("一時トークンの削除結果: {}", deleteResult);

        logger.info("セッションID取得処理が完了しました。セッションID: {}", sessionId);
        return ResponseEntity.ok(Map.of("sessionId", sessionId));
    }
}
