package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import se.michaelthelin.spotify.model_objects.IPlaylistItem;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class TrackDataRetriever {

    public static Logger logger = LoggerFactory.getLogger(TrackDataRetriever.class);

    public final SpotifyTrackService trackService;
    private final WebClient webClient;

    @Autowired
    public TrackDataRetriever(SpotifyTrackService trackService, WebClient webClient) {
        logger.info("TrackDataRetriever constructor started.");
        this.trackService = trackService;
        this.webClient = webClient;
        logger.info("TrackDataRetriever constructor finished.");
    }

    /**
     * プレイリストのトラック情報を取得し、リストとして返す
     *
     * @param tracks プレイリストのトラック配列
     * @return トラック情報のリスト
     */
    public List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) {
        logger.info("getTrackListData: トラック数: {}", tracks != null ? tracks.length : 0);

        List<Map<String, Object>> trackList = new ArrayList<>();
        try {
            if (tracks == null) {
                logger.warn("getTrackListData: tracks 配列が null です。");
                return Collections.emptyList(); // 空のリストを返すか、例外処理を検討
            }

            // 全てのトラックIDをリストアップ
            List<String> trackIds = Arrays.stream(tracks)
                    .map(PlaylistTrack::getTrack)
                    .filter(Objects::nonNull) // null チェックを追加
                    .map(IPlaylistItem::getId)
                    .filter(Objects::nonNull) // null チェックを追加
                    .collect(Collectors.toList());
            logger.info("getTrackListData: トラックIDリスト作成完了, trackIds size: {}", trackIds.size());
            logger.debug("getTrackListData: トラックIDリスト: {}", trackIds);


            // 全てのトラックのAudioFeaturesを一度に取得
            logger.info("getTrackListData: AudioFeatures 取得開始, trackIds size: {}", trackIds.size());
            List<AudioFeatures> audioFeaturesList = Collections.emptyList(); //初期化
            if (!trackIds.isEmpty()) {
                audioFeaturesList = trackService.getAudioFeaturesForTracks(trackIds);
            }
            logger.info("getTrackListData: AudioFeatures 取得完了, audioFeaturesList size: {}", audioFeaturesList.size());

            // トラック情報とAudioFeaturesをマッピング
            logger.info("getTrackListData: トラック情報と AudioFeatures のマッピング開始");
            for (int i = 0; i < tracks.length; i++) {
                Map<String, Object> trackData = new HashMap<>();
                Track fullTrack = (Track) tracks[i].getTrack();
                AudioFeatures audioFeatures = i < audioFeaturesList.size() ? audioFeaturesList.get(i) : null;

                trackData.put("track", fullTrack);
                trackData.put("audioFeatures", audioFeatures);
                trackList.add(trackData);
                logger.debug("getTrackListData: トラックデータ追加, track: {}, audioFeatures: {}", fullTrack, audioFeatures);
            }
            logger.info("getTrackListData: トラック情報と AudioFeatures のマッピング完了, trackList size: {}", trackList.size());


            logger.info("getTrackListData: トラックデータリスト作成完了");
            return trackList;
        } catch (Exception e) {
            logger.error("getTrackListData: トラックデータの取得中にエラーが発生しました。", e);
            logger.error("getTrackListData: エラー詳細: ", e);
            throw new InternalServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "トラックデータの取得中にエラーが発生しました。",
                    e
            );
        }
    }
}
