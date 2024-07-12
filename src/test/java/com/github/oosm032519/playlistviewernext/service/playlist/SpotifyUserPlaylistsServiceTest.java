package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistSimplified;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyUserPlaylistsServiceTest {

    @Mock
    private SpotifyApi spotifyApi;

    @Mock
    private SpotifyAuthService authService;

    @InjectMocks
    private SpotifyUserPlaylistsService userPlaylistsService;

    @BeforeEach
    void setUp() {
        // 初期化メソッドは必要ないため削除
    }

    @Test
    void testGetCurrentUsersPlaylists_withPlaylists() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        GetListOfCurrentUsersPlaylistsRequest.Builder builder = mock(GetListOfCurrentUsersPlaylistsRequest.Builder.class);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = mock(GetListOfCurrentUsersPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistsPaging = mock(Paging.class);
        PlaylistSimplified[] playlistSimplifieds = new PlaylistSimplified[]{mock(PlaylistSimplified.class)};

        doNothing().when(authService).setAccessToken(authentication);
        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(builder);
        when(builder.build()).thenReturn(playlistsRequest);
        when(playlistsRequest.execute()).thenReturn(playlistsPaging);
        when(playlistsPaging.getItems()).thenReturn(playlistSimplifieds);

        // Act
        List<PlaylistSimplified> result = userPlaylistsService.getCurrentUsersPlaylists(authentication);

        // Assert
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(playlistSimplifieds[0]);
    }

    @Test
    void testGetCurrentUsersPlaylists_noPlaylists() throws IOException, SpotifyWebApiException, ParseException {
        // Arrange
        OAuth2AuthenticationToken authentication = mock(OAuth2AuthenticationToken.class);
        GetListOfCurrentUsersPlaylistsRequest.Builder builder = mock(GetListOfCurrentUsersPlaylistsRequest.Builder.class);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = mock(GetListOfCurrentUsersPlaylistsRequest.class);
        Paging<PlaylistSimplified> playlistsPaging = mock(Paging.class);

        doNothing().when(authService).setAccessToken(authentication);
        when(spotifyApi.getListOfCurrentUsersPlaylists()).thenReturn(builder);
        when(builder.build()).thenReturn(playlistsRequest);
        when(playlistsRequest.execute()).thenReturn(playlistsPaging);
        when(playlistsPaging.getItems()).thenReturn(null);

        // Act
        List<PlaylistSimplified> result = userPlaylistsService.getCurrentUsersPlaylists(authentication);

        // Assert
        assertThat(result).isEmpty();
    }
}
