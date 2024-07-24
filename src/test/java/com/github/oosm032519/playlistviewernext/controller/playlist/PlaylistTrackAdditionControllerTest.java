package com.github.oosm032519.playlistviewernext.controller.playlist;

import com.github.oosm032519.playlistviewernext.model.PlaylistTrackAdditionRequest;
import com.github.oosm032519.playlistviewernext.service.playlist.SpotifyPlaylistTrackAdditionService;
import org.apache.hc.core5.http.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.special.SnapshotResult;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class PlaylistTrackAdditionControllerTest {

    @Mock
    private SpotifyPlaylistTrackAdditionService spotifyService;

    @InjectMocks
    private PlaylistTrackAdditionController controller;

    private MockHttpSession session;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        session = new MockHttpSession();
    }

    @Test
    public void testAddTrackToPlaylist_Success() throws Exception {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId("playlist123");
        request.setTrackId("track123");
        session.setAttribute("accessToken", "validToken");

        SnapshotResult snapshotResult = mock(SnapshotResult.class);
        when(snapshotResult.getSnapshotId()).thenReturn("snapshot123");
        when(spotifyService.addTrackToPlaylist(anyString(), anyString(), anyString())).thenReturn(snapshotResult);

        // Act
        ResponseEntity<String> response = controller.addTrackToPlaylist(request, session);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("snapshot123");
        verify(spotifyService, times(1)).addTrackToPlaylist("validToken", "playlist123", "track123");
    }

    @Test
    public void testAddTrackToPlaylist_Unauthorized() throws IOException, ParseException, SpotifyWebApiException {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId("playlist123");
        request.setTrackId("track123");

        // Act
        ResponseEntity<String> response = controller.addTrackToPlaylist(request, session);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo("認証が必要です。");
        verify(spotifyService, times(0)).addTrackToPlaylist(anyString(), anyString(), anyString());
    }

    @Test
    public void testAddTrackToPlaylist_InternalServerError() throws Exception {
        // Arrange
        PlaylistTrackAdditionRequest request = new PlaylistTrackAdditionRequest();
        request.setPlaylistId("playlist123");
        request.setTrackId("track123");
        session.setAttribute("accessToken", "validToken");

        when(spotifyService.addTrackToPlaylist(anyString(), anyString(), anyString()))
                .thenThrow(new IOException("IO Error"));

        // Act
        ResponseEntity<String> response = controller.addTrackToPlaylist(request, session);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).contains("エラー: IO Error");
        verify(spotifyService, times(1)).addTrackToPlaylist("validToken", "playlist123", "track123");
    }
}
