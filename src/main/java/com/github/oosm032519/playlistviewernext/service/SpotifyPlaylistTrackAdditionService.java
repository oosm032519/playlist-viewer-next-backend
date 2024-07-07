package com.github.oosm032519.playlistviewernext.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;
import org.apache.hc.core5.http.ParseException;

import java.io.IOException;

@Service
public class SpotifyPlaylistTrackAdditionService {

    @Autowired
    private SpotifyApi spotifyApi;

    public SnapshotResult addTrackToPlaylist(String accessToken, String playlistId, String trackId) throws IOException, SpotifyWebApiException, ParseException {
        spotifyApi.setAccessToken(accessToken);

        String[] uris = new String[]{"spotify:track:" + trackId};
        AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi.addItemsToPlaylist(playlistId, uris).build();

        return addItemsToPlaylistRequest.execute();
    }
}
