// SpotifyRecommendationService.java

package com.github.oosm032519.playlistviewernext.service.recommendation;

import com.github.oosm032519.playlistviewernext.service.analytics.AudioFeatureSetter;
import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Recommendations;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class SpotifyRecommendationService {
    // ロガーの初期化
    private static final Logger logger = LoggerFactory.getLogger(SpotifyRecommendationService.class);

    // Spotify APIインスタンス
    private final SpotifyApi spotifyApi;

    // オーディオフィーチャー設定クラスのインスタンス
    private final AudioFeatureSetter audioFeatureSetter;

    /**
     * コンストラクタ
     *
     * @param spotifyApi         Spotify APIインスタンス
     * @param audioFeatureSetter オーディオフィーチャー設定クラスのインスタンス
     */
    @Autowired
    public SpotifyRecommendationService(SpotifyApi spotifyApi, AudioFeatureSetter audioFeatureSetter) {
        this.spotifyApi = spotifyApi;
        this.audioFeatureSetter = audioFeatureSetter;
    }

    /**
     * 推奨トラックを取得するメソッド
     *
     * @param seedGenres          シードジャンルのリスト
     * @param maxAudioFeatures    最大オーディオフィーチャーのマップ
     * @param minAudioFeatures    最小オーディオフィーチャーのマップ
     * @param medianAudioFeatures 中央オーディオフィーチャーのマップ
     * @param modeValues          モード値のマップ
     * @return 推奨トラックのリスト
     * @throws IOException            入出力例外
     * @throws SpotifyWebApiException Spotify API例外
     * @throws ParseException         解析例外
     */
    public List<Track> getRecommendations(List<String> seedGenres, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> medianAudioFeatures, Map<String, Object> modeValues) throws IOException, SpotifyWebApiException, ParseException {
        // メソッド開始ログ
        logger.info("getRecommendations: seedGenres: {}, maxAudioFeatures: {}, minAudioFeatures: {}, medianAudioFeatures: {}, modeValues: {}", seedGenres, maxAudioFeatures, minAudioFeatures, medianAudioFeatures, modeValues);

        // シードジャンルが空の場合、空のリストを返す
        if (seedGenres.isEmpty()) {
            return Collections.emptyList();
        }

        // シードジャンルをカンマ区切りの文字列に変換
        String genres = String.join(",", seedGenres);

        // 推奨リクエストのビルダーを作成
        GetRecommendationsRequest.Builder recommendationsRequestBuilder = spotifyApi.getRecommendations()
                .seed_genres(genres)
                .limit(20);

        // 最大オーディオフィーチャーを設定
        audioFeatureSetter.setMaxAudioFeatures(recommendationsRequestBuilder, maxAudioFeatures);

        // 最小オーディオフィーチャーを設定
        audioFeatureSetter.setMinAudioFeatures(recommendationsRequestBuilder, minAudioFeatures);

        // 中央オーディオフィーチャーを設定
        audioFeatureSetter.setMedianAudioFeatures(recommendationsRequestBuilder, medianAudioFeatures);

        // モード値を設定
        audioFeatureSetter.setModeValues(recommendationsRequestBuilder, modeValues);

        // 推奨リクエストをビルド
        GetRecommendationsRequest recommendationsRequest = recommendationsRequestBuilder.build();

        // 推奨を実行
        Recommendations recommendations = recommendationsRequest.execute();

        // 推奨トラックが存在しない場合、空のリストを返す
        if (recommendations == null || recommendations.getTracks() == null) {
            logger.info("getRecommendations: 推奨トラックが見つかりませんでした");
            return Collections.emptyList();
        }

        // 推奨トラック数のログを記録
        logger.info("getRecommendations: 推奨トラック数: {}", recommendations.getTracks().length);

        // 推奨トラックをリストに変換して返す
        return Arrays.asList(recommendations.getTracks());
    }
}
