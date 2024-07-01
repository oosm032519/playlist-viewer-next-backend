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
import se.michaelthelin.spotify.requests.data.browse.GetRecommendationsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetListOfCurrentUsersPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.search.simplified.SearchPlaylistsRequest;
import se.michaelthelin.spotify.requests.data.tracks.GetAudioFeaturesForTrackRequest;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

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
        List<PlaylistSimplified> playlists = playlistSimplifiedPaging.getItems() != null ?
                Arrays.asList(playlistSimplifiedPaging.getItems()) : Collections.emptyList();
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

        Map<String, Integer> genreCount = new HashMap<>();
        for (PlaylistTrack track : tracks) {
            Track fullTrack = (Track) track.getTrack();
            ArtistSimplified[] artists = fullTrack.getArtists();
            for (ArtistSimplified artist : artists) {
                String artistId = artist.getId();
                List<String> genres = getArtistGenres(artistId);
                for (String genre : genres) {
                    genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1);
                }
                logger.info("トラック: '{}', アーティスト: '{}', ジャンル: {}", fullTrack.getName(), artist.getName(), String.join(", ", genres));
            }
            String trackId = fullTrack.getId();
            AudioFeatures audioFeatures = getAudioFeaturesForTrack(trackId);
            logAudioFeatures(fullTrack.getName(), audioFeatures);
        }

        logGenreCounts(genreCount);
        return tracks;
    }

    private void logGenreCounts(Map<String, Integer> genreCount) {
        logger.info("ジャンル出現回数:");
        genreCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(entry -> logger.info("{}: {} 回", entry.getKey(), entry.getValue()));
    }

    public Map<String, Integer> getGenreCountsForPlaylist(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのジャンル集計を開始します。プレイリストID: {}", playlistId);
        GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
        Playlist playlist = getPlaylistRequest.execute();
        PlaylistTrack[] tracks = playlist.getTracks().getItems();

        Map<String, Integer> genreCount = new HashMap<>();
        for (PlaylistTrack track : tracks) {
            Track fullTrack = (Track) track.getTrack();
            ArtistSimplified[] artists = fullTrack.getArtists();
            for (ArtistSimplified artist : artists) {
                String artistId = artist.getId();
                List<String> genres = getArtistGenres(artistId);
                genres.forEach(genre -> genreCount.put(genre, genreCount.getOrDefault(genre, 0) + 1));
            }
        }

        // ジャンルの出現回数を降順でソート
        return genreCount.entrySet()
                .stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, _) -> e1,
                        LinkedHashMap::new
                ));
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
        List<PlaylistSimplified> playlists = playlistsPaging.getItems() != null ?
                Arrays.asList(playlistsPaging.getItems()) : Collections.emptyList();
        logger.info("取得したプレイリスト数: {}", playlists.size());
        return playlists;
    }

    public List<String> getTop5GenresForPlaylist(String playlistId) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("プレイリストのジャンル出現頻度上位5つの取得を開始します。プレイリストID: {}", playlistId);

        Map<String, Integer> genreCounts = getGenreCountsForPlaylist(playlistId);

        // 出現頻度上位5つのジャンルを取得
        return genreCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    public List<Track> getRecommendations(List<String> seedGenres) throws IOException, SpotifyWebApiException, ParseException {
        logger.info("SpotifyService: getRecommendationsメソッドが呼び出されました。シードジャンル: {}", seedGenres);
        if (seedGenres.isEmpty()) {
            logger.info("SpotifyService: シードジャンルが空のため、Spotify APIを呼び出しません。");
            return Collections.emptyList();
        }

        String joinedGenres = String.join(",", seedGenres);
        GetRecommendationsRequest getRecommendationsRequest = spotifyApi.getRecommendations()
                .seed_genres(joinedGenres)
                .limit(20)
                .build();

        Recommendations recommendations = getRecommendationsRequest.execute();
        List<Track> recommendedTracks = recommendations.getTracks() != null ?
                Arrays.asList(recommendations.getTracks()) : Collections.emptyList();

        logger.info("SpotifyService: オススメ楽曲を取得しました。楽曲数: {}", recommendedTracks.size());
        for (Track track : recommendedTracks) {
            logger.info(" - 曲名: {}, アーティスト: {}", track.getName(), track.getArtists()[0].getName());
        }
        return recommendedTracks;
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
}
