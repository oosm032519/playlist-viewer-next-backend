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

@Service
public class SpotifyTrackService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyTrackService.class);
    private final SpotifyApi spotifyApi;

    @Autowired
    public SpotifyTrackService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public AudioFeatures getAudioFeaturesForTrack(String trackId) {
        try {
            GetAudioFeaturesForTrackRequest audioFeaturesRequest = spotifyApi.getAudioFeaturesForTrack(trackId).build();
            return audioFeaturesRequest.execute();
        } catch (Exception e) {
            logger.error("Error retrieving audio features for track ID: " + trackId, e);
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "AUDIO_FEATURES_RETRIEVAL_ERROR",
                    "オーディオ特徴の取得中にエラーが発生しました。",
                    e
            );
        }
    }
}
