package com.github.oosm032519.playlistviewernext.service.recommendation;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
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
     * トラックの推薦リストを取得する
     *
     * @param artists          推薦に使用する上位5つのアーティストのリスト
     * @param maxAudioFeatures AudioFeaturesの最大値を含むマップ
     * @param minAudioFeatures AudioFeaturesの最小値を含むマップ
     * @return 推薦されたトラックのリスト
     */
    public List<Track> getRecommendations(List<String> artists,
                                          Map<String, Float> maxAudioFeatures,
                                          Map<String, Float> minAudioFeatures) {
        if (artists == null || artists.isEmpty()) {
            LOGGER.warn("アーティストリストが空です。推薦を生成できません。");
            return Collections.emptyList();
        }

        try {
            return recommendationService.getRecommendations(artists, maxAudioFeatures, minAudioFeatures);
        } catch (Exception e) {
            LOGGER.error("Spotify APIの呼び出し中にエラーが発生しました。", e);
            throw new InternalServerException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "トラックの推薦中にエラーが発生しました。",
                    e
            );
        }
    }
}
