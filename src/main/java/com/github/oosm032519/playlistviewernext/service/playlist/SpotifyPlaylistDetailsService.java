package com.github.oosm032519.playlistviewernext.service.playlist;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.exception.ResourceNotFoundException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.*;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistRequest;
import se.michaelthelin.spotify.requests.data.playlists.GetPlaylistsItemsRequest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Spotifyのプレイリストの詳細情報を取得するサービスクラス
 * プレイリストの楽曲情報、オーディオ特徴、統計情報などを提供する
 */
@Service
public class SpotifyPlaylistDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyPlaylistDetailsService.class);

    private final SpotifyApi spotifyApi;
    private final WebClient webClient;
    private final ObjectMapper objectMapper;

    @Value("${spotify.mock-api.url}")
    private String mockApiUrl;

    @Value("${spotify.mock.enabled:false}")
    private boolean mockEnabled;

    @Autowired
    public SpotifyPlaylistDetailsService(SpotifyApi spotifyApi, WebClient.Builder webClientBuilder, ObjectMapper objectMapper) {
        logger.info("SpotifyPlaylistDetailsService constructor started.");
        this.spotifyApi = spotifyApi;
        webClient = webClientBuilder.build();
        this.objectMapper = objectMapper;
        logger.info("SpotifyPlaylistDetailsService constructor finished.");
    }

    /**
     * 指定されたプレイリストIDのトラック情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリスト内のトラックの配列
     * @throws ResourceNotFoundException プレイリストが見つからない場合
     */
    public PlaylistTrack[] getPlaylistTracks(String playlistId) throws SpotifyWebApiException {
        logger.info("getPlaylistTracks: playlistId: {}", playlistId);
        if (mockEnabled && mockApiUrl != null && !mockApiUrl.isEmpty()) {
            logger.info("getPlaylistTracks: モックAPIからトラック情報を取得");
            return convertMapListToPlaylistTrackArray(getPlaylistTracksMock(playlistId)); // モックAPIからの返り値を変換
        } else {
            logger.info("getPlaylistTracks: Spotify APIからトラック情報を取得");
            return getPlaylistTracksReal(playlistId);
        }
    }

    // List<Map<String, Object>> を PlaylistTrack[] に変換する
    private PlaylistTrack[] convertMapListToPlaylistTrackArray(List<Map<String, Object>> mapList) {
        logger.info("convertMapListToPlaylistTrackArray: mapList size: {}", mapList != null ? mapList.size() : 0);
        if (mapList == null) {
            return new PlaylistTrack[0];
        }

        PlaylistTrack[] playlistTracks = new PlaylistTrack[mapList.size()];
        for (int i = 0; i < mapList.size(); i++) {
            Map<String, Object> trackMap = mapList.get(i);

            // Track オブジェクトを生成
            Track track = objectMapper.convertValue(trackMap, Track.class);
            logger.debug("convertMapListToPlaylistTrackArray: Track オブジェクト生成: {}", track);


            // PlaylistTrack オブジェクトを生成
            PlaylistTrack.Builder playlistTrackBuilder = new PlaylistTrack.Builder();
            playlistTrackBuilder.setTrack(track);

            playlistTracks[i] = playlistTrackBuilder.build();
            logger.debug("convertMapListToPlaylistTrackArray: PlaylistTrack オブジェクト生成: {}", playlistTracks[i]);
        }
        logger.info("convertMapListToPlaylistTrackArray: PlaylistTrack 変換完了, playlistTracks size: {}", playlistTracks.length);

        return playlistTracks;
    }

    private List<Map<String, Object>> getPlaylistTracksMock(String playlistId) {
        logger.info("getPlaylistTracksMock: playlistId: {}", playlistId);
        logger.info("getPlaylistTracksMock: mockApiUrl: {}", mockApiUrl);
        logger.info("getPlaylistTracksMock: WebClient GET リクエスト送信, URL: {}/playlists/{}/tracks", mockApiUrl, playlistId);


        // WebClientを使用してモックAPIからデータを取得
        List<Map<String, Object>> tracks = null;
        try {
            tracks = webClient.get() // List<Map<String, Object>> を取得
                    .uri(mockApiUrl + "/playlists/{playlistId}/tracks", playlistId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<List<Map<String, Object>>>() { // TypeReference を List<Map<String, Object>> に変更
                    })
                    .onErrorMap(WebClientResponseException.class, e -> {
                        logger.error("getPlaylistTracksMock: Mock API 呼び出しエラー: {}", e.getResponseBodyAsString(), e);
                        return new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Error calling mock API", e);
                    })
                    .block();
            logger.info("getPlaylistTracksMock: Mock API レスポンス受信, tracks size: {}", tracks != null ? tracks.size() : 0);
            logger.debug("getPlaylistTracksMock: Mock API レスポンス内容: {}", tracks);

        } catch (WebClientRequestException e) {
            logger.error("getPlaylistTracksMock: WebClientRequestException: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("getPlaylistTracksMock: 予期せぬエラー: ", e);
            throw e;
        }


        return tracks;
    }

    private PlaylistTrack[] getPlaylistTracksReal(String playlistId) throws SpotifyWebApiException {
        logger.info("getPlaylistTracksReal: playlistId: {}", playlistId);

        return RetryUtil.executeWithRetry(() -> {
            try {
                Playlist playlist = getPlaylist(playlistId);
                if (playlist == null) {
                    throw new ResourceNotFoundException(
                            HttpStatus.NOT_FOUND,
                            "指定されたプレイリストが見つかりません。"
                    );
                }

                // 最初の100曲を追加
                List<PlaylistTrack> allTracks = new ArrayList<>(Arrays.asList(playlist.getTracks().getItems()));
                logger.info("getPlaylistTracksReal: 最初の100曲を取得, allTracks size: {}", allTracks.size());


                int offset = 100;
                int limit = 100;

                // 全曲取得するまで繰り返す
                while (allTracks.size() < playlist.getTracks().getTotal()) {
                    logger.info("getPlaylistTracksReal: ページネーション処理開始, offset: {}, limit: {}", offset, limit);
                    GetPlaylistsItemsRequest getPlaylistsItemsRequest = spotifyApi
                            .getPlaylistsItems(playlistId)
                            .limit(limit)
                            .offset(offset)
                            .build();
                    Paging<PlaylistTrack> playlistTracks = getPlaylistsItemsRequest.execute();
                    List<PlaylistTrack> items = Arrays.asList(playlistTracks.getItems());
                    logger.info("getPlaylistTracksReal: ページネーションレスポンス受信, items size: {}", items.size());
                    allTracks.addAll(items);
                    offset += limit;
                    logger.info("getPlaylistTracksReal: ページネーション処理完了, allTracks size: {}", allTracks.size());
                }

                logger.info("getPlaylistTracksReal: 全トラック取得完了, allTracks size: {}", allTracks.size());
                return allTracks.toArray(new PlaylistTrack[0]);
            } catch (ResourceNotFoundException e) {
                // ResourceNotFoundException はそのまま再スロー
                logger.warn("getPlaylistTracksReal: ResourceNotFoundException: {}", e.getMessage());
                throw e;
            } catch (SpotifyWebApiException e) {
                // SpotifyWebApiException はそのまま再スロー
                logger.error("getPlaylistTracksReal: SpotifyWebApiException: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                logger.error("getPlaylistTracksReal: トラック情報の取得中にエラーが発生しました。 playlistId: {}", playlistId, e);
                logger.error("getPlaylistTracksReal: エラー詳細: ", e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "トラック情報の取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS); // 最大3回再試行、初期間隔は RetryUtil のデフォルト値
    }

    /**
     * 指定されたプレイリストIDのプレイリスト情報を取得するメソッド
     *
     * @param playlistId プレイリストのID
     * @return プレイリスト情報、見つからない場合は null
     */
    public Playlist getPlaylist(String playlistId) throws SpotifyWebApiException {
        logger.info("getPlaylist: playlistId: {}", playlistId);
        if (mockEnabled && mockApiUrl != null && !mockApiUrl.isEmpty()) {
            logger.info("getPlaylist: モックAPIからプレイリスト情報を取得");
            return getPlaylistMock(playlistId);
        } else {
            logger.info("getPlaylist: Spotify APIからプレイリスト情報を取得");
            return getPlaylistReal(playlistId);
        }
    }

    private Playlist getPlaylistMock(String playlistId) {
        logger.info("getPlaylistMock: playlistId: {}", playlistId);
        logger.info("getPlaylistMock: mockApiUrl: {}", mockApiUrl);
        logger.info("getPlaylistMock: WebClient GET リクエスト送信, URL: {}/playlists/{}", mockApiUrl, playlistId);

        // WebClientを使用してモックAPIからデータを取得
        Map<String, Object> response = null;
        try {
            response = webClient.get()
                    .uri(mockApiUrl + "/playlists/{playlistId}", playlistId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .onErrorMap(WebClientResponseException.class, e -> {
                        logger.error("getPlaylistMock: Mock API 呼び出しエラー: {}", e.getResponseBodyAsString(), e);
                        return new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Error calling mock API", e);
                    })
                    .block();
            logger.info("getPlaylistMock: Mock API レスポンス受信, response: {}", response);
            logger.debug("getPlaylistMock: Mock API レスポンス内容: {}", response);
        } catch (WebClientRequestException e) {
            logger.error("getPlaylistMock: WebClientRequestException: ", e);
            throw e;
        } catch (Exception e) {
            logger.error("getPlaylistMock: 予期せぬエラー: ", e);
            throw e;
        }

        // 取得したデータをPlaylistオブジェクトにマッピング
        Playlist.Builder builder = new Playlist.Builder();
        builder.setId((String) response.get("id"));
        builder.setName((String) response.get("playlistName"));

        // owner オブジェクトをマッピング
        Map<String, Object> ownerMap = (Map<String, Object>) response.get("owner");
        if (ownerMap != null) {
            User.Builder userBuilder = new User.Builder();
            userBuilder.setId((String) ownerMap.get("id"));
            userBuilder.setDisplayName((String) ownerMap.get("displayName"));
            builder.setOwner(userBuilder.build());
            logger.info("getPlaylistMock: ownerオブジェクト設定完了");
        } else {
            logger.warn("getPlaylistMock: ownerオブジェクトがnullです");
        }

        Playlist playlist = builder.build();
        logger.info("getPlaylistMock: Playlist オブジェクト: {}", playlist);
        logger.info("getPlaylistMock: Playlist オブジェクト生成完了");

        return playlist;
    }

    private Playlist getPlaylistReal(String playlistId) throws SpotifyWebApiException {
        logger.info("getPlaylistReal: playlistId: {}", playlistId);

        return RetryUtil.executeWithRetry(() -> {
            try {
                logger.info("getPlaylistReal: Spotify API リクエスト送信, playlistId: {}", playlistId);
                GetPlaylistRequest getPlaylistRequest = spotifyApi.getPlaylist(playlistId).build();
                Playlist playlist = getPlaylistRequest.execute();
                logger.info("getPlaylistReal: Spotify API レスポンス受信, playlist: {}", playlist);
                return playlist;
            } catch (SpotifyWebApiException e) {
                // SpotifyWebApiException はそのまま再スロー
                logger.error("getPlaylistReal: SpotifyWebApiException: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                logger.error("getPlaylistReal: プレイリスト情報の取得中にエラーが発生しました。 playlistId: {}", playlistId, e);
                logger.error("getPlaylistReal: エラー詳細: ", e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "プレイリスト情報の取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS);
    }
}
