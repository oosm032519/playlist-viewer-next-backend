// TrackDataRetriever.java

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

    // ロガーのインスタンスを取得
    private static final Logger logger = LoggerFactory.getLogger(TrackDataRetriever.class);

    // SpotifyTrackServiceのインスタンスを自動的に注入
    @Autowired
    private SpotifyTrackService trackService;

    /**
     * プレイリストのトラック情報を取得し、リストとして返すメソッド
     *
     * @param tracks プレイリストのトラック配列
     * @return トラック情報のリスト
     * @throws Exception トラック情報の取得中に発生する可能性のある例外
     */
    public List<Map<String, Object>> getTrackListData(PlaylistTrack[] tracks) throws Exception {
        // トラック数をログに出力
        logger.info("getTrackListData: トラック数: {}", tracks.length);

        // トラック情報を格納するリストを初期化
        List<Map<String, Object>> trackList = new ArrayList<>();

        // 各トラックについて処理を行う
        for (PlaylistTrack track : tracks) {
            // トラック情報を格納するマップを初期化
            Map<String, Object> trackData = new HashMap<>();

            // プレイリストトラックからフルトラック情報を取得
            Track fullTrack = (Track) track.getTrack();
            trackData.put("track", fullTrack);

            // トラックIDを取得
            String trackId = fullTrack.getId();

            // トラックのオーディオ特徴を取得
            AudioFeatures audioFeatures = trackService.getAudioFeaturesForTrack(trackId);
            trackData.put("audioFeatures", audioFeatures);

            // トラック情報をリストに追加
            trackList.add(trackData);
        }

        // トラックデータリスト作成完了をログに出力
        logger.info("getTrackListData: トラックデータリスト作成完了");

        // トラック情報のリストを返す
        return trackList;
    }
}
