// TrackRecommendationService.java

package com.github.oosm032519.playlistviewernext.service.recommendation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class TrackRecommendationService {

    // ロガーのインスタンスを作成
    protected static Logger logger = LoggerFactory.getLogger(TrackRecommendationService.class);

    // SpotifyRecommendationServiceのインスタンスを注入
    @Autowired
    private SpotifyRecommendationService recommendationService;

    /**
     * トラックの推薦リストを取得するメソッド
     *
     * @param top5Genres          推薦に使用する上位5つのジャンルのリスト
     * @param maxAudioFeatures    オーディオ特徴量の最大値を含むマップ
     * @param minAudioFeatures    オーディオ特徴量の最小値を含むマップ
     * @param medianAudioFeatures オーディオ特徴量の中央値を含むマップ
     * @param modeValues          オーディオ特徴量の最頻値を含むマップ
     * @return 推薦されたトラックのリスト
     */
    public List<Track> getRecommendations(List<String> top5Genres, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> medianAudioFeatures, Map<String, Object> modeValues) {
        List<Track> recommendations = new ArrayList<>();
        try {
            // ジャンルリストが空でない場合、推薦サービスを呼び出して推薦トラックを取得
            if (!top5Genres.isEmpty()) {
                recommendations = recommendationService.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);
            }
        } catch (Exception e) {
            // エラーが発生した場合、ログにエラーメッセージを記録
            logger.error("TrackRecommendationService: Spotify APIの呼び出し中にエラーが発生しました。", e);
        }
        return recommendations;
    }

    /**
     * テスト用にロガーを設定するメソッド
     *
     * @param logger ロガーのインスタンス
     */
    public static void setLogger(Logger logger) {
        TrackRecommendationService.logger = logger;
    }
}
