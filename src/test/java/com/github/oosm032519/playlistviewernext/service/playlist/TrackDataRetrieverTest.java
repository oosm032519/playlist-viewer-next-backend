package com.github.oosm032519.playlistviewernext.service.playlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class TrackDataRetrieverTest {

    @InjectMocks
    private TrackDataRetriever trackDataRetriever;

    @BeforeEach
    public void setUp() {
        Track mockTrack = mock(Track.class);
        when(mockTrack.getId()).thenReturn("testTrackId");

        PlaylistTrack mockPlaylistTrack = mock(PlaylistTrack.class);
        when(mockPlaylistTrack.getTrack()).thenReturn(mockTrack);

    }
}
