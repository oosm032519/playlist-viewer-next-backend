package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.service.SpotifyService;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/playlists")
public class PlaylistController {

    @Autowired
    private SpotifyService spotifyService;

    @GetMapping("/search")
    public ResponseEntity<List<PlaylistSimplified>> searchPlaylists(@RequestParam String query) throws ParseException {
        try {
            spotifyService.getAccessToken();
            List<PlaylistSimplified> playlists = spotifyService.searchPlaylists(query);
            return ResponseEntity.ok(playlists);
        } catch (IOException | SpotifyWebApiException e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
