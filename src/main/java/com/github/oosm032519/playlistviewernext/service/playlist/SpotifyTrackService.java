package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
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
    private final WebClient webClient;

    @Value("${spotify.mock-api.url}")
    private String mockApiUrl;

    @Value("${spotify.mock.enabled:false}")
    private boolean mockEnabled;

    @Autowired
    public SpotifyTrackService(SpotifyApi spotifyApi, WebClient webClient) {
        logger.info("SpotifyTrackService constructor started. mockApiUrl: {}", mockApiUrl);
        this.spotifyApi = spotifyApi;
        this.webClient = webClient;
        logger.info("SpotifyTrackService constructor finished.");
    }

    /**
     * 指定されたトラックIDリストに対応するAudioFeaturesを取得する
     * 100曲を超える場合は内部で分割してリクエストする
     *
     * @param trackIds 取得対象のトラックIDリスト
     * @return 指定されたトラックのAudioFeaturesリスト
     * @throws InternalServerException その他のエラーが発生した場合
     */
    public List<AudioFeatures> getAudioFeaturesForTracks(List<String> trackIds) throws SpotifyWebApiException {
        logger.info("getAudioFeaturesForTracks: トラック数: {}", trackIds.size());

        if (mockEnabled && mockApiUrl != null && !mockApiUrl.isEmpty()) {
            logger.info("getAudioFeaturesForTracks: モックAPIからAudioFeaturesを取得");
            return getAudioFeaturesForTracksMock(trackIds);
        } else {
            logger.info("getAudioFeaturesForTracks: Spotify APIからAudioFeaturesを取得");
            return getAudioFeaturesForTracksReal(trackIds);
        }
    }

    private List<AudioFeatures> getAudioFeaturesForTracksMock(List<String> trackIds) {
        logger.info("getAudioFeaturesForTracksMock: trackIds: {}", trackIds);
        logger.info("getAudioFeaturesForTracksMock: mockApiUrl: {}", mockApiUrl);
        logger.info("getAudioFeaturesForTracksMock: WebClient GET リクエスト送信, URL: {}/tracks/audio-features", mockApiUrl);


        // WebClientを使用してモックAPIからデータを取得
        List<AudioFeatures> audioFeatures = null;
        try {
            audioFeatures = webClient.get()
                    .uri(mockApiUrl + "/tracks/audio-features", uriBuilder -> uriBuilder
                            .queryParam("trackIds", String.join(",", trackIds))
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<AudioFeatures>>() {
                    })
                    .onErrorMap(WebClientResponseException.class, e -> {
                        logger.error("getAudioFeaturesForTracksMock: Mock API 呼び出しエラー: {}", e.getResponseBodyAsString(), e);
                        return new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Error calling mock API", e);
                    })
                    .block();
            logger.info("getAudioFeaturesForTracksMock: Mock API レスポンス受信, audioFeatures size: {}", audioFeatures != null ? audioFeatures.size() : 0);
            logger.debug("getAudioFeaturesForTracksMock: Mock API レスポンス内容: {}", audioFeatures);

        } catch (WebClientRequestException e) {
            logger.error("getAudioFeaturesForTracksMock: WebClientRequestException: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("getAudioFeaturesForTracksMock: 予期せぬエラー: ", e);
            throw e;
        }


        return audioFeatures;
    }

    private List<AudioFeatures> getAudioFeaturesForTracksReal(List<String> trackIds) throws SpotifyWebApiException {
        logger.info("getAudioFeaturesForTracksReal: trackIds: {}", trackIds);
        return RetryUtil.executeWithRetry(() -> {
            try {
                List<AudioFeatures> allAudioFeatures = new ArrayList<>();

                // 100曲ずつ分割してリクエスト
                for (int i = 0; i < trackIds.size(); i += MAX_TRACKS_PER_REQUEST) {
                    int endIndex = Math.min(i + MAX_TRACKS_PER_REQUEST, trackIds.size());
                    List<String> trackIdsChunk = trackIds.subList(i, endIndex);
                    logger.info("getAudioFeaturesForTracksReal: トラックID分割処理, chunk index: {}, chunkSize: {}", i / MAX_TRACKS_PER_REQUEST, trackIdsChunk.size());


                    // APIリクエスト
                    String ids = String.join(",", trackIdsChunk);
                    logger.info("getAudioFeaturesForTracksReal: Spotify API リクエスト送信, trackIdsChunk: {}", ids);
                    GetAudioFeaturesForSeveralTracksRequest request = spotifyApi.getAudioFeaturesForSeveralTracks(ids).build();
                    AudioFeatures[] audioFeaturesArray = request.execute();
                    List<AudioFeatures> features = Arrays.asList(audioFeaturesArray);
                    logger.info("getAudioFeaturesForTracksReal: Spotify API レスポンス受信, audioFeaturesArray size: {}", features.size());


                    allAudioFeatures.addAll(features);
                    logger.info("getAudioFeaturesForTracksReal: AudioFeatures をリストに追加, allAudioFeatures size: {}", allAudioFeatures.size());
                }

                logger.info("getAudioFeaturesForTracksReal: AudioFeatures取得完了, 合計 AudioFeatures 数: {}", allAudioFeatures.size());
                return allAudioFeatures;
            } catch (SpotifyWebApiException e) {
                // SpotifyWebApiException はそのまま再スロー
                logger.error("getAudioFeaturesForTracksReal: SpotifyWebApiException: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                logger.error("getAudioFeaturesForTracksReal: AudioFeaturesの取得中にエラーが発生しました。 trackIds: {}", trackIds, e);
                logger.error("getAudioFeaturesForTracksReal: エラー詳細: ", e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "AudioFeaturesの取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }
}
