package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistDetailsRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsRetrievalService.class);

    @Autowired
    private SpotifyPlaylistDetailsService playlistDetailsService;
    @Autowired
    private SpotifyTrackService trackService;
    @Autowired
    private SpotifyClientCredentialsAuthentication authController;

    public Map<String, Object> getPlaylistDetails(String id) throws Exception {
        logger.info("getPlaylistDetails: プレイリストID: {}", id);
        authController.authenticate();

        PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);
        List<Map<String, Object>> trackList = getTrackListData(tracks);

        String playlistName = playlistDetailsService.getPlaylistName(id);
        User owner = playlistDetailsService.getPlaylistOwner(id);

        // ダンスアビリティの最大値を計算
        float maxDanceability = calculateMaxDanceability(trackList);
        logger.info("getPlaylistDetails: 最大ダンスアビリティ: {}", maxDanceability);

        Map<String, Object> response = new HashMap<>();
        response.put("tracks", Map.of("items", trackList));
        response.put("playlistName", playlistName);
        response.put("ownerId", owner.getId());
        response.put("ownerName", owner.getDisplayName());
        response.put("maxDanceability", maxDanceability); // 最大ダンスアビリティを追加
        return response;
    }

    private List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) throws Exception {
        logger.info("getTrackListData: トラック数: {}", tracks.length);
        List<Map<String, Object>> trackList = new ArrayList<>();
        for (PlaylistTrack track : tracks) {
            Map<String, Object> trackData = new HashMap<>();
            Track fullTrack = (Track) track.getTrack();
            trackData.put("track", fullTrack);

            String trackId = fullTrack.getId();
            AudioFeatures audioFeatures = trackService.getAudioFeaturesForTrack(trackId);
            trackData.put("audioFeatures", audioFeatures);
            trackList.add(trackData);
        }
        logger.info("getTrackListData: トラックデータリスト作成完了");
        return trackList;
    }

    private float calculateMaxDanceability(List<Map<String, Object>> trackList) {
        logger.info("calculateMaxDanceability: 計算開始");
        float maxDanceability = 0.0f;
        for (Map<String, Object> trackData : trackList) {
            AudioFeatures audioFeatures = (AudioFeatures) trackData.get("audioFeatures");
            if (audioFeatures != null && audioFeatures.getDanceability() > maxDanceability) {
                maxDanceability = audioFeatures.getDanceability();
            }
        }
        logger.info("calculateMaxDanceability: 最大ダンスアビリティ計算完了: {}", maxDanceability);
        return maxDanceability;
    }
}
