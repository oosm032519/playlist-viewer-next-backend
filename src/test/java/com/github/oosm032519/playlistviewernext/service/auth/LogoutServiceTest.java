// LogoutServiceTest.java

package com.github.oosm032519.playlistviewernext.service.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

class LogoutServiceTest {

    private LogoutService logoutService;

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private SecurityContextLogoutHandler logoutHandler;

    /**
     * 各テストの前にモックの初期化とテスト対象のインスタンスのセットアップを行う
     */
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logoutService = new LogoutService(authorizedClientService, logoutHandler);
        SecurityContextHolder.setContext(securityContext);
    }

    /**
     * 認証されたユーザーがログアウトする際の処理をテストする
     */
    @Test
    void testProcessLogoutWithAuthenticatedUser() {
        // Arrange: モックの設定
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("testCookie", "testValue")});

        // Act: ログアウト処理を実行
        logoutService.processLogout(request, response);

        // Assert: ログアウトハンドラが呼び出されたことを検証
        verify(logoutHandler, times(1)).logout(request, response, authentication);
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    /**
     * OAuth2認証トークンを持つユーザーがログアウトする際の処理をテストする
     */
    @Test
    void testProcessLogoutWithOAuth2AuthenticationToken() {
        // Arrange: モックの設定
        OAuth2AuthenticationToken oauthToken = mock(OAuth2AuthenticationToken.class);
        when(securityContext.getAuthentication()).thenReturn(oauthToken);
        when(oauthToken.getName()).thenReturn("testUser");
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("testCookie", "testValue")});

        // Act: ログアウト処理を実行
        logoutService.processLogout(request, response);

        // Assert: OAuth2クライアントサービスとログアウトハンドラが呼び出されたことを検証
        verify(authorizedClientService, times(1)).removeAuthorizedClient("spotify", "testUser");
        verify(logoutHandler, times(1)).logout(request, response, oauthToken);
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    /**
     * 認証情報がない場合のログアウト処理をテストする
     */
    @Test
    void testProcessLogoutWithoutAuthentication() {
        // Arrange: モックの設定
        when(securityContext.getAuthentication()).thenReturn(null);
        when(request.getCookies()).thenReturn(new Cookie[]{new Cookie("testCookie", "testValue")});

        // Act: ログアウト処理を実行
        logoutService.processLogout(request, response);

        // Assert: ログアウトハンドラが呼び出されないことを検証
        verify(logoutHandler, never()).logout(request, response, null);
        verify(response, times(1)).addCookie(any(Cookie.class));
    }

    /**
     * クッキーがない場合のログアウト処理をテストする
     */
    @Test
    void testProcessLogoutWithNoCookies() {
        // Arrange: モックの設定
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getCookies()).thenReturn(null);

        // Act: ログアウト処理を実行
        logoutService.processLogout(request, response);

        // Assert: ログアウトハンドラが呼び出されたことを検証
        verify(logoutHandler, times(1)).logout(request, response, authentication);
        verify(response, never()).addCookie(any(Cookie.class));
    }

    /**
     * 複数のクッキーがある場合のログアウト処理をテストする
     */
    @Test
    void testProcessLogoutWithMultipleCookies() {
        // Arrange: モックの設定
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(request.getCookies()).thenReturn(new Cookie[]{
                new Cookie("cookie1", "value1"),
                new Cookie("cookie2", "value2"),
                new Cookie("cookie3", "value3")
        });

        // Act: ログアウト処理を実行
        logoutService.processLogout(request, response);

        // Assert: ログアウトハンドラが呼び出されたことを検証
        verify(logoutHandler, times(1)).logout(request, response, authentication);
        verify(response, times(3)).addCookie(any(Cookie.class));
    }
}
