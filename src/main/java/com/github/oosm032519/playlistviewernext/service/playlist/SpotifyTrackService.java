package com.github.oosm032519.playlistviewernext.service.playlist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

import java.io.IOException;

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
     * @throws IOException                             入出力例外
     * @throws SpotifyWebApiException                  Spotify API例外
     * @throws org.apache.hc.core5.http.ParseException パース例外
     */
    public AudioFeatures getAudioFeaturesForTrack(String trackId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        try {
            // トラックIDに基づいてオーディオ特徴を取得するリクエストを作成
            GetAudioFeaturesForTrackRequest audioFeaturesRequest = spotifyApi.getAudioFeaturesForTrack(trackId).build();

            // リクエストを実行してオーディオ特徴を取得
            return audioFeaturesRequest.execute();
        } catch (IOException | SpotifyWebApiException | org.apache.hc.core5.http.ParseException e) {
            logger.error("Error retrieving audio features for track ID: " + trackId, e);
            throw e;
        }
    }
}
