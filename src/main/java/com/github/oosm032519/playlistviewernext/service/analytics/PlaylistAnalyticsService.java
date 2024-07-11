// PlaylistAnalyticsService.java

package com.github.oosm032519.playlistviewernext.service.analytics;

import org.apache.hc.core5.http.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class PlaylistAnalyticsService {

    // SpotifyPlaylistAnalyticsServiceのインスタンスを自動的に注入
    @Autowired
    private SpotifyPlaylistAnalyticsService analyticsService;

    /**
     * 指定されたプレイリストのジャンルごとの曲数を取得します。
     *
     * @param id プレイリストのID
     * @return ジャンルごとの曲数を表すMap
     * @throws IOException            入出力例外が発生した場合
     * @throws ParseException         パース例外が発生した場合
     * @throws SpotifyWebApiException Spotify Web API例外が発生した場合
     */
    public Map<String, Integer> getGenreCountsForPlaylist(String id) throws IOException, ParseException, SpotifyWebApiException {
        return analyticsService.getGenreCountsForPlaylist(id);
    }

    /**
     * 指定されたプレイリストのトップ5ジャンルを取得します。
     *
     * @param id プレイリストのID
     * @return トップ5ジャンルのリスト
     * @throws IOException            入出力例外が発生した場合
     * @throws ParseException         パース例外が発生した場合
     * @throws SpotifyWebApiException Spotify Web API例外が発生した場合
     */
    public List<String> getTop5GenresForPlaylist(String id) throws IOException, ParseException, SpotifyWebApiException {
        return analyticsService.getTop5GenresForPlaylist(id);
    }
}
