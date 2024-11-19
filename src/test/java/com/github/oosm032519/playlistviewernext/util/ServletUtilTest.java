package com.github.oosm032519.playlistviewernext.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServletUtilTest {

    @Mock
    private HttpServletRequest mockRequest;

    @Test
    @DisplayName("セッションIDを含むCookieが存在する場合、正しくセッションIDを抽出できること")
    void extractSessionIdFromRequest_WithValidSessionIdCookie_ReturnsSessionId() {
        // テストデータの準備
        String expectedSessionId = "test-session-id";
        Cookie sessionCookie = new Cookie("sessionId", expectedSessionId);
        Cookie[] cookies = {
                new Cookie("otherCookie", "otherValue"),
                sessionCookie
        };

        // モックの設定
        when(mockRequest.getCookies()).thenReturn(cookies);

        // テスト実行
        String actualSessionId = ServletUtil.extractSessionIdFromRequest(mockRequest);

        // 検証
        assertThat(actualSessionId)
                .as("抽出されたセッションIDが期待値と一致すること")
                .isEqualTo(expectedSessionId);
    }

    @Test
    @DisplayName("Cookieが存在しない場合、nullを返すこと")
    void extractSessionIdFromRequest_WithNoCookies_ReturnsNull() {
        // モックの設定
        when(mockRequest.getCookies()).thenReturn(null);

        // テスト実行
        String sessionId = ServletUtil.extractSessionIdFromRequest(mockRequest);

        // 検証
        assertThat(sessionId)
                .as("Cookieが存在しない場合はnullを返すこと")
                .isNull();
    }

    @Test
    @DisplayName("セッションIDを含むCookieが存在しない場合、nullを返すこと")
    void extractSessionIdFromRequest_WithoutSessionIdCookie_ReturnsNull() {
        // テストデータの準備
        Cookie[] cookies = {
                new Cookie("otherCookie1", "value1"),
                new Cookie("otherCookie2", "value2")
        };

        // モックの設定
        when(mockRequest.getCookies()).thenReturn(cookies);

        // テスト実行
        String sessionId = ServletUtil.extractSessionIdFromRequest(mockRequest);

        // 検証
        assertThat(sessionId)
                .as("セッションIDを含むCookieが存在しない場合はnullを返すこと")
                .isNull();
    }
}