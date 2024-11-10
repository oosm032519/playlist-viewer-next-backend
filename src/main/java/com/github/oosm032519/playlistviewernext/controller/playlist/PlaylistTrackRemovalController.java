package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.model.PlaylistTrackRemovalRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackRemovalService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.util.Map;

/**
 * プレイリストからトラックを削除するためのコントローラークラス
 */
@RestController
@RequestMapping("/api/playlist")
@Validated
public class PlaylistTrackRemovalController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PlaylistTrackRemovalController.class);

    private final SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService;
    private final HttpServletRequest request; // リクエスト情報を取得

    public PlaylistTrackRemovalController(SpotifyPlaylistTrackRemovalService spotifyPlaylistTrackRemovalService,
                                          HttpServletRequest request) {
        this.spotifyPlaylistTrackRemovalService = spotifyPlaylistTrackRemovalService;
        this.request = request;
    }

    /**
     * プレイリストからトラックを削除するエンドポイント
     *
     * @param request   トラック削除リクエストの詳細を含むオブジェクト
     * @param principal 認証されたユーザー情報
     * @return トラック削除の結果を含むResponseEntity
     */
    @PostMapping("/remove-track")
    public ResponseEntity<?> removeTrackFromPlaylist(
            @Valid @RequestBody PlaylistTrackRemovalRequest request,
            @AuthenticationPrincipal OAuth2User principal) throws SpotifyWebApiException {
        LOGGER.info("removeTrackFromPlaylist メソッドが呼び出されました。リクエスト: {}", request);

        if (principal == null) {
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "認証されていないユーザーがアクセスしようとしました。"
            );
        }

        ResponseEntity<String> response = spotifyPlaylistTrackRemovalService.removeTrackFromPlaylist(request, principal);
        if (response.getStatusCode() == HttpStatus.OK) {
            return ResponseEntity.ok(Map.of("message", "トラックが正常に削除されました。"));
        } else {
            throw new InternalServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Spotify APIでトラックの削除中にエラーが発生しました。しばらく時間をおいてから再度お試しください。"
            );
        }
    }
}
