package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;

import java.io.IOException;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SpotifyTrackRecommendationServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @InjectMocks
    private SpotifyRecommendationService spotifyRecommendationService;

    @Test
    void getRecommendations_ShouldReturnRecommendedTracks() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        List<String> seedGenres = Arrays.asList("rock", "pop");
        Track[] mockTracks = createMockTracks();
        Recommendations mockRecommendations = mock(Recommendations.class);
        when(mockRecommendations.getTracks()).thenReturn(mockTracks);

        GetRecommendationsRequest.Builder recommendationsBuilder = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest recommendationsRequest = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.seed_genres(anyString())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.limit(anyInt())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.build()).thenReturn(recommendationsRequest);
        when(recommendationsRequest.execute()).thenReturn(mockRecommendations);

        // Act
        List<Track> result = spotifyRecommendationService.getRecommendations(seedGenres);

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getName()).isEqualTo("Track 1");
        assertThat(result.get(1).getName()).isEqualTo("Track 2");
        verify(recommendationsBuilder).seed_genres("rock,pop");
        verify(recommendationsBuilder).limit(20);
    }

    @Test
    void getRecommendations_ShouldHandleEmptySeedGenres() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        List<String> seedGenres = Collections.emptyList();

        // Act
        List<Track> result = spotifyRecommendationService.getRecommendations(seedGenres);

        // Assert
        assertThat(result).isEmpty();
    }

    @Test
    void getRecommendations_ShouldHandleApiException() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        List<String> seedGenres = Arrays.asList("rock", "pop");
        GetRecommendationsRequest.Builder recommendationsBuilder = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest recommendationsRequest = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.seed_genres(anyString())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.limit(anyInt())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.build()).thenReturn(recommendationsRequest);
        when(recommendationsRequest.execute()).thenThrow(new SpotifyWebApiException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyRecommendationService.getRecommendations(seedGenres))
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessage("API error");
    }

    @Test
    void getRecommendations_ShouldHandleNullRecommendations() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        List<String> seedGenres = Arrays.asList("rock", "pop");
        GetRecommendationsRequest.Builder recommendationsBuilder = mock(GetRecommendationsRequest.Builder.class);
        GetRecommendationsRequest recommendationsRequest = mock(GetRecommendationsRequest.class);

        when(spotifyApi.getRecommendations()).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.seed_genres(anyString())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.limit(anyInt())).thenReturn(recommendationsBuilder);
        when(recommendationsBuilder.build()).thenReturn(recommendationsRequest);
        when(recommendationsRequest.execute()).thenReturn(null);

        // Act
        List<Track> result = spotifyRecommendationService.getRecommendations(seedGenres);

        // Assert
        assertThat(result).isEmpty();
    }

    private Track[] createMockTracks() {
        Track track1 = mock(Track.class);
        Track track2 = mock(Track.class);
        ArtistSimplified artist1 = mock(ArtistSimplified.class);
        ArtistSimplified artist2 = mock(ArtistSimplified.class);

        when(track1.getName()).thenReturn("Track 1");
        when(track2.getName()).thenReturn("Track 2");
        when(track1.getArtists()).thenReturn(new ArtistSimplified[]{artist1});
        when(track2.getArtists()).thenReturn(new ArtistSimplified[]{artist2});
        when(artist1.getName()).thenReturn("Artist 1");
        when(artist2.getName()).thenReturn("Artist 2");

        return new Track[]{track1, track2};
    }
}
