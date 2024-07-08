package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.security.UserAuthenticationService;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyUserPlaylistCreationService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistCreationControllerTest {

    @Mock
    private UserAuthenticationService userAuthenticationService;

    @Mock
    private SpotifyUserPlaylistCreationService spotifyUserPlaylistCreationService;

    @Mock
    private OAuth2User principal;

    @InjectMocks
    private PlaylistCreationController playlistCreationController;

    private List<String> trackIds;
    private String accessToken;
    private String userId;
    private String userName;

    @BeforeEach
    void setUp() {
        trackIds = List.of("track1", "track2", "track3");
        accessToken = "validAccessToken";
        userId = "userId";
        userName = "userName";
    }

    @Test
    void createPlaylist_Success() throws Exception {
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttributes()).thenReturn(Map.of("display_name", userName));
        when(spotifyUserPlaylistCreationService.createPlaylist(any(), any(), any(), any())).thenReturn("playlistId");

        ResponseEntity<String> response = playlistCreationController.createPlaylist(trackIds, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("playlistId");

        verify(userAuthenticationService).getAccessToken(principal);
        verify(spotifyUserPlaylistCreationService).createPlaylist(eq(accessToken), eq(userId), anyString(), eq(trackIds));
    }

    @Test
    void createPlaylist_Unauthorized() throws IOException, ParseException, SpotifyWebApiException {
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(null);

        ResponseEntity<String> response = playlistCreationController.createPlaylist(trackIds, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("認証が必要です。");

        verify(userAuthenticationService).getAccessToken(principal);
        verify(spotifyUserPlaylistCreationService, never()).createPlaylist(any(), any(), any(), any());
    }

    @Test
    void createPlaylist_InternalServerError() throws Exception {
        when(userAuthenticationService.getAccessToken(principal)).thenReturn(accessToken);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttributes()).thenReturn(Map.of("display_name", userName));
        when(spotifyUserPlaylistCreationService.createPlaylist(any(), any(), any(), any())).thenThrow(new RuntimeException("Internal error"));

        ResponseEntity<String> response = playlistCreationController.createPlaylist(trackIds, principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).startsWith("エラー: Internal error");

        verify(userAuthenticationService).getAccessToken(principal);
        verify(spotifyUserPlaylistCreationService).createPlaylist(eq(accessToken), eq(userId), anyString(), eq(trackIds));
    }
}
