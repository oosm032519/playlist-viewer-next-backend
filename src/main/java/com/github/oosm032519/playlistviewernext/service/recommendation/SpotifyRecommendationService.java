package com.github.oosm032519.playlistviewernext.service.recommendation;

import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import com.github.oosm032519.playlistviewernext.service.analytics.AudioFeatureSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 与えられたパラメータに基づいて推奨トラックを取得するサービスクラス
 */
@Service
public class SpotifyRecommendationService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyRecommendationService.class);

    private final SpotifyApi spotifyApi;
    private final AudioFeatureSetter audioFeatureSetter;

    /**
     * SpotifyRecommendationServiceのコンストラクタ。
     *
     * @param spotifyApi         Spotify APIクライアント
     * @param audioFeatureSetter AudioFeaturesを設定するユーティリティ
     */
    @Autowired
    public SpotifyRecommendationService(SpotifyApi spotifyApi, AudioFeatureSetter audioFeatureSetter) {
        this.spotifyApi = spotifyApi;
        this.audioFeatureSetter = audioFeatureSetter;
    }

    /**
     * 指定されたパラメータに基づいて推奨トラックのリストを取得します。
     *
     * @param seedGenres          ジャンルのシードリスト
     * @param maxAudioFeatures    最大AudioFeaturesのマップ
     * @param minAudioFeatures    最小AudioFeaturesのマップ
     * @param medianAudioFeatures 中央値のAudioFeaturesのマップ
     * @param modeValues          モード値のマップ
     * @return 推奨トラックのリスト
     * @throws SpotifyApiException Spotify APIの呼び出し中にエラーが発生した場合
     */
    public List<Track> getRecommendations(List<String> seedGenres, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> medianAudioFeatures, Map<String, Object> modeValues) {
        logger.info("getRecommendations: seedGenres: {}, maxAudioFeatures: {}, minAudioFeatures: {}, medianAudioFeatures: {}, modeValues: {}", seedGenres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        if (seedGenres.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            GetRecommendationsRequest recommendationsRequest = createRecommendationsRequest(seedGenres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

            Recommendations recommendations = recommendationsRequest.execute();

            if (recommendations == null || recommendations.getTracks() == null) {
                logger.info("getRecommendations: 推奨トラックが見つかりませんでした");
                return Collections.emptyList();
            }

            logger.info("getRecommendations: 推奨トラック数: {}", recommendations.getTracks().length);

            return Stream.of(recommendations.getTracks()).collect(Collectors.toList());
        } catch (Exception e) {
            // 推奨トラックの取得中にエラーが発生した場合は SpotifyApiException をスロー
            logger.error("推奨トラックの取得中にエラーが発生しました。", e);
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "RECOMMENDATIONS_RETRIEVAL_ERROR",
                    "推奨トラックの取得中にエラーが発生しました。",
                    e
            );
        }
    }

    /**
     * 推奨リクエストを作成します。
     *
     * @param seedGenres          ジャンルのシードリスト
     * @param maxAudioFeatures    最大AudioFeaturesのマップ
     * @param minAudioFeatures    最小AudioFeaturesのマップ
     * @param medianAudioFeatures 中央値のAudioFeaturesのマップ
     * @param modeValues          モード値のマップ
     * @return 構築されたGetRecommendationsRequest
     */
    private GetRecommendationsRequest createRecommendationsRequest(List<String> seedGenres, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> medianAudioFeatures, Map<String, Object> modeValues) {
        String genres = String.join(",", seedGenres);

        GetRecommendationsRequest.Builder recommendationsRequestBuilder = spotifyApi.getRecommendations()
                .seed_genres(genres)
                .limit(20);

        // AudioFeaturesとモード値を設定
        audioFeatureSetter.setMaxAudioFeatures(recommendationsRequestBuilder, maxAudioFeatures);
        audioFeatureSetter.setMinAudioFeatures(recommendationsRequestBuilder, minAudioFeatures);
        audioFeatureSetter.setMedianAudioFeatures(recommendationsRequestBuilder, medianAudioFeatures);
        audioFeatureSetter.setModeValues(recommendationsRequestBuilder, modeValues);

        return recommendationsRequestBuilder.build();
    }
}
