package com.github.oosm032519.playlistviewernext.service.recommendation;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.service.analytics.AudioFeatureSetter;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
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
    public SpotifyRecommendationService(SpotifyApi spotifyApi, AudioFeatureSetter audioFeatureSetter) {
        this.spotifyApi = spotifyApi;
        this.audioFeatureSetter = audioFeatureSetter;
    }

    /**
     * 指定されたパラメータに基づいて推奨トラックのリストを取得します。
     *
     * @param seedArtists      アーティストIDのシードリスト
     * @param maxAudioFeatures 最大AudioFeaturesのマップ
     * @param minAudioFeatures 最小AudioFeaturesのマップ
     * @return 推奨トラックのリスト
     */
    public List<Track> getRecommendations(List<String> seedArtists, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures) {
        logger.info("getRecommendations: seedArtists: {}, maxAudioFeatures: {}, minAudioFeatures: {}", seedArtists, maxAudioFeatures, minAudioFeatures);

        if (seedArtists == null || seedArtists.isEmpty()) {
            logger.warn("seedArtistsがnullまたは空です。");
            return Collections.emptyList();
        }

        return RetryUtil.executeWithRetry(() -> {
            try {
                GetRecommendationsRequest recommendationsRequest = createRecommendationsRequest(seedArtists, maxAudioFeatures, minAudioFeatures);
                Recommendations recommendations = recommendationsRequest.execute();

                if (recommendations == null || recommendations.getTracks() == null) {
                    logger.info("getRecommendations: 推奨トラックが見つかりませんでした");
                    return Collections.emptyList();
                }

                logger.info("getRecommendations: 推奨トラック数: {}", recommendations.getTracks().length);
                return Stream.of(recommendations.getTracks()).collect(Collectors.toList());
            } catch (SpotifyWebApiException e) {
                // SpotifyWebApiException はそのまま再スロー
                logger.error("Spotify API エラー: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                // その他の例外は InternalServerException にラップしてスロー
                logger.error("推奨トラックの取得中にエラーが発生しました。", e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "推奨トラックの取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS);
    }

    /**
     * 推奨リクエストを作成します。
     *
     * @param seedArtists      アーティストのシードリスト
     * @param maxAudioFeatures 最大AudioFeaturesのマップ
     * @param minAudioFeatures 最小AudioFeaturesのマップ
     * @return 構築されたGetRecommendationsRequest
     */
    private GetRecommendationsRequest createRecommendationsRequest(List<String> seedArtists, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures) {
        String artists = String.join(",", seedArtists);

        GetRecommendationsRequest.Builder recommendationsRequestBuilder = spotifyApi.getRecommendations()
                .seed_artists(artists)
                .limit(20);

        audioFeatureSetter.setMaxAudioFeatures(recommendationsRequestBuilder, maxAudioFeatures);
        audioFeatureSetter.setMinAudioFeatures(recommendationsRequestBuilder, minAudioFeatures);

        GetRecommendationsRequest request = recommendationsRequestBuilder.build();
        logger.debug("Recommendation Request Parameters: {}", request.getBodyParameters());
        return request;
    }
}
