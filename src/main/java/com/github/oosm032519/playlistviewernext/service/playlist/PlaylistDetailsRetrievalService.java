// PlaylistDetailsRetrievalService.java

package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.service.analytics.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.*;

@Service
public class PlaylistDetailsRetrievalService {

    // ロガーの初期化
    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsRetrievalService.class);

    // 各種サービスの自動配線
    @Autowired
    private SpotifyPlaylistDetailsService playlistDetailsService;
    @Autowired
    private SpotifyTrackService trackService;
    @Autowired
    private SpotifyClientCredentialsAuthentication authController;
    @Autowired
    private MaxAudioFeaturesCalculator maxAudioFeaturesCalculator;
    @Autowired
    private MinAudioFeaturesCalculator minAudioFeaturesCalculator;
    @Autowired
    private MedianAudioFeaturesCalculator medianAudioFeaturesCalculator;
    @Autowired
    private AverageAudioFeaturesCalculator averageAudioFeaturesCalculator;
    @Autowired
    private TrackDataRetriever trackDataRetriever;
    @Autowired
    private ModeValuesCalculator modeValuesCalculator;

    /**
     * プレイリストの詳細情報を取得するメソッド
     *
     * @param id プレイリストのID
     * @return プレイリストの詳細情報を含むマップ
     * @throws Exception 認証やデータ取得に失敗した場合にスローされる例外
     */
    public Map<String, Object> getPlaylistDetails(String id) throws Exception {
        // プレイリストIDのログ出力
        logger.info("getPlaylistDetails: プレイリストID: {}", id);

        // Spotify APIの認証
        authController.authenticate();

        // プレイリストのトラックを取得
        PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);

        // トラックの詳細情報をリストに変換
        List<Map<String, Object>> trackList = trackDataRetriever.getTrackListData(tracks);

        // プレイリスト名の取得
        String playlistName = playlistDetailsService.getPlaylistName(id);

        // プレイリストのオーナー情報を取得
        User owner = playlistDetailsService.getPlaylistOwner(id);

        // 最大オーディオフィーチャーの計算
        Map<String, Float> maxAudioFeatures = maxAudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 最大オーディオフィーチャー: {}", maxAudioFeatures);

        // 最小オーディオフィーチャーの計算
        Map<String, Float> minAudioFeatures = minAudioFeaturesCalculator.calculateMinAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 最小オーディオフィーチャー: {}", minAudioFeatures);

        // 中央オーディオフィーチャーの計算
        Map<String, Float> medianAudioFeatures = medianAudioFeaturesCalculator.calculateMedianAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 中央オーディオフィーチャー: {}", medianAudioFeatures);

        // 平均オーディオフィーチャーの計算
        Map<String, Float> averageAudioFeatures = averageAudioFeaturesCalculator.calculateAverageAudioFeatures(trackList);
        logger.info("getPlaylistDetails: 平均オーディオフィーチャー: {}", averageAudioFeatures);

        // 最頻値の計算
        Map<String, Object> modeValues = modeValuesCalculator.calculateModeValues(trackList);
        logger.info("getPlaylistDetails: 最頻値: {}", modeValues);

        // レスポンスマップの作成
        Map<String, Object> response = new HashMap<>();
        response.put("tracks", Map.of("items", trackList));
        response.put("playlistName", playlistName);
        response.put("ownerId", owner.getId());
        response.put("ownerName", owner.getDisplayName());
        response.put("maxAudioFeatures", maxAudioFeatures);
        response.put("minAudioFeatures", minAudioFeatures);
        response.put("medianAudioFeatures", medianAudioFeatures);
        response.put("averageAudioFeatures", averageAudioFeatures);
        response.put("modeValues", modeValues);

        // レスポンスマップを返却
        return response;
    }
}
