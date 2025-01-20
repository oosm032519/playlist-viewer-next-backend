package com.github.oosm032519.playlistviewernext.model.mock;

import com.github.oosm032519.playlistviewernext.model.FavoritePlaylistResponse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class MockData {

    public static Map<String, Object> getMockedPlaylistSearchResponse() {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> playlists = new ArrayList<>();

        // ダミーのプレイリストをいくつか作成
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> playlist = new HashMap<>();
            playlist.put("id", "mock-playlist-id-" + i);
            playlist.put("name", "Mock Playlist " + i);

            Map<String, Object> tracks = new HashMap<>();
            tracks.put("total", 10);
            playlist.put("tracks", tracks);

            List<Map<String, String>> images = new ArrayList<>();
            images.add(Map.of("url", "https://via.placeholder.com/150"));
            playlist.put("images", images);

            Map<String, Object> externalUrls = new HashMap<>();
            externalUrls.put("spotify", "https://open.spotify.com/playlist/mock-playlist-id-" + i);
            playlist.put("externalUrls", Map.of("externalUrls", externalUrls));

            playlists.add(playlist);
        }

        response.put("playlists", playlists);
        response.put("total", 5);
        return response;
    }

    public static Map<String, Object> getMockedPlaylistDetails(String playlistId) {
        Map<String, Object> response = new HashMap<>();
        response.put("playlistName", "Mock Playlist");
        response.put("ownerId", "mock-owner-id");
        response.put("ownerName", "Mock Owner");
        response.put("totalDuration", 3600000); // 1 hour in milliseconds

        // AudioFeaturesのモックデータ（オブジェクト形式に修正）
        Map<String, Object> maxAudioFeatures = new HashMap<>();
        maxAudioFeatures.put("acousticness", 1.0f);
        maxAudioFeatures.put("danceability", 1.0f);
        maxAudioFeatures.put("energy", 1.0f);
        maxAudioFeatures.put("instrumentalness", 1.0f);
        maxAudioFeatures.put("liveness", 1.0f);
        maxAudioFeatures.put("speechiness", 1.0f);
        maxAudioFeatures.put("valence", 1.0f);
        maxAudioFeatures.put("tempo", 200.0f);
        response.put("maxAudioFeatures", maxAudioFeatures);

        Map<String, Object> minAudioFeatures = new HashMap<>();
        minAudioFeatures.put("acousticness", 0.0f);
        minAudioFeatures.put("danceability", 0.0f);
        minAudioFeatures.put("energy", 0.0f);
        minAudioFeatures.put("instrumentalness", 0.0f);
        minAudioFeatures.put("liveness", 0.0f);
        minAudioFeatures.put("speechiness", 0.0f);
        minAudioFeatures.put("valence", 0.0f);
        minAudioFeatures.put("tempo", 50.0f);
        response.put("minAudioFeatures", minAudioFeatures);

        Map<String, Object> averageAudioFeatures = new HashMap<>();
        averageAudioFeatures.put("acousticness", 0.5f);
        averageAudioFeatures.put("danceability", 0.5f);
        averageAudioFeatures.put("energy", 0.5f);
        averageAudioFeatures.put("instrumentalness", 0.5f);
        averageAudioFeatures.put("liveness", 0.5f);
        averageAudioFeatures.put("speechiness", 0.5f);
        averageAudioFeatures.put("valence", 0.5f);
        averageAudioFeatures.put("tempo", 120.0f);
        response.put("averageAudioFeatures", averageAudioFeatures);

        // ジャンルカウントのモックデータ
        Map<String, Integer> genreCounts = new HashMap<>();
        genreCounts.put("Pop", 5);
        genreCounts.put("Rock", 3);
        genreCounts.put("Jazz", 2);
        response.put("genreCounts", genreCounts);

        // シードアーティストのモックデータ
        List<String> seedArtists = Arrays.asList("artistId1", "artistId2", "artistId3");
        response.put("seedArtists", seedArtists);

        // トラックリストのモックデータ
        List<Map<String, Object>> tracks = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            Map<String, Object> trackData = new HashMap<>();
            trackData.put("id", "mock-track-id-" + i);
            trackData.put("name", "Mock Track " + i);
            trackData.put("durationMs", 180000); // 3 minutes in milliseconds

            Map<String, Object> album = new HashMap<>();
            album.put("name", "Mock Album " + i);
            album.put("images", Collections.singletonList(Map.of("url", "https://via.placeholder.com/150")));
            // album.externalUrls を externalUrls オブジェクトでラップ
            album.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/album/mock-album-id-" + i)));
            trackData.put("album", album);

            List<Map<String, Object>> artists = new ArrayList<>();
            Map<String, Object> artist = new HashMap<>();
            artist.put("name", "Mock Artist " + i);
            // artist.externalUrls を externalUrls オブジェクトでラップ
            artist.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/artist/mock-artist-id-" + i)));
            artists.add(artist);
            trackData.put("artists", artists);

            // track.externalUrls を externalUrls オブジェクトでラップ
            trackData.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/track/mock-track-id-" + i)));
            trackData.put("previewUrl", "https://via.placeholder.com/150");

            // AudioFeaturesのモックデータ（オブジェクト形式）
            Map<String, Object> audioFeatures = new HashMap<>();
            audioFeatures.put("acousticness", 0.5f);
            audioFeatures.put("danceability", 0.5f);
            audioFeatures.put("energy", 0.5f);
            audioFeatures.put("instrumentalness", 0.5f);
            audioFeatures.put("liveness", 0.5f);
            audioFeatures.put("loudness", -6.0f);
            audioFeatures.put("mode", "MAJOR");
            audioFeatures.put("speechiness", 0.5f);
            audioFeatures.put("tempo", 120.0f);
            audioFeatures.put("timeSignature", 4);
            audioFeatures.put("valence", 0.5f);
            audioFeatures.put("key", 7);
            audioFeatures.put("durationMs", 180000);
            audioFeatures.put("id", "mock-track-id-" + i);
            // trackDataにaudioFeaturesを追加
            trackData.put("audioFeatures", audioFeatures);

            // trackData を track オブジェクトでラップ
            Map<String, Object> track = new HashMap<>();
            track.put("track", trackData);
            // itemにaudioFeaturesを追加
            track.put("audioFeatures", audioFeatures);
            tracks.add(track);
        }
        response.put("tracks", Map.of("items", tracks));

        // プレイリストIDをモックデータに含める
        response.put("id", playlistId);

        return response;
    }

    public static List<Map<String, Object>> getMockedRecommendations() {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        // ダミーのトラックをいくつか作成
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> track = new HashMap<>();
            track.put("id", "mock-track-id-" + i);
            track.put("name", "Mock Track " + i);
            track.put("durationMs", 180000); // 3 minutes in milliseconds

            Map<String, Object> album = new HashMap<>();
            album.put("name", "Mock Album " + i);
            album.put("images", Collections.singletonList(Map.of("url", "https://via.placeholder.com/150")));
            album.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/album/mock-album-id-" + i)));
            track.put("album", album);

            List<Map<String, Object>> artists = new ArrayList<>();
            Map<String, Object> artist = new HashMap<>();
            artist.put("name", "Mock Artist " + i);
            artist.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/artist/mock-artist-id-" + i)));
            artists.add(artist);
            track.put("artists", artists);

            track.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/track/mock-track-id-" + i)));
            track.put("previewUrl", "https://via.placeholder.com/150");

            recommendations.add(track);
        }

        return recommendations;
    }

    public static List<Map<String, Object>> getMockedFollowedPlaylists() {
        List<Map<String, Object>> playlists = new ArrayList<>();

        // ダミーのプレイリストをいくつか作成
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> playlist = new HashMap<>();
            playlist.put("id", "mock-playlist-id-" + i);
            playlist.put("name", "Mock Playlist " + i);

            Map<String, Object> tracks = new HashMap<>();
            tracks.put("total", 10);
            playlist.put("tracks", tracks);

            List<Map<String, String>> images = new ArrayList<>();
            images.add(Map.of("url", "https://via.placeholder.com/150"));
            playlist.put("images", images);

            playlist.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/playlist/mock-playlist-id-" + i)));

            Map<String, Object> owner = new HashMap<>();
            owner.put("displayName", "Mock Owner " + i);
            playlist.put("owner", owner);

            playlists.add(playlist);
        }

        return playlists;
    }

    public static List<FavoritePlaylistResponse> getMockedFavoritePlaylists() {
        List<FavoritePlaylistResponse> favoritePlaylists = new ArrayList<>();

        // ダミーのお気に入りプレイリストをいくつか作成
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        for (int i = 1; i <= 5; i++) {
            FavoritePlaylistResponse playlist = new FavoritePlaylistResponse(
                    "mock-playlist-id-" + i,
                    "Mock Playlist " + i,
                    "Mock Owner " + i,
                    10,
                    LocalDateTime.now()
            );
            favoritePlaylists.add(playlist);
        }

        return favoritePlaylists;
    }

    public static Map<String, Object> getMockedSessionCheckResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User is authenticated");
        response.put("userId", "mock-user-id");
        response.put("userName", "Mock User");
        response.put("spotifyAccessToken", "mock-access-token");
        return response;
    }
}
