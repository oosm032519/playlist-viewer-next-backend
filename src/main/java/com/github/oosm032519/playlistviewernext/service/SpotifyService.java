// SpotifyService.java

package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

@Service
public class SpotifyService {

    private static final Logger logger = Logger.getLogger(SpotifyService.class.getName());

    @Autowired
    private SpotifyApi spotifyApi;

    public void getAccessToken() throws IOException, SpotifyWebApiException, ParseException {
        logger.info("アクセストークンの取得を開始します。");
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        logger.info("ClientCredentialsRequestを作成しました。");
        ClientCredentials clientCredentials = clientCredentialsRequest.execute();
        logger.info("ClientCredentialsRequestを実行し、ClientCredentialsを取得しました。");
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        logger.info("アクセストークンを設定しました: " + clientCredentials.getAccessToken());
    }

    public List<PlaylistSimplified> searchPlaylists(String query) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストの検索を開始します。検索クエリ: " + query);
        SearchPlaylistsRequest searchPlaylistsRequest = spotifyApi.searchPlaylists(query).build();
        logger.info("SearchPlaylistsRequestを作成しました。");
        Paging<PlaylistSimplified> playlistSimplifiedPaging = searchPlaylistsRequest.execute();
        logger.info("SearchPlaylistsRequestを実行し、結果を取得しました。");
        List<PlaylistSimplified> playlists = Arrays.asList(playlistSimplifiedPaging.getItems());
        logger.info("検索結果: " + playlists.size() + "件のプレイリストが見つかりました。");
        return playlists;
    }

    public PlaylistTrack[] getPlaylistTracks(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのトラック取得を開始します。プレイリストID: " + playlistId);
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId)
                // .fields("tracks.items(track(name, artists(name), album(name, images)))")
                .build();
        logger.info("GetPlaylistRequestを作成しました。");
        Playlist playlist = getPlaylistRequest.execute();
        logger.info("GetPlaylistRequestを実行し、プレイリスト情報を取得しました。");
        PlaylistTrack[] tracks = playlist.getTracks().getItems();
        logger.info("プレイリストのトラック数: " + tracks.length);
        return tracks;
    }
}
