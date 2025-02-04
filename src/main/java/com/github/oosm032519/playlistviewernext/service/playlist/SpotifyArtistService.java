package com.github.oosm032519.playlistviewernext.service.playlist;

import com.github.oosm032519.playlistviewernext.exception.InternalServerException;
import com.github.oosm032519.playlistviewernext.util.RetryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.specification.Artist;
import se.michaelthelin.spotify.requests.data.artists.GetSeveralArtistsRequest;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Spotify API を使用してアーティスト情報を取得するサービス
 */
@Service
public class SpotifyArtistService {

    private static final Logger logger = LoggerFactory.getLogger(SpotifyArtistService.class);

    private final SpotifyApi spotifyApi;
    private final WebClient webClient;

    @Value("${spotify.mock-api.url}")
    private String mockApiUrl;

    @Value("${spotify.mock.enabled:false}")
    private boolean mockEnabled;

    @Autowired
    public SpotifyArtistService(SpotifyApi spotifyApi, WebClient.Builder webClientBuilder) {
        this.spotifyApi = spotifyApi;
        webClient = webClientBuilder.build();
    }

    /**
     * 指定されたアーティストIDのリストに基づき、各アーティストのジャンルを取得する
     *
     * @param artistIds アーティストIDのリスト
     * @return アーティストIDとジャンルのリストのマップ
     */
    public Map<String, List<String>> getArtistGenres(List<String> artistIds) throws SpotifyWebApiException {
        if (mockEnabled && mockApiUrl != null && !mockApiUrl.isEmpty()) {
            return getArtistGenresMock(artistIds);
        } else {
            return getArtistGenresReal(artistIds);
        }
    }

    private Map<String, List<String>> getArtistGenresMock(List<String> artistIds) {
        logger.info("Getting artist genres using mock API. Artist IDs: {}", artistIds);

        // WebClientを使用してモックAPIからデータを取得
        Map<String, List<String>> response = webClient.get()
                .uri(mockApiUrl + "/artists/genres", uriBuilder -> uriBuilder
                        .queryParam("artistIds", String.join(",", artistIds))
                        .build())
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, List<String>>>() {
                })
                .onErrorMap(WebClientResponseException.class, e -> {
                    logger.error("Error calling mock API: {}", e.getResponseBodyAsString(), e);
                    return new InternalServerException(HttpStatus.INTERNAL_SERVER_ERROR, "Error calling mock API", e);
                })
                .block();

        return response;
    }

    private Map<String, List<String>> getArtistGenresReal(List<String> artistIds) throws SpotifyWebApiException {
        logger.info("Getting artist genres using real API. Artist IDs: {}", artistIds);

        return RetryUtil.executeWithRetry(() -> {
            try {
                // アーティストIDのリストを50個以下のチャンクに分割
                List<List<String>> artistIdChunks = new ArrayList<>();
                for (int i = 0; i < artistIds.size(); i += 50) {
                    artistIdChunks.add(artistIds.subList(i, Math.min(i + 50, artistIds.size())));
                }

                // 各チャンクに対して GetSeveralArtistsRequest を実行
                Map<String, List<String>> artistGenresMap = new HashMap<>();
                for (List<String> chunk : artistIdChunks) {
                    Artist[] artists = getArtists(chunk);
                    artistGenresMap.putAll(Arrays.stream(artists)
                            .collect(Collectors.toMap(Artist::getId, artist -> List.of(artist.getGenres()))));
                }

                return artistGenresMap;
            } catch (SpotifyWebApiException e) {
                // SpotifyWebApiException はそのまま再スロー
                logger.error("Spotify API エラー: {}", e.getMessage(), e);
                throw e;
            } catch (Exception e) {
                // その他の例外は InternalServerException にラップしてスロー
                logger.error("アーティスト情報の取得中にエラーが発生しました。 artistIds: {}", artistIds, e);
                throw new InternalServerException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "アーティスト情報の取得中にエラーが発生しました。",
                        e
                );
            }
        }, 3, RetryUtil.DEFAULT_RETRY_INTERVAL_MILLIS);
    }

    /**
     * 指定されたアーティストIDリストのアーティスト情報を取得する
     *
     * @param artistIds アーティストIDのリスト
     * @return アーティスト情報の配列
     * @throws Exception Spotify API 呼び出し中にエラーが発生した場合
     */
    private Artist[] getArtists(List<String> artistIds) throws Exception {
        GetSeveralArtistsRequest getSeveralArtistsRequest = spotifyApi.getSeveralArtists(artistIds.toArray(new String[0])).build();
        return getSeveralArtistsRequest.execute();
    }
}
