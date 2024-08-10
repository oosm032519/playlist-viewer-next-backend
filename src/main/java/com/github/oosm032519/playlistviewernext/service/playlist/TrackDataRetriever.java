package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
     * @throws PlaylistViewerNextException トラックデータの取得中にエラーが発生した場合
     */
    public List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) {
        logger.info("getTrackListData: トラック数: {}", tracks.length);

        try {
            List<Map<String, Object>> trackList = Arrays.stream(tracks)
                    .map(this::getTrackData)
                    .collect(Collectors.toList());

            logger.info("getTrackListData: トラックデータリスト作成完了");
            return trackList;
        } catch (Exception e) {
            // トラックデータの取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("トラックデータの取得中にエラーが発生しました。", e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRACK_DATA_RETRIEVAL_ERROR",
                    "トラックデータの取得中にエラーが発生しました。",
                    e
            );
        }
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
