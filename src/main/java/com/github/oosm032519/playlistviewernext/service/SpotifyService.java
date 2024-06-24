// SpotifyService.java

package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class SpotifyService {

    @Autowired
    private SpotifyApi spotifyApi;

    public void getAccessToken() throws IOException, SpotifyWebApiException, ParseException {
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        ClientCredentials clientCredentials = clientCredentialsRequest.execute();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
    }

    public List<PlaylistSimplified> searchPlaylists(String query) throws IOException, SpotifyWebApiException, ParseException {
        SearchPlaylistsRequest searchPlaylistsRequest = spotifyApi.searchPlaylists(query).build();
        Paging<PlaylistSimplified> playlistSimplifiedPaging = searchPlaylistsRequest.execute();
        return Arrays.asList(playlistSimplifiedPaging.getItems());
    }

    public PlaylistTrack[] getPlaylistTracks(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId)
                // .fields("tracks.items(track(name, artists(name), album(name, images)))")
                .build();
        Playlist playlist = getPlaylistRequest.execute();
        return playlist.getTracks().getItems();
    }
}
