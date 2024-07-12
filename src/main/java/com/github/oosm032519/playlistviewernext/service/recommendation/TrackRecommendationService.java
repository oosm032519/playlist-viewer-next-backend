package com.github.oosm032519.playlistviewernext.service.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class TrackRecommendationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackRecommendationService.class);

    private final SpotifyRecommendationService recommendationService;

    public TrackRecommendationService(SpotifyRecommendationService recommendationService) {
        this.recommendationService = recommendationService;
    }

    /**
     * トラックの推薦リストを取得します。
     *
     * @param genres              推薦に使用する上位5つのジャンルのリスト
     * @param maxAudioFeatures    オーディオ特徴量の最大値を含むマップ
     * @param minAudioFeatures    オーディオ特徴量の最小値を含むマップ
     * @param medianAudioFeatures オーディオ特徴量の中央値を含むマップ
     * @param modeValues          オーディオ特徴量の最頻値を含むマップ
     * @return 推薦されたトラックのリスト
     */
    public List<Track> getRecommendations(List<String> genres,
                                          Map<String, Float> maxAudioFeatures,
                                          Map<String, Float> minAudioFeatures,
                                          Map<String, Float> medianAudioFeatures,
                                          Map<String, Object> modeValues) {
        if (genres.isEmpty()) {
            LOGGER.warn("ジャンルリストが空です。推薦を生成できません。");
            return Collections.emptyList();
        }

        try {
            return recommendationService.getRecommendations(genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
        } catch (Exception e) {
            LOGGER.error("Spotify APIの呼び出し中にエラーが発生しました。", e);
            return Collections.emptyList();
        }
    }
}
