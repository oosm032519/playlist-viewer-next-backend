package com.github.oosm032519.playlistviewernext.service.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.*;

@Service
public class TrackDataRetriever {

    private static final Logger logger = LoggerFactory.getLogger(TrackDataRetriever.class);

    @Autowired
    private SpotifyTrackService trackService;

    public List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) throws Exception {
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
}
