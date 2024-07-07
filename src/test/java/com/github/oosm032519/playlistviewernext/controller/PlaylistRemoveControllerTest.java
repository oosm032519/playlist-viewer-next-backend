package com.github.oosm032519.playlistviewernext.controller;

import com.github.oosm032519.playlistviewernext.model.RemoveTrackRequest;
import com.github.oosm032519.playlistviewernext.service.PlaylistRemoveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.core.user.OAuth2User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PlaylistRemoveControllerTest {

    @Mock
    private PlaylistRemoveService playlistRemoveService;

    @InjectMocks
    private PlaylistRemoveController playlistRemoveController;

    private RemoveTrackRequest removeTrackRequest;

    @BeforeEach
    void setUp() {
        removeTrackRequest = new RemoveTrackRequest();
        removeTrackRequest.setPlaylistId("playlistId");
        removeTrackRequest.setTrackId("trackId");
    }

    @Nested
    @DisplayName("removeTrackFromPlaylist method tests")
    class RemoveTrackFromPlaylistTests {

        @Test
        @DisplayName("Should return unauthorized when principal is null")
        void shouldReturnUnauthorizedWhenPrincipalIsNull() {
            ResponseEntity<String> response = playlistRemoveController.removeTrackFromPlaylist(removeTrackRequest, null);

            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            assertThat(response.getBody()).isEqualTo("認証が必要です。");
        }

        @Test
        @DisplayName("Should delegate to PlaylistRemoveService")
        void shouldDelegateToPlaylistService() {
            OAuth2User principal = mock(OAuth2User.class);
            ResponseEntity<String> expectedResponse = ResponseEntity.ok("トラックが正常に削除されました。");
            when(playlistRemoveService.removeTrackFromPlaylist(removeTrackRequest, principal)).thenReturn(expectedResponse);

            ResponseEntity<String> response = playlistRemoveController.removeTrackFromPlaylist(removeTrackRequest, principal);

            assertThat(response).isEqualTo(expectedResponse);
            verify(playlistRemoveService).removeTrackFromPlaylist(removeTrackRequest, principal);
        }
    }
}
