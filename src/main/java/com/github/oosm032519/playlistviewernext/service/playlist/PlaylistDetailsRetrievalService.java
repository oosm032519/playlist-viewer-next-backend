package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.controller.auth.SpotifyClientCredentialsAuthentication;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
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
        logger.info("PlaylistDetailsRetrievalService constructor started.");
        this.playlistDetailsService = playlistDetailsService;
        this.authController = authController;
        this.trackDataRetriever = trackDataRetriever;
        this.playlistAnalyticsService = playlistAnalyticsService;
        logger.info("PlaylistDetailsRetrievalService constructor finished.");
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

        Map<String, Object> playlistDetails = new HashMap<>();
        try {
            logger.info("getPlaylistDetails: モックモード有効か: {}", mockEnabled);
            // モックモードが有効な場合は認証をスキップ
            if (!mockEnabled && authController != null) {
                logger.info("getPlaylistDetails: Spotify API 認証開始");
                // Spotify APIの認証を行う
                authController.authenticate();
                logger.info("getPlaylistDetails: Spotify API 認証完了");
            } else {
                logger.info("getPlaylistDetails: モックモード有効、認証スキップ");
            }

            logger.info("getPlaylistDetails: プレイリスト情報取得開始, プレイリストID: {}", id);
            // プレイリスト情報の取得
            Playlist playlist = playlistDetailsService.getPlaylist(id);
            logger.info("getPlaylistDetails: プレイリスト情報取得完了, プレイリスト: {}", playlist);

            if (playlist == null) {
                logger.warn("getPlaylistDetails: プレイリストが見つかりません, プレイリストID: {}", id);
                throw new ResourceNotFoundException(
                        HttpStatus.NOT_FOUND,
                        "指定されたプレイリストが見つかりません。"
                );
            }

            String playlistName = playlist.getName();
            User owner = playlist.getOwner();
            logger.info("getPlaylistDetails: プレイリスト名: {}, オーナーID: {}, オーナー名: {}", playlistName, owner.getId(), owner.getDisplayName());

            logger.info("getPlaylistDetails: トラック情報取得開始, プレイリストID: {}", id);
            // トラック情報の取得と解析
            PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);
            logger.info("getPlaylistDetails: トラック情報取得完了, トラック数: {}", tracks != null ? tracks.length : 0);
            List<Map<String, Object>> trackList = trackDataRetriever.getTrackListData(tracks);
            logger.info("getPlaylistDetails: トラックデータリスト取得完了, トラックリストサイズ: {}", trackList.size());


            logger.info("getPlaylistDetails: オーディオ特徴量計算開始");
            // オーディオ特徴の計算
            Map<String, Float> maxAudioFeatures = AudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);
            Map<String, Float> minAudioFeatures = AudioFeaturesCalculator.calculateMinAudioFeatures(trackList);
            Map<String, Float> averageAudioFeatures = AudioFeaturesCalculator.calculateAverageAudioFeatures(trackList);
            logger.info("getPlaylistDetails: オーディオ特徴量計算完了");

            // 上位アーティストの取得
            logger.info("getPlaylistDetails: 上位アーティスト取得開始");
            List<String> seedArtists = playlistAnalyticsService.getTop5ArtistsForPlaylist(id);
            logger.info("getPlaylistDetails: 上位アーティスト取得完了, seedArtists: {}", seedArtists);

            logAudioFeatures(maxAudioFeatures, minAudioFeatures, averageAudioFeatures);

            long totalDuration = calculateTotalDuration(tracks);

            playlistDetails = createResponse(trackList, playlistName, owner, maxAudioFeatures, minAudioFeatures, averageAudioFeatures, totalDuration, seedArtists);
            logger.info("getPlaylistDetails: レスポンス作成完了, プレイリストID: {}", id);


        } catch (ResourceNotFoundException e) {
            logger.warn("getPlaylistDetails: ResourceNotFoundException: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            logger.error("getPlaylistDetails: プレイリストの詳細情報の取得中に予期しないエラーが発生しました。", e);
            logger.error("getPlaylistDetails: エラー詳細: ", e);
            throw new InvalidRequestException(HttpStatus.INTERNAL_SERVER_ERROR, "プレイリストの詳細情報の取得中にエラーが発生しました。", e);
        }
        return playlistDetails;
    }

    /**
     * オーディオ特徴をログに出力する
     */
    private void logAudioFeatures(Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> averageAudioFeatures) {
        logger.info("logAudioFeatures: 最大AudioFeatures: {}", maxAudioFeatures);
        logger.info("logAudioFeatures: 最小AudioFeatures: {}", minAudioFeatures);
        logger.info("logAudioFeatures: 平均AudioFeatures: {}", averageAudioFeatures);
    }

    /**
     * プレイリストの総再生時間を計算する
     *
     * @param tracks プレイリストのトラック配列
     * @return 総再生時間（ミリ秒）
     */
    private long calculateTotalDuration(PlaylistTrack[] tracks) {
        long totalDuration = 0;
        if (tracks != null) {
            for (PlaylistTrack track : tracks) {
                totalDuration += track.getTrack().getDurationMs();
            }
        }
        logger.info("calculateTotalDuration: 総再生時間: {}ms", totalDuration);
        return totalDuration;
    }

    /**
     * レスポンス用のMapを作成する
     */
    private Map<String, Object> createResponse(List<Map<String, Object>> trackList, String playlistName, User owner, Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> averageAudioFeatures, long totalDuration, final List<String> seedArtists) {
        logger.info("createResponse: レスポンス作成開始");
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
        logger.info("createResponse: レスポンス: {}", response);
        logger.info("createResponse: レスポンス作成完了");
        return response;
    }
}
