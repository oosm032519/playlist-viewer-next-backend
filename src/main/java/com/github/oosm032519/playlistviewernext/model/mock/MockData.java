package com.github.oosm032519.playlistviewernext.model.mock;

import com.github.javafaker.Faker;

import java.util.*;

public class MockData {

    private static final Faker faker = new Faker(new Locale("ja"));
    private static final Random random = new Random();

    public static Map<String, Object> getMockedPlaylistSearchResponse(int offset, int limit) {
        Map<String, Object> response = new HashMap<>();
        List<Map<String, Object>> allPlaylists = new ArrayList<>();

        // ダミーのプレイリストを500個作成
        for (int i = 1; i <= 500; i++) {
            Map<String, Object> playlist = new HashMap<>();
            playlist.put("id", generateMockPlaylistId(i)); // ID生成メソッドを使用
            playlist.put("name", faker.book().title()); // java-faker
            playlist.put("description", faker.lorem().sentence()); // java-faker

            Map<String, Object> tracks = new HashMap<>();
            tracks.put("total", random.nextInt(50) + 1); // 1-50の範囲でランダム
            playlist.put("tracks", tracks);

            List<Map<String, String>> images = new ArrayList<>();
            // Picsum Photosを使用 (UUIDをシードとして使用)
            String seed = UUID.randomUUID().toString();
            images.add(Map.of("url", String.format("https://picsum.photos/seed/%s/128/128", seed)));
            playlist.put("images", images);

            playlist.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/playlist/" + playlist.get("id"))));

            allPlaylists.add(playlist);
        }

        // offset と limit に基づいてプレイリストをフィルタリング
        int startIndex = offset;
        int endIndex = Math.min(offset + limit, allPlaylists.size());

        if (startIndex >= endIndex || startIndex < 0 || endIndex > allPlaylists.size()) {
            response.put("playlists", Collections.emptyList());
            response.put("total", allPlaylists.size());
        } else {
            List<Map<String, Object>> playlists = allPlaylists.subList(startIndex, endIndex);
            response.put("playlists", playlists);
            response.put("total", allPlaylists.size());
        }

        return response;
    }

    // モックプレイリストIDを生成
    private static String generateMockPlaylistId(int i) {
        return "mockPlaylistId" + String.format("%03d", i);
    }

