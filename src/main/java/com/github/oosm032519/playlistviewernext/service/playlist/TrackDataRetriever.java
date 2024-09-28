package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackDataRetriever {

    public static Logger logger = LoggerFactory.getLogger(TrackDataRetriever.class);

    private final SpotifyTrackService trackService;

    public TrackDataRetriever(SpotifyTrackService trackService) {
        this.trackService = trackService;
    }

    /**
     * プレイリストのトラック情報を取得し、リストとして返す
     *
     * @param tracks プレイリストのトラック配列
     * @return トラック情報のリスト
     * @throws SpotifyApiException トラックデータの取得中にエラーが発生した場合
     */
    public List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) {
        logger.info("getTrackListData: トラック数: {}", tracks.length);

        try {
            // 全てのトラックIDをリストアップ
            List<String> trackIds = Arrays.stream(tracks)
                    .map(track -> track.getTrack().getId())
                    .collect(Collectors.toList());

            // 全てのトラックのAudioFeaturesを一度に取得
            List<AudioFeatures> audioFeaturesList = trackService.getAudioFeaturesForTracks(trackIds);

            // トラック情報とAudioFeaturesをマッピング
            List<Map<String, Object>> trackList = new ArrayList<>();
            for (int i = 0; i < tracks.length; i++) {
                Map<String, Object> trackData = new HashMap<>();
                Track fullTrack = (Track) tracks[i].getTrack();
                trackData.put("track", fullTrack);
                trackData.put("audioFeatures", audioFeaturesList.get(i));
                trackList.add(trackData);
            }

            logger.info("getTrackListData: トラックデータリスト作成完了");
            return trackList;
        } catch (Exception e) {
            // トラックデータの取得中にエラーが発生した場合は SpotifyApiException をスロー
            logger.error("トラックデータの取得中にエラーが発生しました。", e);
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TRACK_DATA_RETRIEVAL_ERROR",
                    "トラックデータの取得中にエラーが発生しました。",
                    e
            );
        }
    }
}
