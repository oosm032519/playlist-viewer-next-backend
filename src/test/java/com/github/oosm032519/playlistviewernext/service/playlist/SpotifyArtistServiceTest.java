package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.SpotifyApiException;
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
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyArtistServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @InjectMocks
    private SpotifyArtistService artistService;

    private GetArtistRequest getArtistRequest;
    private Artist artist;

    @BeforeEach
    void setUp() {
        GetArtistRequest.Builder builder = mock(GetArtistRequest.Builder.class);
        getArtistRequest = mock(GetArtistRequest.class);
        artist = mock(Artist.class);

        when(spotifyApi.getArtist(anyString())).thenReturn(builder);
        when(builder.build()).thenReturn(getArtistRequest);
    }

    @Test
    void getArtistGenres_shouldReturnGenres_whenArtistHasGenres() throws IOException, SpotifyWebApiException, ParseException {
        String artistId = "test-artist-id";
        String[] genres = {"pop", "rock"};

        when(getArtistRequest.execute()).thenReturn(artist);
        when(artist.getGenres()).thenReturn(genres);

        List<String> result = artistService.getArtistGenres(artistId);

        assertThat(result).containsExactly("pop", "rock");
    }

    @Test
    void getArtistGenres_shouldReturnEmptyList_whenArtistHasNoGenres() throws IOException, SpotifyWebApiException, ParseException {
        String artistId = "test-artist-id";

        when(getArtistRequest.execute()).thenReturn(artist);
        when(artist.getGenres()).thenReturn(null);

        List<String> result = artistService.getArtistGenres(artistId);

        assertThat(result).isEmpty();
    }

    @Test
    void getArtistGenres_shouldThrowException_whenArtistNotFound() throws IOException, SpotifyWebApiException, ParseException {
        String artistId = "non-existent-artist-id";

        when(getArtistRequest.execute()).thenThrow(new SpotifyWebApiException("Artist not found"));

        assertThatThrownBy(() -> artistService.getArtistGenres(artistId))
                .isInstanceOf(SpotifyApiException.class)
                .hasMessageContaining("アーティスト情報の取得中にエラーが発生しました。");
    }

    @Test
    void getArtistGenres_shouldReturnAllGenres_whenArtistHasMultipleGenres() throws IOException, SpotifyWebApiException, ParseException {
        String artistId = "test-artist-id";
        String[] genres = {"pop", "rock", "indie", "alternative"};

        when(getArtistRequest.execute()).thenReturn(artist);
        when(artist.getGenres()).thenReturn(genres);

        List<String> result = artistService.getArtistGenres(artistId);

        assertThat(result).containsExactly("pop", "rock", "indie", "alternative");
    }
}
