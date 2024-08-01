package com.github.oosm032519.playlistviewernext.service.playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SpotifyUserPlaylistsServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private OAuth2User oauth2User;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @Mock
    private GetListOfCurrentUsersPlaylistsRequest.Builder requestBuilder;

    @Mock
    private GetListOfCurrentUsersPlaylistsRequest request;

    @InjectMocks
    private SpotifyUserPlaylistsService spotifyUserPlaylistsService;

    @BeforeEach
    public void setup() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(oauth2User);
        SecurityContextHolder.setContext(securityContext);

        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(requestBuilder);
        when(requestBuilder.build()).thenReturn(request);
    }

    @Test
    public void getCurrentUsersPlaylists_success() throws Exception {
        // Arrange
        String accessToken = "mockAccessToken";
        when(oauth2User.getAttribute("spotify_access_token")).thenReturn(accessToken);

        Paging<PlaylistSimplified> playlistsPaging = mock(Paging.class);
        when(request.execute()).thenReturn(playlistsPaging);
        when(playlistsPaging.getItems()).thenReturn(new PlaylistSimplified[0]);

        // Act
        List<PlaylistSimplified> playlists = spotifyUserPlaylistsService.getCurrentUsersPlaylists();

        // Assert
        assertThat(playlists).isEmpty();
        verify(spotifyApi).setAccessToken(accessToken);
    }

    @Test
    public void getCurrentUsersPlaylists_spotifyApiException() throws Exception {
        // Arrange
        String accessToken = "mockAccessToken";
        when(oauth2User.getAttribute("spotify_access_token")).thenReturn(accessToken);
        when(request.execute()).thenThrow(new SpotifyWebApiException("API error"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyUserPlaylistsService.getCurrentUsersPlaylists())
                .isInstanceOf(SpotifyWebApiException.class)
                .hasMessageContaining("API error");
        verify(spotifyApi).setAccessToken(accessToken);
    }

    @Test
    public void getCurrentUsersPlaylists_ioException() throws Exception {
        // Arrange
        String accessToken = "mockAccessToken";
        when(oauth2User.getAttribute("spotify_access_token")).thenReturn(accessToken);
        when(request.execute()).thenThrow(new IOException("IO error"));

        // Act & Assert
        assertThatThrownBy(() -> spotifyUserPlaylistsService.getCurrentUsersPlaylists())
                .isInstanceOf(IOException.class)
                .hasMessageContaining("IO error");
        verify(spotifyApi).setAccessToken(accessToken);
    }
}
