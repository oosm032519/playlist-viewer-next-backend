package com.github.oosm032519.playlistviewernext.service.analytics;

import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistAnalyticsService;
import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistAnalyticsService {

    @Autowired
    private SpotifyPlaylistAnalyticsService analyticsService;

    public Map<String, Integer> getGenreCountsForPlaylist(String id) throws IOException, ParseException, SpotifyWebApiException {
        return analyticsService.getGenreCountsForPlaylist(id);
    }

    public List<String> getTop5GenresForPlaylist(String id) throws IOException, ParseException, SpotifyWebApiException {
        return analyticsService.getTop5GenresForPlaylist(id);
    }
}