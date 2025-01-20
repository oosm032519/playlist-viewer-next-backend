package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
import com.github.oosm032519.playlistviewernext.model.mock.MockData;
import com.github.oosm032519.playlistviewernext.service.analytics.AudioFeaturesCalculator;
import com.github.oosm032519.playlistviewernext.service.analytics.SpotifyPlaylistAnalyticsService;
import com.github.oosm032519.playlistviewernext.service.recommendation.SpotifyRecommendationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spotifyプレイリストの詳細情報を取得するサービスクラス
 * プレイリストの楽曲情報、オーディオ特徴、統計情報などを提供する
 */
@Service
public class PlaylistDetailsRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsRetrievalService.class);

    private final SpotifyPlaylistDetailsService playlistDetailsService;
    private final SpotifyClientCredentialsAuthentication authController;
    private final TrackDataRetriever trackDataRetriever;
    private final SpotifyPlaylistAnalyticsService playlistAnalyticsService;

    @Value("${spotify.mock.enabled}")
    private boolean mockEnabled;

    /**
     * コンストラクタ - 必要な依存関係を注入する
     *
     * @param playlistDetailsService     プレイリスト詳細取得サービス
     * @param authController             Spotify認証コントローラ
     * @param trackDataRetriever         トラックデータ取得サービス
     * @param playlistAnalyticsService   プレイリスト分析サービス
     * @param trackRecommendationService トラック推薦サービス
     */
    @Autowired
    public PlaylistDetailsRetrievalService(
            SpotifyPlaylistDetailsService playlistDetailsService,
            @Autowired(required = false) SpotifyClientCredentialsAuthentication authController,
            TrackDataRetriever trackDataRetriever,
            SpotifyPlaylistAnalyticsService playlistAnalyticsService,
            SpotifyRecommendationService trackRecommendationService) {
        this.playlistDetailsService = playlistDetailsService;
        this.authController = authController;
        this.trackDataRetriever = trackDataRetriever;
        this.playlistAnalyticsService = playlistAnalyticsService;
    }

    /**
     * プレイリストの詳細情報を取得する
     *
     * @param id プレイリストID
     * @return プレイリストの詳細情報を含むMap
     * @throws ResourceNotFoundException プレイリストが見つからない場合
     * @throws InvalidRequestException   処理中にエラーが発生した場合
     */
    public Map<String, Object> getPlaylistDetails(String id) {
        logger.info("getPlaylistDetails: プレイリストID: {}", id);

        try {
            // モックモードが有効な場合は認証をスキップ
            if (!mockEnabled && authController != null) {
                // Spotify APIの認証を行う
                authController.authenticate();
            }

            // モックモードの場合はモックデータを返す
            if (mockEnabled) {
                logger.info("モックモードが有効です。モックデータを返します。");
                return MockData.getMockedPlaylistDetails(id);
            }

            // プレイリスト情報の取得
            Playlist playlist = playlistDetailsService.getPlaylist(id);
            if (playlist == null) {
                throw new ResourceNotFoundException(
                        HttpStatus.NOT_FOUND,
                        "指定されたプレイリストが見つかりません。"
                );
            }

            String playlistName = playlist.getName();
            User owner = playlist.getOwner();

            // トラック情報の取得と解析
            PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);
            List<Map<String, Object>> trackList = trackDataRetriever.getTrackListData(tracks);

            // オーディオ特徴の計算
            Map<String, Float> maxAudioFeatures = AudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);
            Map<String, Float> minAudioFeatures = AudioFeaturesCalculator.calculateMinAudioFeatures(trackList);
            Map<String, Float> averageAudioFeatures = AudioFeaturesCalculator.calculateAverageAudioFeatures(trackList);

            // 上位アーティストの取得
            List<String> seedArtists = playlistAnalyticsService.getTop5ArtistsForPlaylist(id);

            logAudioFeatures(maxAudioFeatures, minAudioFeatures, averageAudioFeatures);

            long totalDuration = calculateTotalDuration(tracks);

            return createResponse(trackList, playlistName, owner, maxAudioFeatures, minAudioFeatures, averageAudioFeatures, totalDuration, seedArtists);

        } catch (ResourceNotFoundException e) {
            throw e;
        } catch (Exception e) {
            logger.error("プレイリストの詳細情報の取得中に予期しないエラーが発生しました。", e);
            throw new InvalidRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "プレイリストの詳細情報の取得中にエラーが発生しました。", e);
        }
    }

    /**
     * オーディオ特徴をログに出力する
     */
    private void logAudioFeatures(Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> averageAudioFeatures) {
        logger.info("getPlaylistDetails: 最大AudioFeatures: {}", maxAudioFeatures);
        logger.info("getPlaylistDetails: 最小AudioFeatures: {}", minAudioFeatures);
        logger.info("getPlaylistDetails: 平均AudioFeatures: {}", averageAudioFeatures);
    }

    /**
     * プレイリストの総再生時間を計算する
     *
     * @param tracks プレイリストのトラック配列
     * @return 総再生時間（ミリ秒）
     */
    public long calculateTotalDuration(PlaylistTrack[] tracks) {
        long totalDuration = 0;
        for (PlaylistTrack track : tracks) {
            totalDuration += track.getTrack().getDurationMs();
        }
        return totalDuration;
    }

    /**
     * レスポンス用のMapを作成する
     */
    private Map<String, Object> createResponse(List<Map<String, Object>> trackList, String playlistName, User owner, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> averageAudioFeatures, long totalDuration, final List<String> seedArtists) {
        Map<String, Object> response = new HashMap<>();
        response.put("tracks", Map.of("items", trackList));
        response.put("playlistName", playlistName);
        response.put("ownerId", owner.getId());
        response.put("ownerName", owner.getDisplayName());
        response.put("maxAudioFeatures", maxAudioFeatures);
        response.put("minAudioFeatures", minAudioFeatures);
        response.put("averageAudioFeatures", averageAudioFeatures);
        response.put("totalDuration", totalDuration);
        response.put("seedArtists", seedArtists);
        return response;
    }
}
