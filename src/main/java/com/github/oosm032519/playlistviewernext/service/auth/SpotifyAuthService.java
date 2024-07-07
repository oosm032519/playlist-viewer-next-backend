package com.github.oosm032519.playlistviewernext.service.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;

@Service
public class SpotifyAuthService {
    private final SpotifyApi spotifyApi;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    public SpotifyAuthService(SpotifyApi spotifyApi, OAuth2AuthorizedClientService authorizedClientService) {
        this.spotifyApi = spotifyApi;
        this.authorizedClientService = authorizedClientService;
    }

    public void setAccessToken(OAuth2AuthenticationToken authentication) {
        String accessToken = authorizedClientService.loadAuthorizedClient("spotify", authentication.getName())
                .getAccessToken().getTokenValue();
        spotifyApi.setAccessToken(accessToken);
    }

    public void getClientCredentialsToken() throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        ClientCredentials clientCredentials = clientCredentialsRequest.execute();
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
    }
}
