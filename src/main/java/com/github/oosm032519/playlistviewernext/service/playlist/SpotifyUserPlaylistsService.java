package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SpotifyUserPlaylistsService {
    private final SpotifyApi spotifyApi;
    private final SpotifyAuthService authService;

    @Autowired
    public SpotifyUserPlaylistsService(SpotifyApi spotifyApi, SpotifyAuthService authService) {
        this.spotifyApi = spotifyApi;
        this.authService = authService;
    }

    public List<PlaylistSimplified> getCurrentUsersPlaylists(OAuth2AuthenticationToken authentication) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        authService.setAccessToken(authentication);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();
        Paging<PlaylistSimplified> playlistsPaging = playlistsRequest.execute();
        PlaylistSimplified[] items = playlistsPaging.getItems();
        return items != null ? Arrays.asList(items) : Collections.emptyList();
    }
}
