package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

/**
 * Spotifyトラックに関連するサービスを提供するクラス。
 * このクラスは、Spotify APIを使用してトラックのオーディオ特徴を取得します。
 */
@Service
public class SpotifyTrackService {

    /**
     * このクラスのロガーインスタンス
     */
    private static final Logger logger = LoggerFactory.getLogger(SpotifyTrackService.class);

    /**
     * Spotify APIとの通信に使用するSpotifyApiインスタンス
     */
    private final SpotifyApi spotifyApi;

    /**
     * SpotifyTrackServiceのコンストラクタ。
     *
     * @param spotifyApi Spotify APIとの通信に使用するSpotifyApiインスタンス
     */
    @Autowired
    public SpotifyTrackService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたトラックIDに対応するオーディオ特徴を取得します。
     *
     * @param trackId 取得対象のトラックID
     * @return 指定されたトラックのAudioFeatures
     * @throws SpotifyApiException オーディオ特徴の取得中にエラーが発生した場合
     */
    public AudioFeatures getAudioFeaturesForTrack(String trackId) {
        try {
            // Spotify APIを使用してオーディオ特徴のリクエストを構築
            GetAudioFeaturesForTrackRequest audioFeaturesRequest = spotifyApi.getAudioFeaturesForTrack(trackId).build();
            // リクエストを実行し、結果を返す
            return audioFeaturesRequest.execute();
        } catch (Exception e) {
            // エラーログを記録
            logger.error("Error retrieving audio features for track ID: " + trackId, e);
            // カスタム例外をスローして、エラーを上位層に伝播
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AUDIO_FEATURES_RETRIEVAL_ERROR",
                    "オーディオ特徴の取得中にエラーが発生しました。",
                    e
            );
        }
    }
}
