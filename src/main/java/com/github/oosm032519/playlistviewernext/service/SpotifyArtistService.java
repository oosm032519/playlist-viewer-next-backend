package com.github.oosm032519.playlistviewernext.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Service
public class SpotifyArtistService {
    private final SpotifyApi spotifyApi;

    @Autowired
    public SpotifyArtistService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    public List<String> getArtistGenres(String artistId) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        GetArtistRequest getArtistRequest = spotifyApi.getArtist(artistId).build();
        Artist artist = getArtistRequest.execute();
        return artist.getGenres() != null ? Arrays.asList(artist.getGenres()) : Collections.emptyList();
    }
}
