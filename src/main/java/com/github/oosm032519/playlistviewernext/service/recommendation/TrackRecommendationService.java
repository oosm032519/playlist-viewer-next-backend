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

    protected static Logger logger = LoggerFactory.getLogger(TrackRecommendationService.class);

    @Autowired
    private SpotifyRecommendationService recommendationService;

    public List<Track> getRecommendations(List<String> top5Genres, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> medianAudioFeatures) {
        List<Track> recommendations = new ArrayList<>();
        try {
            if (!top5Genres.isEmpty()) {
                recommendations = recommendationService.getRecommendations(top5Genres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures);
            }
        } catch (Exception e) {
            logger.error("TrackRecommendationService: Spotify APIの呼び出し中にエラーが発生しました。", e);
        }
        return recommendations;
    }

    // テスト用のメソッドを追加
    public static void setLogger(Logger logger) {
        TrackRecommendationService.logger = logger;
    }
}
