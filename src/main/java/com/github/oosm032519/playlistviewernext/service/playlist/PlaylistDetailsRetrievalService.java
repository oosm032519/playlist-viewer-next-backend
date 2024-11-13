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
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistDetailsRetrievalService {

    private static final Logger logger = LoggerFactory.getLogger(PlaylistDetailsRetrievalService.class);

    private final SpotifyPlaylistDetailsService playlistDetailsService;
    private final SpotifyClientCredentialsAuthentication authController;
    private final TrackDataRetriever trackDataRetriever;
    private final SpotifyPlaylistAnalyticsService playlistAnalyticsService;

    @Autowired
    public PlaylistDetailsRetrievalService(
            SpotifyPlaylistDetailsService playlistDetailsService,
            SpotifyClientCredentialsAuthentication authController,
            TrackDataRetriever trackDataRetriever,
            SpotifyPlaylistAnalyticsService playlistAnalyticsService,
            SpotifyRecommendationService trackRecommendationService) {
        this.playlistDetailsService = playlistDetailsService;
        this.authController = authController;
        this.trackDataRetriever = trackDataRetriever;
        this.playlistAnalyticsService = playlistAnalyticsService;
    }

    public Map<String, Object> getPlaylistDetails(String id) {
        logger.info("getPlaylistDetails: プレイリストID: {}", id);

        try {
            authController.authenticate();

            Playlist playlist = playlistDetailsService.getPlaylist(id);
            if (playlist == null) {
                throw new ResourceNotFoundException(
                        HttpStatus.NOT_FOUND,
                        "指定されたプレイリストが見つかりません。"
                );
            }

            String playlistName = playlist.getName();
            User owner = playlist.getOwner();

            PlaylistTrack[] tracks = playlistDetailsService.getPlaylistTracks(id);
            List<Map<String, Object>> trackList = trackDataRetriever.getTrackListData(tracks);

            Map<String, Float> maxAudioFeatures = AudioFeaturesCalculator.calculateMaxAudioFeatures(trackList);
            Map<String, Float> minAudioFeatures = AudioFeaturesCalculator.calculateMinAudioFeatures(trackList);
            Map<String, Float> averageAudioFeatures = AudioFeaturesCalculator.calculateAverageAudioFeatures(trackList);


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

    public long calculateTotalDuration(PlaylistTrack[] tracks) {
        long totalDuration = 0;
        for (PlaylistTrack track : tracks) {
            totalDuration += track.getTrack().getDurationMs();
        }
        return totalDuration;
    }

    private void logAudioFeatures(Map<String, Float> maxAudioFeatures, Map<String, Float> minAudioFeatures, Map<String, Float> averageAudioFeatures) {
        logger.info("getPlaylistDetails: 最大AudioFeatures: {}", maxAudioFeatures);
        logger.info("getPlaylistDetails: 最小AudioFeatures: {}", minAudioFeatures);
        logger.info("getPlaylistDetails: 平均AudioFeatures: {}", averageAudioFeatures);
    }

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