    public static Map<String, Object> getMockedPlaylistDetails(String playlistId) {
        Map<String, Object> response = new HashMap<>();
        response.put("id", playlistId); // 連番のIDを使用
        response.put("playlistName", faker.book().title()); // java-faker
        response.put("ownerId", UUID.randomUUID().toString()); // ランダムなUUID
        response.put("ownerName", faker.name().fullName()); // java-faker
        int totalDuration = random.nextInt(3600000) + 1800000; // 30分から90分の間でランダム
        response.put("totalDuration", totalDuration);

        // AudioFeaturesのモックデータ
        Map<String, Object> maxAudioFeatures = new HashMap<>();
        maxAudioFeatures.put("acousticness", (float) random.nextDouble());
        maxAudioFeatures.put("danceability", (float) random.nextDouble());
        maxAudioFeatures.put("energy", (float) random.nextDouble());
        maxAudioFeatures.put("instrumentalness", (float) random.nextDouble());
        maxAudioFeatures.put("liveness", (float) random.nextDouble());
        maxAudioFeatures.put("speechiness", (float) random.nextDouble());
        maxAudioFeatures.put("valence", (float) random.nextDouble());
        maxAudioFeatures.put("tempo", (float) (random.nextDouble() * 140 + 60)); // 60-200の範囲
        response.put("maxAudioFeatures", maxAudioFeatures);

        Map<String, Object> minAudioFeatures = new HashMap<>();
        minAudioFeatures.put("acousticness", (float) random.nextDouble());
        minAudioFeatures.put("danceability", (float) random.nextDouble());
        minAudioFeatures.put("energy", (float) random.nextDouble());
        minAudioFeatures.put("instrumentalness", (float) random.nextDouble());
        minAudioFeatures.put("liveness", (float) random.nextDouble());
        minAudioFeatures.put("speechiness", (float) random.nextDouble());
        minAudioFeatures.put("valence", (float) random.nextDouble());
        minAudioFeatures.put("tempo", (float) (random.nextDouble() * 140 + 60)); // 60-200の範囲
        response.put("minAudioFeatures", minAudioFeatures);

        Map<String, Object> averageAudioFeatures = new HashMap<>();
        averageAudioFeatures.put("acousticness", (float) random.nextDouble());
        averageAudioFeatures.put("danceability", (float) random.nextDouble());
        averageAudioFeatures.put("energy", (float) random.nextDouble());
        averageAudioFeatures.put("instrumentalness", (float) random.nextDouble());
        averageAudioFeatures.put("liveness", (float) random.nextDouble());
        averageAudioFeatures.put("speechiness", (float) random.nextDouble());
        averageAudioFeatures.put("valence", (float) random.nextDouble());
        averageAudioFeatures.put("tempo", (float) (random.nextDouble() * 140 + 60)); // 60-200の範囲
        response.put("averageAudioFeatures", averageAudioFeatures);

        // ジャンルカウントのモックデータ
        Map<String, Integer> genreCounts = new HashMap<>();
        List<String> genres = Arrays.asList("Pop", "Rock", "Jazz", "Hip-hop", "Electronic", "Classical", "Country");
        for (int i = 0; i < 3; i++) { // 3つのランダムなジャンルを選択
            String genre = genres.get(random.nextInt(genres.size()));
            genreCounts.put(genre, random.nextInt(5) + 1); // 1-5の範囲でカウント
        }
        response.put("genreCounts", genreCounts);

        // シードアーティストのモックデータ
        List<String> seedArtists = new ArrayList<>();
        for (int i = 0; i < 3; i++) { // 3つのランダムなアーティスト
            seedArtists.add(faker.artist().name());
        }
        response.put("seedArtists", seedArtists);

        // トラックリストのモックデータ
        List<Map<String, Object>> tracks = new ArrayList<>();
        int trackCount = random.nextInt(10) + 5; // 5-15の範囲でトラック数
        int remainingDuration = totalDuration;
        for (int i = 1; i <= trackCount; i++) {
            Map<String, Object> trackData = new HashMap<>();
            int trackDuration = (i == trackCount) ? remainingDuration : random.nextInt(remainingDuration / (trackCount - i + 1)) + 60000; // 最後のトラックで残り時間を調整
            remainingDuration -= trackDuration;

            trackData.put("id", generateMockTrackId(i)); // ID生成メソッドを使用
            trackData.put("name", faker.book().title()); // java-faker
            trackData.put("durationMs", trackDuration);

            Map<String, Object> album = new HashMap<>();
            album.put("name", faker.book().genre()); // java-faker
            // Picsum Photosを使用 (UUIDをシードとして使用)
            String seed = UUID.randomUUID().toString();
            album.put("images", Collections.singletonList(Map.of("url", String.format("https://picsum.photos/seed/%s/128/128", seed))));
            album.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/album/" + trackData.get("id"))));
            trackData.put("album", album);

            List<Map<String, Object>> artists = new ArrayList<>();
            Map<String, Object> artist = new HashMap<>();
            artist.put("name", faker.artist().name()); // java-faker
            artist.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/artist/" + trackData.get("id"))));
            artists.add(artist);
            trackData.put("artists", artists);

            trackData.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/track/" + trackData.get("id"))));
            trackData.put("previewUrl", "https://via.placeholder.com/150");

            // AudioFeaturesのモックデータ
            Map<String, Object> audioFeatures = new HashMap<>();
            audioFeatures.put("acousticness", (float) random.nextDouble());
            audioFeatures.put("danceability", (float) random.nextDouble());
            audioFeatures.put("energy", (float) random.nextDouble());
            audioFeatures.put("instrumentalness", (float) random.nextDouble());
            audioFeatures.put("liveness", (float) random.nextDouble());
            audioFeatures.put("loudness", (float) (-60.0 * random.nextDouble())); // -60.0から0.0の範囲
            audioFeatures.put("mode", random.nextInt(2)); // 0または1
            audioFeatures.put("speechiness", (float) random.nextDouble());
            audioFeatures.put("tempo", (float) (random.nextDouble() * 140 + 60)); // 60-200の範囲
            audioFeatures.put("timeSignature", random.nextInt(3) + 3); // 3, 4, 5
            audioFeatures.put("valence", (float) random.nextDouble());
            audioFeatures.put("key", random.nextInt(12)); // 0-11の範囲
            audioFeatures.put("durationMs", trackDuration);
            audioFeatures.put("id", trackData.get("id"));
            trackData.put("audioFeatures", audioFeatures);

            Map<String, Object> track = new HashMap<>();
            track.put("track", trackData);
            track.put("audioFeatures", audioFeatures);
            tracks.add(track);
        }
        response.put("tracks", Map.of("items", tracks));

        return response;
    }

