package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.SpotifyUserPlaylistsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/playlists/followed")
public class UserPlaylistsController {

    private static final Logger logger = LoggerFactory.getLogger(UserPlaylistsController.class);

    @Autowired
    private final SpotifyUserPlaylistsService userPlaylistsService;

    @Autowired
    public UserPlaylistsController(SpotifyUserPlaylistsService userPlaylistsService) {
        this.userPlaylistsService = userPlaylistsService;
    }

    @GetMapping
    public ResponseEntity<?> getFollowedPlaylists(OAuth2AuthenticationToken authentication) {
        try {
            return ResponseEntity.ok(userPlaylistsService.getCurrentUsersPlaylists(authentication));
        } catch (Exception e) {
            logger.error("UserPlaylistsController: フォロー中のプレイリストの取得中にエラーが発生しました", e);
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}
