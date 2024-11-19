package com.github.oosm032519.playlistviewernext.service.analytics;

import org.springframework.stereotype.Component;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Spotifyの楽曲推薦リクエストに対してオーディオ特徴の制約を設定するコンポーネント。
 * 最大値と最小値の制約を個別に設定する。
 */
@Component
public class AudioFeatureSetter {

    /**
     * GetRecommendationsRequest.Builderに最大AudioFeaturesを設定する。
     * 各AudioFeaturesの上限値を設定する。
     *
     * @param builder          楽曲推薦リクエストのビルダーオブジェクト
     * @param maxAudioFeatures AudioFeaturesの最大値を格納したマップ。キーはAudioFeatures名、値は制限値
     */
    public void setMaxAudioFeatures(GetRecommendationsRequest.Builder builder, Map<String, Float> maxAudioFeatures) {
        setAudioFeatures(builder, maxAudioFeatures, this::setMaxFeature);
    }

    /**
     * AudioFeaturesの設定を一括して行う内部メソッド。
     *
     * @param builder       楽曲推薦リクエストのビルダーオブジェクト
     * @param audioFeatures 設定するAudioFeaturesのマップ
     * @param featureSetter 特徴を設定するための関数インターフェース
     */
    private void setAudioFeatures(GetRecommendationsRequest.Builder builder, Map<String, Float> audioFeatures, BiConsumer<GetRecommendationsRequest.Builder, Map.Entry<String, Float>> featureSetter) {
        // マップの各エントリーに対して特徴設定処理を実行
        for (Map.Entry<String, Float> entry : audioFeatures.entrySet()) {
            featureSetter.accept(builder, entry);
        }
    }

    /**
     * 各AudioFeaturesの最大値を設定する内部メソッド。
     *
     * @param builder ビルダーオブジェクト
     * @param entry   設定するAudioFeatures名と値のエントリー
     */
    private void setMaxFeature(GetRecommendationsRequest.Builder builder, Map.Entry<String, Float> entry) {
        // 特徴名に応じて対応するビルダーメソッドを呼び出し
        switch (entry.getKey()) {
            case "danceability":
                builder.max_danceability(entry.getValue());
                break;
            case "energy":
                builder.max_energy(entry.getValue());
                break;
            case "valence":
                builder.max_valence(entry.getValue());
                break;
            case "tempo":
                builder.max_tempo(entry.getValue());
                break;
            case "acousticness":
                builder.max_acousticness(entry.getValue());
                break;
            case "instrumentalness":
                builder.max_instrumentalness(entry.getValue());
                break;
            case "liveness":
                builder.max_liveness(entry.getValue());
                break;
            case "speechiness":
                builder.max_speechiness(entry.getValue());
                break;
        }
    }

    /**
     * GetRecommendationsRequest.Builderに最小AudioFeaturesを設定する。
     * 各AudioFeaturesの下限値を設定する。
     *
     * @param builder          楽曲推薦リクエストのビルダーオブジェクト
     * @param minAudioFeatures AudioFeaturesの最小値を格納したマップ。キーはAudioFeatures名、値は制限値
     */
    public void setMinAudioFeatures(GetRecommendationsRequest.Builder builder, Map<String, Float> minAudioFeatures) {
        setAudioFeatures(builder, minAudioFeatures, this::setMinFeature);
    }

    /**
     * 各AudioFeaturesの最小値を設定する内部メソッド。
     *
     * @param builder ビルダーオブジェクト
     * @param entry   設定するAudioFeaturesと値のエントリー
     */
    private void setMinFeature(GetRecommendationsRequest.Builder builder, Map.Entry<String, Float> entry) {
        // 特徴名に応じて対応するビルダーメソッドを呼び出し
        switch (entry.getKey()) {
            case "danceability":
                builder.min_danceability(entry.getValue());
                break;
            case "energy":
                builder.min_energy(entry.getValue());
                break;
            case "valence":
                builder.min_valence(entry.getValue());
                break;
            case "tempo":
                builder.min_tempo(entry.getValue());
                break;
            case "acousticness":
                builder.min_acousticness(entry.getValue());
                break;
            case "instrumentalness":
                builder.min_instrumentalness(entry.getValue());
                break;
            case "liveness":
                builder.min_liveness(entry.getValue());
                break;
            case "speechiness":
                builder.min_speechiness(entry.getValue());
                break;
        }
    }
}
