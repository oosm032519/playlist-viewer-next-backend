package com.github.oosm032519.playlistviewernext.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SpotifyPlaylistService {
    private final SpotifyApi spotifyApi;
    private final SpotifyAuthService authService;

    @Autowired
    public SpotifyPlaylistService(SpotifyApi spotifyApi, SpotifyAuthService authService) {
        this.spotifyApi = spotifyApi;
        this.authService = authService;
    }

    public List<PlaylistSimplified> searchPlaylists(String query) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        SearchPlaylistsRequest searchPlaylistsRequest = spotifyApi.searchPlaylists(query).limit(20).build();
        Paging<PlaylistSimplified> playlistSimplifiedPaging = searchPlaylistsRequest.execute();
        return playlistSimplifiedPaging.getItems() != null ? Arrays.asList(playlistSimplifiedPaging.getItems()) : Collections.emptyList();
    }

    public PlaylistTrack[] getPlaylistTracks(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        Playlist playlist = getPlaylistRequest.execute();
        return playlist.getTracks().getItems();
    }

    public String getPlaylistName(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        Playlist playlist = getPlaylistRequest.execute();
        return playlist.getName();
    }

    public User getPlaylistOwner(String playlistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        Playlist playlist = getPlaylistRequest.execute();
        return playlist.getOwner();
    }

    public List<PlaylistSimplified> getCurrentUsersPlaylists(OAuth2AuthenticationToken authentication) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        authService.setAccessToken(authentication);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();
        Paging<PlaylistSimplified> playlistsPaging = playlistsRequest.execute();
        return playlistsPaging.getItems() != null ? Arrays.asList(playlistsPaging.getItems()) : Collections.emptyList();
    }
}
