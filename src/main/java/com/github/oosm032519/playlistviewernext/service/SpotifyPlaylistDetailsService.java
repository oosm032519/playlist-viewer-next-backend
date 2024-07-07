package com.github.oosm032519.playlistviewernext.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.User;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;

import java.io.IOException;

@Service
public class SpotifyPlaylistDetailsService {
    private final SpotifyApi spotifyApi;

    @Autowired
    public SpotifyPlaylistDetailsService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public PlaylistTrack[] getPlaylistTracks(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        return getPlaylistRequest.execute().getTracks().getItems();
    }

    public String getPlaylistName(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        return getPlaylistRequest.execute().getName();
    }

    public User getPlaylistOwner(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        return getPlaylistRequest.execute().getOwner();
    }
}
