package com.github.oosm032519.playlistviewernext.service.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;

import java.io.IOException;
import java.util.List;

@Service
public class SpotifyUserPlaylistCreationService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyUserPlaylistCreationService.class);

    @Autowired
    private SpotifyApi spotifyApi;

    public String createPlaylist(String accessToken, String userId, String playlistName, List<String> trackIds) throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        logger.info("SpotifyUserPlaylistCreationService.createPlaylist() が呼び出されました。");
        logger.info("accessToken: {}", accessToken);
        logger.info("userId: {}", userId);
        logger.info("playlistName: {}", playlistName);
        logger.info("trackIds: {}", trackIds);

        spotifyApi.setAccessToken(accessToken);

        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName)
                .public_(false)
                .build();

        Playlist playlist = createPlaylistRequest.execute();
        String playlistId = playlist.getId();
        logger.info("playlistId: {}", playlistId);

        List<String> trackUris = trackIds.stream()
                .map(id -> "spotify:track:" + id)
                .toList();
        logger.info("trackUris: {}", trackUris);

        if (!trackUris.isEmpty()) {
            AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi.addItemsToPlaylist(playlistId, trackUris.toArray(new String[0]))
                    .build();
            addItemsToPlaylistRequest.execute();
            logger.info("トラックをプレイリストに追加しました。");
        }

        logger.info("プレイリストの作成が完了しました。");
        return playlistId;
    }
}
