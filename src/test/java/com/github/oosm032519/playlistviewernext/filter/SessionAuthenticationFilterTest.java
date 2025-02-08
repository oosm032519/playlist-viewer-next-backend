package com.github.oosm032519.playlistviewernext.filter;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SessionAuthenticationFilterTest {

    @InjectMocks
    private SessionAuthenticationFilter sessionAuthenticationFilter;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private HashOperations<String, Object, Object> hashOperations;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.clearContext(); // テスト開始前にSecurityContextをクリア
    }

    /**
     * 有効なセッションIDがCookieに含まれている場合、認証が成功し、SecurityContextに認証情報が設定されることを確認する。
     */
    @Test
    void doFilterInternal_ValidSessionId_AuthenticationSuccess() throws ServletException, IOException {
        // Arrange: テストデータの準備とモックの設定
        String sessionId = "validSessionId";
        Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
        when(request.getCookies()).thenReturn(new Cookie[]{sessionIdCookie});
        Map<Object, Object> sessionData = createSessionData();

        // HashOperations のモックをテストメソッド内で設定
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("session:" + sessionId)).thenReturn(sessionData);

        // Act: テスト対象メソッドの実行
        sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert: 認証情報がSecurityContextに設定されていることを確認
        verify(filterChain).doFilter(request, response);
        OAuth2AuthenticationToken authentication = (OAuth2AuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication).isNotNull();
        OAuth2User oAuth2User = authentication.getPrincipal();
        assertThat(oAuth2User.<String>getAttribute("id")).isEqualTo("user123");
        assertThat(oAuth2User.<String>getAttribute("name")).isEqualTo("Test User");
        assertThat(oAuth2User.<String>getAttribute("spotify_access_token")).isEqualTo("testToken");
    }

    private Map<Object, Object> createSessionData() {
        Map<Object, Object> sessionData = new HashMap<>();
        sessionData.put("userId", "user123");
        sessionData.put("userName", "Test User");
        sessionData.put("spotifyAccessToken", "testToken");
        return sessionData;
    }

    /**
     * 無効なセッションIDがCookieに含まれている場合、認証が失敗し、AuthenticationExceptionがスローされることを確認する。
     */
    @Test
    void doFilterInternal_InvalidSessionId_AuthenticationFailure() {
        // Arrange: テストデータの準備とモックの設定
        String sessionId = "invalidSessionId";
        Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
        when(request.getCookies()).thenReturn(new Cookie[]{sessionIdCookie});

        // HashOperations のモックをテストメソッド内で設定
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("session:" + sessionId)).thenReturn(null);

        // Act & Assert: AuthenticationExceptionがスローされることの確認
        assertThatThrownBy(() -> sessionAuthenticationFilter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("セッションが有効期限切れか、無効です。再度ログインしてください。");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     * セッションIDがCookieに含まれていない場合、認証処理が行われず、フィルターチェーンが続行されることを確認する。
     */
    @Test
    void doFilterInternal_NoSessionId_NoAuthentication() throws ServletException, IOException {
        // Arrange: Cookieがないリクエストをシミュレート
        when(request.getCookies()).thenReturn(null);

        // Act: テスト対象メソッドの実行
        sessionAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // Assert: フィルターチェーンが続行され、認証情報が設定されないことを確認
        verify(filterChain).doFilter(request, response);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }

    /**
     * Redis操作中にエラーが発生した場合、AuthenticationExceptionがスローされることを確認する。
     */
    @Test
    void doFilterInternal_RedisError_AuthenticationException() {
        // Arrange: テストデータの準備とモックの設定
        String sessionId = "errorSessionId";
        Cookie sessionIdCookie = new Cookie("sessionId", sessionId);
        when(request.getCookies()).thenReturn(new Cookie[]{sessionIdCookie});

        // HashOperations のモックをテストメソッド内で設定
        when(redisTemplate.opsForHash()).thenReturn(hashOperations);
        when(hashOperations.entries("session:" + sessionId)).thenThrow(new RuntimeException("Redis error"));

        // Act & Assert: AuthenticationExceptionがスローされることの確認
        assertThatThrownBy(() -> sessionAuthenticationFilter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("セッション情報の取得中にエラーが発生しました。")
                .hasFieldOrPropertyWithValue("httpStatus", HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
