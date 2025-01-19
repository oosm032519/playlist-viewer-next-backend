package com.github.oosm032519.playlistviewernext.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.oosm032519.playlistviewernext.model.mock.MockData;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Map;

@Component
public class MockDataInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(MockDataInterceptor.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${spotify.mock.enabled}")
    private boolean mockEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (mockEnabled && request.getRequestURI().startsWith("/api/")) {
            logger.info("MockDataInterceptor: モックモードが有効です。リクエストURI: {}", request.getRequestURI());

            // モックデータを使用してレスポンスを生成
            Object mockResponse = getMockResponse(request.getRequestURI(), request.getMethod());

            if (mockResponse != null) {
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(objectMapper.writeValueAsString(mockResponse));
                logger.info("MockDataInterceptor: モックデータを返却しました。URI: {}", request.getRequestURI());
                return false; // ハンドラーの実行を停止
            } else {
                logger.warn("MockDataInterceptor: リクエストURIに一致するモックデータが見つかりません。URI: {}", request.getRequestURI());
            }
        }
        return true; // 通常の処理を続行
    }

    private Object getMockResponse(String requestURI, String method) {
        // リクエストURIに基づいて適切なモックデータを返す
        if (requestURI.startsWith("/api/playlists/search") && "GET".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/playlists/search に対するモックデータを取得します。");
            return MockData.getMockedPlaylistSearchResponse();
        } else if (requestURI.matches("/api/playlists/\\w+/details") && "GET".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/playlists/{id}/details に対するモックデータを取得します。");
            return MockData.getMockedPlaylistDetails();
        } else if (requestURI.startsWith("/api/playlists/recommendations") && "POST".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/playlists/recommendations に対するモックデータを取得します。");
            return MockData.getMockedRecommendations();
        } else if (requestURI.startsWith("/api/playlists/followed") && "GET".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/playlists/followed に対するモックデータを取得します。");
            return MockData.getMockedFollowedPlaylists();
        } else if (requestURI.startsWith("/api/playlists/favorites") && "GET".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/playlists/favorites に対するモックデータを取得します。");
            return MockData.getMockedFavoritePlaylists();
        } else if (requestURI.startsWith("/api/playlists/favorite") && ("POST".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method))) {
            logger.info("MockDataInterceptor: /api/playlists/favorite に対するモックデータを取得します。");
            return Map.of("status", "success", "message", "お気に入りプレイリストの操作が成功しました");
        } else if (requestURI.startsWith("/api/playlists/create") && "POST".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/playlists/create に対するモックデータを取得します。");
            return Map.of("playlistId", "mock-playlist-id");
        } else if (requestURI.startsWith("/api/playlist/add-track") && "POST".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/playlist/add-track に対するモックデータを取得します。");
            return Map.of("message", "トラックが正常に追加されました。", "snapshot_id", "mock-snapshot-id");
        } else if (requestURI.startsWith("/api/playlist/remove-track") && "POST".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/playlist/remove-track に対するモックデータを取得します。");
            return Map.of("message", "トラックが正常に削除されました。");
        } else if (requestURI.startsWith("/api/session/check") && "GET".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/session/check に対するモックデータを取得します。");
            return MockData.getMockedSessionCheckResponse();
        } else if (requestURI.startsWith("/api/session/logout") && "POST".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/session/logout に対するモックデータを取得します。");
            return Map.of("status", "success", "message", "ログアウトしました。");
        } else if (requestURI.startsWith("/api/session/sessionId") && "POST".equalsIgnoreCase(method)) {
            logger.info("MockDataInterceptor: /api/session/sessionId に対するモックデータを取得します。");
            return Map.of("sessionId", "mock-session-id");
        }
        return null;
    }
}
