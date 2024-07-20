package com.github.oosm032519.playlistviewernext.service.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class TrackDataRetriever {

    public static Logger logger = LoggerFactory.getLogger(TrackDataRetriever.class);

    private final SpotifyTrackService trackService;

    public TrackDataRetriever(SpotifyTrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * プレイリストのトラック情報を取得し、リストとして返します。
     *
     * @param tracks プレイリストのトラック配列
     * @return トラック情報のリスト
     */
    public List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) {
        logger.info("getTrackListData: トラック数: {}", tracks.length);

        List<Map<String, Object>> trackList = Arrays.stream(tracks)
                .map(this::getTrackData)
                .collect(Collectors.toList());

        logger.info("getTrackListData: トラックデータリスト作成完了");
        return trackList;
    }

    private Map<String, Object> getTrackData(PlaylistTrack playlistTrack) {
        Map<String, Object> trackData = new HashMap<>();
        Track fullTrack = (Track) playlistTrack.getTrack();
        trackData.put("track", fullTrack);

        String trackId = fullTrack.getId();
        try {
            AudioFeatures audioFeatures = trackService.getAudioFeaturesForTrack(trackId);
            trackData.put("audioFeatures", audioFeatures);
        } catch (Exception e) {
            logger.error("Error fetching audio features for track {}: {}", trackId, e.getMessage());
            trackData.put("audioFeatures", null);
        }

        return trackData;
    }
}
