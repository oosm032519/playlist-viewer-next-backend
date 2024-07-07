package com.github.oosm032519.playlistviewernext.service;

import com.github.oosm032519.playlistviewernext.controller.PlaylistAuthController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistService {

    @Autowired
    private SpotifyPlaylistDetailsService playlistDetailsService;
    @Autowired
    private SpotifyTrackService trackService;
    @Autowired
    private PlaylistAuthController authController;

    public Map<String, Object> getPlaylistDetails(String id) throws Exception {
        authController.authenticate();

        PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);
        List<Map<String, Object>> trackList = getTrackListData(tracks);

        String playlistName = playlistDetailsService.getPlaylistName(id);
        User owner = playlistDetailsService.getPlaylistOwner(id);

        Map<String, Object> response = new HashMap<>();
        response.put("tracks", Map.of("items", trackList));
        response.put("playlistName", playlistName);
        response.put("ownerId", owner.getId());
        response.put("ownerName", owner.getDisplayName());
        return response;
    }

    private List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) throws Exception {
        List<Map<String, Object>> trackList = new ArrayList<>();
        for (PlaylistTrack track : tracks) {
            Map<String, Object> trackData = new HashMap<>();
            Track fullTrack = (Track) track.getTrack();
            trackData.put("track", fullTrack);

            String trackId = fullTrack.getId();
            trackData.put("audioFeatures", trackService.getAudioFeaturesForTrack(trackId));
            trackList.add(trackData);
        }
        return trackList;
    }
}
