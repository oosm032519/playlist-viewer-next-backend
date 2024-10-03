package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.AudioFeatures;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForSeveralTracksRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Spotifyトラックに関連するサービスを提供するクラス
 * 複数トラックのAudioFeaturesを一度に取得する
 */
@Service
public class SpotifyTrackService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyTrackService.class);
    private static final int MAX_TRACKS_PER_REQUEST = 100; // Spotify APIの制限

    private final SpotifyApi spotifyApi;

    public SpotifyTrackService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 指定されたトラックIDリストに対応するAudioFeaturesを取得する
     * 100曲を超える場合は内部で分割してリクエストする
     *
     * @param trackIds 取得対象のトラックIDリスト
     * @return 指定されたトラックのAudioFeaturesリスト
     * @throws InternalServerException その他のエラーが発生した場合
     */
    public List<AudioFeatures> getAudioFeaturesForTracks(List<String> trackIds) {
        logger.info("getAudioFeaturesForTracks: トラック数: {}", trackIds.size());

        return RetryUtil.executeWithRetry(() -> {
            try {
                List<AudioFeatures> allAudioFeatures = new ArrayList<>();

                // 100曲ずつ分割してリクエスト
                for (int i = 0; i < trackIds.size(); i += MAX_TRACKS_PER_REQUEST) {
                    int endIndex = Math.min(i + MAX_TRACKS_PER_REQUEST, trackIds.size());
                    List<String> trackIdsChunk = trackIds.subList(i, endIndex);

                    // APIリクエスト
                    String ids = String.join(",", trackIdsChunk);
                    GetAudioFeaturesForSeveralTracksRequest request = spotifyApi.getAudioFeaturesForSeveralTracks(ids).build();
                    AudioFeatures[] audioFeaturesArray = request.execute();

                    allAudioFeatures.addAll(Arrays.asList(audioFeaturesArray));
                }

                logger.info("getAudioFeaturesForTracks: AudioFeatures取得完了");
                return allAudioFeatures;
            } catch (SpotifyWebApiException e) {
                // SpotifyWebApiException はそのまま再スロー
                logger.error("Spotify API エラー: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                logger.error("AudioFeaturesの取得中にエラーが発生しました。 trackIds: {}", trackIds, e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "AudioFeaturesの取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }
}
