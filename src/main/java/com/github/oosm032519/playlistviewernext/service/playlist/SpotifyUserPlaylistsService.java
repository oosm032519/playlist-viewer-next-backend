package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class SpotifyUserPlaylistsService {
    private final SpotifyApi spotifyApi;

    @Autowired
    public SpotifyUserPlaylistsService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public List<PlaylistSimplified> getCurrentUsersPlaylists() {
        try {
            OAuth2User oauth2User = (OAuth2User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            String spotifyAccessToken = oauth2User.getAttribute("spotify_access_token");

            if (spotifyAccessToken == null) {
                throw new AuthenticationException(
                        HttpStatus.UNAUTHORIZED,
                        "AUTHENTICATION_ERROR",
                        "Spotify access token is missing"
                );
            }

            spotifyApi.setAccessToken(spotifyAccessToken);
            return getPlaylists();
        } catch (AuthenticationException e) {
            throw e;
        } catch (Exception e) {
            throw new SpotifyApiException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "PLAYLISTS_RETRIEVAL_ERROR",
                    "Error occurred while retrieving playlists",
                    e
            );
        }
    }

    private List<PlaylistSimplified> getPlaylists() throws Exception {
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();
        Paging<PlaylistSimplified> playlistsPaging = playlistsRequest.execute();
        return Optional.ofNullable(playlistsPaging.getItems())
                .map(Arrays::asList)
                .orElse(Collections.emptyList());
    }
}
