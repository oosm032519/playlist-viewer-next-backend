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

    @BeforeEach
    void setUp() {
    }

    @Test
    void testGetArtistGenres_正常系_ジャンルあり() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String artistId = "test-artist-id";
        GetArtistRequest.Builder builder = mock(GetArtistRequest.Builder.class);
        GetArtistRequest getArtistRequest = mock(GetArtistRequest.class);
        Artist artist = mock(Artist.class);
        String[] genres = new String[]{"pop", "rock"};

        when(spotifyApi.getArtist(artistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getArtistRequest);
        when(getArtistRequest.execute()).thenReturn(artist);
        when(artist.getGenres()).thenReturn(genres);

        // Act
        List<String> result = artistService.getArtistGenres(artistId);

        // Assert
        assertThat(result).containsExactly("pop", "rock");
    }

    @Test
    void testGetArtistGenres_正常系_ジャンルなし() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String artistId = "test-artist-id";
        GetArtistRequest.Builder builder = mock(GetArtistRequest.Builder.class);
        GetArtistRequest getArtistRequest = mock(GetArtistRequest.class);
        Artist artist = mock(Artist.class);

        when(spotifyApi.getArtist(artistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getArtistRequest);
        when(getArtistRequest.execute()).thenReturn(artist);
        when(artist.getGenres()).thenReturn(null);

        // Act
        List<String> result = artistService.getArtistGenres(artistId);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void testGetArtistGenres_異常系_アーティストが存在しない() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String artistId = "non-existent-artist-id";
        GetArtistRequest.Builder builder = mock(GetArtistRequest.Builder.class);
        GetArtistRequest getArtistRequest = mock(GetArtistRequest.class);

        when(spotifyApi.getArtist(artistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getArtistRequest);
        when(getArtistRequest.execute()).thenThrow(new SpotifyWebApiException("Artist not found"));

        // Act & Assert
        assertThatThrownBy(() -> artistService.getArtistGenres(artistId))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("Artist not found");
    }

    @Test
    void testGetArtistGenres_正常系_複数ジャンル() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        String artistId = "test-artist-id";
        GetArtistRequest.Builder builder = mock(GetArtistRequest.Builder.class);
        GetArtistRequest getArtistRequest = mock(GetArtistRequest.class);
        Artist artist = mock(Artist.class);
        String[] genres = new String[]{"pop", "rock", "indie", "alternative"};

        when(spotifyApi.getArtist(artistId)).thenReturn(builder);
        when(builder.build()).thenReturn(getArtistRequest);
        when(getArtistRequest.execute()).thenReturn(artist);
        when(artist.getGenres()).thenReturn(genres);

        // Act
        List<String> result = artistService.getArtistGenres(artistId);

        // Assert
        assertThat(result).containsExactly("pop", "rock", "indie", "alternative");
    }
}
