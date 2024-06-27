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
import java.util.Arrays;
import java.util.List;

@Service
public class SpotifyService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyService.class);

    @Autowired
    private SpotifyApi spotifyApi;

    @Autowired
    private OAuth2AuthorizedClientService authorizedClientService;

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
        SearchPlaylistsRequest searchPlaylistsRequest = spotifyApi.searchPlaylists(query).build();
        logger.info("SearchPlaylistsRequestを作成しました。");
        Paging<PlaylistSimplified> playlistSimplifiedPaging = searchPlaylistsRequest.execute();
        logger.info("SearchPlaylistsRequestを実行し、結果を取得しました。");
        List<PlaylistSimplified> playlists = Arrays.asList(playlistSimplifiedPaging.getItems());
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

        for (PlaylistTrack track : tracks) {
            Track fullTrack = (Track) track.getTrack();
            ArtistSimplified[] artists = fullTrack.getArtists();
            for (ArtistSimplified artist : artists) {
                String artistId = artist.getId();
                List<String> genres = getArtistGenres(artistId);
                logger.info("トラック: '{}', アーティスト: '{}', ジャンル: {}",
                        fullTrack.getName(), artist.getName(), String.join(", ", genres));
            }

            String trackId = fullTrack.getId();
            AudioFeatures audioFeatures = getAudioFeaturesForTrack(trackId);
            logAudioFeatures(fullTrack.getName(), audioFeatures);
        }

        return tracks;
    }

    public List<String> getArtistGenres(String artistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("アーティストのジャンル取得を開始します。アーティストID: {}", artistId);
        GetArtistRequest getArtistRequest = spotifyApi.getArtist(artistId).build();
        Artist artist = getArtistRequest.execute();
        List<String> genres = Arrays.asList(artist.getGenres());
        logger.info("アーティスト: '{}', ジャンル: {}", artist.getName(), String.join(", ", genres));
        return genres;
    }

    public AudioFeatures getAudioFeaturesForTrack(String trackId) throws IOException, SpotifyWebApiException, ParseException {
        GetAudioFeaturesForTrackRequest audioFeaturesRequest = spotifyApi.getAudioFeaturesForTrack(trackId).build();
        return audioFeaturesRequest.execute();
    }

    private void logAudioFeatures(String trackName, AudioFeatures audioFeatures) {
        logger.info("Track: {}", trackName);
        logger.info("Audio Features:");
        logger.info("  Danceability: {}", audioFeatures.getDanceability());
        logger.info("  Energy: {}", audioFeatures.getEnergy());
        logger.info("  Key: {}", audioFeatures.getKey());
        logger.info("  Loudness: {}", audioFeatures.getLoudness());
        logger.info("  Mode: {}", audioFeatures.getMode());
        logger.info("  Speechiness: {}", audioFeatures.getSpeechiness());
        logger.info("  Acousticness: {}", audioFeatures.getAcousticness());
        logger.info("  Instrumentalness: {}", audioFeatures.getInstrumentalness());
        logger.info("  Liveness: {}", audioFeatures.getLiveness());
        logger.info("  Valence: {}", audioFeatures.getValence());
        logger.info("  Tempo: {}", audioFeatures.getTempo());
        logger.info("  Duration MS: {}", audioFeatures.getDurationMs());
        logger.info("  Time Signature: {}", audioFeatures.getTimeSignature());
        logger.info("--------------------");
    }

    public List<PlaylistSimplified> getCurrentUsersPlaylists(OAuth2AuthenticationToken authentication) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("ユーザーがフォローしているプレイリストの取得を開始します。");
        setAccessToken(authentication);
        GetListOfCurrentUsersPlaylistsRequest playlistsRequest = spotifyApi.getListOfCurrentUsersPlaylists().build();
        logger.info("GetListOfCurrentUsersPlaylistsRequestを作成しました。");
        Paging<PlaylistSimplified> playlistsPaging = playlistsRequest.execute();
        logger.info("GetListOfCurrentUsersPlaylistsRequestを実行し、結果を取得しました。");
        List<PlaylistSimplified> playlists = Arrays.asList(playlistsPaging.getItems());
        logger.info("取得したプレイリスト数: {}", playlists.size());
        return playlists;
    }
}
