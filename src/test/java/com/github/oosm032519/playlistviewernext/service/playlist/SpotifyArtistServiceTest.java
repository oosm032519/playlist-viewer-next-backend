package com.github.oosm032519.playlistviewernext.service.playlist;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.artists.GetSeveralArtistsRequest;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SpotifyArtistServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @InjectMocks
    private SpotifyArtistService artistService;

    private GetSeveralArtistsRequest getSeveralArtistsRequest;
    private Artist artist;


    @BeforeEach
    void setUp() throws ParseException, IOException, SpotifyWebApiException {
        GetSeveralArtistsRequest.Builder builder = mock(GetSeveralArtistsRequest.Builder.class);
        getSeveralArtistsRequest = mock(GetSeveralArtistsRequest.class);
        artist = mock(Artist.class);

        when(spotifyApi.getSeveralArtists(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(getSeveralArtistsRequest);
        when(getSeveralArtistsRequest.execute()).thenReturn(new Artist[]{artist});
    }

    @Test
    void getArtistGenres_shouldReturnGenres_whenArtistHasGenres() throws SpotifyWebApiException {
        String artistId = "test-artist-id";
        String[] genres = {"pop", "rock"};

        when(artist.getGenres()).thenReturn(genres);
        when(artist.getId()).thenReturn(artistId);

        Map<String, List<String>> result = artistService.getArtistGenres(Collections.singletonList(artistId));

        assertThat(result.get(artistId)).containsExactly("pop", "rock");
    }

    @Test
    void getArtistGenres_shouldThrowException_whenArtistNotFound() throws IOException, ParseException, SpotifyWebApiException {
        String artistId = "non-existent-artist-id";

        when(getSeveralArtistsRequest.execute()).thenThrow(new SpotifyWebApiException("Artist not found"));

        assertThatThrownBy(() -> artistService.getArtistGenres(Collections.singletonList(artistId)))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessageContaining("Artist not found");
    }

    @Test
    void getArtistGenres_shouldReturnAllGenres_whenArtistHasMultipleGenres() throws SpotifyWebApiException {
        String artistId = "test-artist-id";
        String[] genres = {"pop", "rock", "indie", "alternative"};

        when(artist.getGenres()).thenReturn(genres);
        when(artist.getId()).thenReturn(artistId);

        Map<String, List<String>> result = artistService.getArtistGenres(Collections.singletonList(artistId));

        assertThat(result.get(artistId)).containsExactly("pop", "rock", "indie", "alternative");
    }
}