    // モックトラックIDを生成
    private static String generateMockTrackId(int i) {
        return "mockTrackId" + String.format("%03d", i);
    }

    public static List<Map<String, Object>> getMockedRecommendations() {
        List<Map<String, Object>> recommendations = new ArrayList<>();

        // ダミーのトラックをいくつか作成
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> track = new HashMap<>();
            track.put("id", generateMockTrackId(i)); // ID生成メソッドを使用
            track.put("name", faker.book().title()); // java-faker
            track.put("durationMs", random.nextInt(300000) + 60000); // 1-6分の範囲

            Map<String, Object> album = new HashMap<>();
            album.put("name", faker.book().genre()); // java-faker
            // Picsum Photosを使用 (UUIDをシードとして使用)
            String seed = UUID.randomUUID().toString();
            album.put("images", Collections.singletonList(Map.of("url", String.format("https://picsum.photos/seed/%s/128/128", seed))));
            album.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/album/" + track.get("id"))));
            track.put("album", album);

            List<Map<String, Object>> artists = new ArrayList<>();
            Map<String, Object> artist = new HashMap<>();
            artist.put("name", faker.artist().name()); // java-faker
            artist.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/artist/" + track.get("id"))));
            artists.add(artist);
            track.put("artists", artists);

            track.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/track/" + track.get("id"))));
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
            playlist.put("id", generateMockPlaylistId(i)); // ID生成メソッドを使用
            playlist.put("name", faker.book().title()); // java-faker

            Map<String, Object> tracks = new HashMap<>();
            tracks.put("total", random.nextInt(50) + 1); // 1-50の範囲でランダム
            playlist.put("tracks", tracks);

            List<Map<String, String>> images = new ArrayList<>();
            // Picsum Photosを使用 (UUIDをシードとして使用)
            String seed = UUID.randomUUID().toString();
            images.add(Map.of("url", String.format("https://picsum.photos/seed/%s/128/128", seed)));
            playlist.put("images", images);

            playlist.put("externalUrls", Map.of("externalUrls", Map.of("spotify", "https://open.spotify.com/playlist/" + playlist.get("id"))));

            Map<String, Object> owner = new HashMap<>();
            owner.put("displayName", faker.name().fullName()); // java-faker
            playlist.put("owner", owner);

            playlists.add(playlist);
        }

        return playlists;
    }

    public static Map<String, Object> getMockedSessionCheckResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User is authenticated");
        response.put("userId", "mock-user-id");
        response.put("userName", faker.name().username()); // java-faker
        return response;
    }

    public static Map<String, Object> getMockedLoginResponse() {
        Map<String, Object> response = new HashMap<>();
        response.put("userId", "mock-user-id");
        response.put("userName", faker.name().username()); // java-faker
        response.put("status", "success");
        response.put("message", "モックログインに成功しました。");
        return response;
    }
}
