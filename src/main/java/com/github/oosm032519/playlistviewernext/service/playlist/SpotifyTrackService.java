package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.PlaylistViewerNextException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

@Service
public class SpotifyTrackService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyTrackService.class);
    private final SpotifyApi spotifyApi;

    /**
     * コンストラクタ
     * SpotifyApiオブジェクトを注入します。
     *
     * @param spotifyApi Spotify APIクライアント
     */
    @Autowired
    public SpotifyTrackService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたトラックIDのオーディオ特徴を取得します。
     *
     * @param trackId トラックのID
     * @return AudioFeatures オーディオ特徴オブジェクト
     * @throws PlaylistViewerNextException オーディオ特徴の取得中にエラーが発生した場合
     */
    public AudioFeatures getAudioFeaturesForTrack(String trackId) {
        try {
            // トラックIDに基づいてオーディオ特徴を取得するリクエストを作成
            GetAudioFeaturesForTrackRequest audioFeaturesRequest = spotifyApi.getAudioFeaturesForTrack(trackId).build();

            // リクエストを実行してオーディオ特徴を取得
            return audioFeaturesRequest.execute();
        } catch (Exception e) {
            // オーディオ特徴の取得中にエラーが発生した場合は PlaylistViewerNextException をスロー
            logger.error("Error retrieving audio features for track ID: " + trackId, e);
            throw new PlaylistViewerNextException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AUDIO_FEATURES_RETRIEVAL_ERROR",
                    "オーディオ特徴の取得中にエラーが発生しました。",
                    e
            );
        }
    }
}
