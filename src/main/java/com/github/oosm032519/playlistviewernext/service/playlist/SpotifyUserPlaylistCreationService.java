package com.github.oosm032519.playlistviewernext.service.playlist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.requests.data.playlists.CreatePlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.AddItemsToPlaylistRequest;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Spotify APIを使用してユーザーのプレイリストを作成するサービスクラス。
 */
@Service
public class SpotifyUserPlaylistCreationService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyUserPlaylistCreationService.class);
    private static final String SPOTIFY_TRACK_URI_PREFIX = "spotify:track:";

    private final SpotifyApi spotifyApi;

    public SpotifyUserPlaylistCreationService(SpotifyApi spotifyApi) {
        this.spotifyApi = spotifyApi;
    }

    /**
     * 新しいプレイリストを作成し、指定されたトラックを追加します。
     *
     * @param accessToken  Spotify APIへのアクセスに使用するトークン
     * @param userId       プレイリストを作成するユーザーのID
     * @param playlistName 作成するプレイリストの名前
     * @param trackIds     プレイリストに追加するトラックのIDリスト
     * @return 作成されたプレイリストのID
     * @throws IOException                             入出力例外が発生した場合
     * @throws SpotifyWebApiException                  Spotify APIの例外が発生した場合
     * @throws org.apache.hc.core5.http.ParseException HTTPレスポンスの解析例外が発生した場合
     */
    public String createPlaylist(String accessToken, String userId, String playlistName, List<String> trackIds)
            throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        logMethodCall(accessToken, userId, playlistName, trackIds);

        spotifyApi.setAccessToken(accessToken);

        String playlistId = createSpotifyPlaylist(userId, playlistName);
        addTracksToPlaylist(playlistId, trackIds);

        logger.info("プレイリストの作成が完了しました。");
        return playlistId;
    }

    private void logMethodCall(String accessToken, String userId, String playlistName, List<String> trackIds) {
        logger.info("SpotifyUserPlaylistCreationService.createPlaylist() が呼び出されました。");
        logger.info("accessToken: {}", accessToken);
        logger.info("userId: {}", userId);
        logger.info("playlistName: {}", playlistName);
        logger.info("trackIds: {}", trackIds);
    }

    private String createSpotifyPlaylist(String userId, String playlistName)
            throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        CreatePlaylistRequest createPlaylistRequest = spotifyApi.createPlaylist(userId, playlistName)
                .public_(false)
                .build();

        Playlist playlist = createPlaylistRequest.execute();
        String playlistId = playlist.getId();
        logger.info("playlistId: {}", playlistId);
        return playlistId;
    }

    private void addTracksToPlaylist(String playlistId, List<String> trackIds)
            throws IOException, SpotifyWebApiException, org.apache.hc.core5.http.ParseException {
        if (trackIds.isEmpty()) {
            return;
        }

        List<String> trackUris = convertToSpotifyUris(trackIds);
        logger.info("trackUris: {}", trackUris);

        AddItemsToPlaylistRequest addItemsToPlaylistRequest = spotifyApi.addItemsToPlaylist(playlistId, trackUris.toArray(new String[0]))
                .build();
        addItemsToPlaylistRequest.execute();
        logger.info("トラックをプレイリストに追加しました。");
    }

    private List<String> convertToSpotifyUris(List<String> trackIds) {
        return trackIds.stream()
                .map(id -> SPOTIFY_TRACK_URI_PREFIX + id)
                .collect(Collectors.toList());
    }
}
