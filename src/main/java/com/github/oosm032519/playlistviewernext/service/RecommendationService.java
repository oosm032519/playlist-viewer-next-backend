package com.github.oosm032519.playlistviewernext.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.util.ArrayList;
import java.util.List;

@Service
public class RecommendationService {

    @Autowired
    private SpotifyRecommendationService recommendationService;

    public List<Track> getRecommendations(List<String> top5Genres) {
        List<Track> recommendations = new ArrayList<>();
        try {
            if (!top5Genres.isEmpty()) {
                recommendations = recommendationService.getRecommendations(top5Genres);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(RecommendationService.class).error("RecommendationService: Spotify APIの呼び出し中にエラーが発生しました。", e);
        }
        return recommendations;
    }
}
