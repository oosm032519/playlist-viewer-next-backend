package com.github.oosm032519.playlistviewernext.service;

import org.apache.hc.core5.http.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;
import se.michaelthelin.spotify.requests.data.artists.GetArtistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

import java.io.IOException;
import java.util.*;

@Service
public class SpotifyService {
    private static final Logger logger = LoggerFactory.getLogger(SpotifyService.class);
    private final SpotifyApi spotifyApi;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Autowired
    public SpotifyService(SpotifyApi spotifyApi, OAuth2AuthorizedClientService authorizedClientService) {
        this.spotifyApi = spotifyApi;
        this.authorizedClientService = authorizedClientService;
    }

    private void setAccessToken(OAuth2AuthenticationToken authentication) {
        String accessToken = authorizedClientService.loadAuthorizedClient("spotify", authentication.getName())
                .getAccessToken().getTokenValue();
        spotifyApi.setAccessToken(accessToken);
    }

    public void getClientCredentialsToken() throws IOException, SpotifyWebApiException, ParseException {
        logger.info("アクセストークンの取得を開始します。");
        ClientCredentialsRequest clientCredentialsRequest = spotifyApi.clientCredentials().build();
        logger.info("ClientCredentialsRequestを作成しました。");
        ClientCredentials clientCredentials = clientCredentialsRequest.execute();
        logger.info("ClientCredentialsRequestを実行し、ClientCredentialsを取得しました。");
        spotifyApi.setAccessToken(clientCredentials.getAccessToken());
        logger.info("アクセストークンを設定しました: {}", clientCredentials.getAccessToken());
    }

    public List<PlaylistSimplified> searchPlaylists(String query) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストの検索を開始します。検索クエリ: {}", query);
        SearchPlaylistsRequest searchPlaylistsRequest = spotifyApi.searchPlaylists(query)
                .limit(20)
                .build();
        logger.info("SearchPlaylistsRequestを作成しました。");
        Paging<PlaylistSimplified> playlistSimplifiedPaging = searchPlaylistsRequest.execute();
        logger.info("SearchPlaylistsRequestを実行し、結果を取得しました。");
        List<PlaylistSimplified> playlists = playlistSimplifiedPaging.getItems() != null ? Arrays.asList(playlistSimplifiedPaging.getItems()) : Collections.emptyList();
        logger.info("検索結果: {}件のプレイリストが見つかりました。", playlists.size());
        return playlists;
    }

    public PlaylistTrack[] getPlaylistTracks(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのトラック取得を開始します。プレイリストID: {}", playlistId);
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        logger.info("GetPlaylistRequestを作成しました。");
        Playlist playlist = getPlaylistRequest.execute();
        logger.info("GetPlaylistRequestを実行し、プレイリスト情報を取得しました。");
        PlaylistTrack[] tracks = playlist.getTracks().getItems();
        logger.info("プレイリストのトラック数: {}", tracks.length);
        return tracks;
    }

    public List<String> getArtistGenres(String artistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("アーティストのジャンル取得を開始します。アーティストID: {}", artistId);
        GetArtistRequest getArtistRequest = spotifyApi.getArtist(artistId).build();
        Artist artist = getArtistRequest.execute();
        List<String> genres = artist.getGenres() != null ? Arrays.asList(artist.getGenres()) : Collections.emptyList();
        logger.info("アーティスト: '{}', ジャンル: {}", artist.getName(), String.join(", ", genres));
        return genres;
    }

    public AudioFeatures getAudioFeaturesForTrack(String trackId) throws IOException, SpotifyWebApiException, ParseException {
        GetAudioFeaturesForTrackRequest audioFeaturesRequest = spotifyApi.getAudioFeaturesForTrack(trackId).build();
        return audioFeaturesRequest.execute();
    }

    public String getPlaylistName(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストの名前を取得します。プレイリストID: {}", playlistId);
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        Playlist playlist = getPlaylistRequest.execute();
        String playlistName = playlist.getName();
        logger.info("プレイリスト名: {}", playlistName);
        return playlistName;
    }

    public User getPlaylistOwner(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストの作成者情報を取得します。プレイリストID: {}", playlistId);
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        Playlist playlist = getPlaylistRequest.execute();
        User owner = playlist.getOwner();
        logger.info("プレイリスト作成者: ID = {}, 名前 = {}", owner.getId(), owner.getDisplayName());
        return owner;
    }

    public List<PlaylistSimplified> getCurrentUsersPlaylists(OAuth2AuthenticationToken authentication) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("ユーザーがフォローしているプレイリストの取得を開始します。");
        setAccessToken(authentication);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();
        logger.info("GetListOfCurrentUsersPlaylistsRequestを作成しました。");
        Paging<PlaylistSimplified> playlistsPaging = playlistsRequest.execute();
        logger.info("GetListOfCurrentUsersPlaylistsRequestを実行し、結果を取得しました。");
        List<PlaylistSimplified> playlists = playlistsPaging.getItems() != null ? Arrays.asList(playlistsPaging.getItems()) : Collections.emptyList();
        logger.info("取得したプレイリスト数: {}", playlists.size());
        return playlists;
    }
}
