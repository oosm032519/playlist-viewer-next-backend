package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackRemovalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlist")
public class PlaylistTrackRemovalController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistTrackRemovalController.class);

    @Autowired
    private SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;

    @PostMapping("/remove-track")
    public ResponseEntity<String> removeTrackFromPlaylist(
            @RequestBody PlaylistTrackRemovalRequest request,
            @AuthenticationPrincipal OAuth2User principal) {
        logger.info("removeTrackFromPlaylist メソッドが呼び出されました。リクエスト: {}", request);

        if (principal == null) {
            logger.warn("認証されていないユーザーがアクセスしようとしました。");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"認証が必要です。\"}");
        }

        boolean success = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(request, principal).hasBody();
        if (success) {
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"message\": \"トラックが正常に削除されました。\"}");
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"error\": \"トラックの削除に失敗しました。\"}");
        }
    }
}
