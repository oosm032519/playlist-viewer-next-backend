package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequestMapping("/api/playlists/details")
public class PlaylistDetailsController {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsController.class);

    private final GetPlaylistDetails getPlaylistDetails;

    @Autowired
    public PlaylistDetailsController(
            GetPlaylistDetails getPlaylistDetails
    ) {
        this.getPlaylistDetails = getPlaylistDetails;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPlaylistById(@PathVariable String id) {
        logger.info("PlaylistDetailsController: getPlaylistById メソッドが呼び出されました。プレイリストID: {}", id);
        try {
            Map<String, Object> response = getPlaylistDetails.getPlaylistDetails(id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("PlaylistDetailsController: プレイリストの取得中にエラーが発生しました", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }
}