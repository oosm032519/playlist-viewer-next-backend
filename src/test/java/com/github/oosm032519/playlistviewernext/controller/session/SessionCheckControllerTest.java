// SessionCheckControllerTest.java

package com.github.oosm032519.playlistviewernext.controller.session;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@MockitoSettings(strictness = Strictness.LENIENT)
@ExtendWith(MockitoExtension.class)
class SessionCheckControllerTest {

    @Mock
    private OAuth2AuthorizedClientService authorizedClientService;

    @Mock
    private OAuth2User principal;

    @Mock
    private OAuth2AuthenticationToken authentication;

    @Mock
    private OAuth2AuthorizedClient authorizedClient;

    @Mock
    private OAuth2AccessToken accessToken;

    @InjectMocks
    private SessionCheckController sessionCheckController;

    /**
     * ユーザーが認証されている場合、成功レスポンスを返すことを確認するテスト
     */
    @Test
    void checkSession_WhenUserAuthenticated_ReturnsSuccessResponse() {
        // Arrange: テストデータの準備
        String tokenValue = "testAccessToken";
        String userId = "testUserId";

        when(authentication.getName()).thenReturn("testUser");
        when(authorizedClientService.loadAuthorizedClient("spotify", "testUser")).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(tokenValue);
        when(principal.getAttribute("id")).thenReturn(userId);

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(principal, authentication);

        // Assert: 結果の検証
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("success");
        assertThat(response.getBody().get("message")).isEqualTo("Access token is present");
        assertThat(response.getBody().get("userId")).isEqualTo(userId);
        assertThat(response.getBody().get("tokenPreview")).isEqualTo("testAccess...");
    }

    /**
     * ユーザーが認証されていない場合、エラーレスポンスを返すことを確認するテスト
     */
    @Test
    void checkSession_WhenUserNotAuthenticated_ReturnsErrorResponse() {
        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(null, null);

        // Assert: 結果の検証
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("error");
        assertThat(response.getBody().get("message")).isEqualTo("User not authenticated");
    }

    /**
     * アクセストークンが存在しない場合、エラーレスポンスを返すことを確認するテスト
     */
    @Test
    void checkSession_WhenNoAccessToken_ReturnsErrorResponse() {
        // Arrange: テストデータの準備
        when(authentication.getName()).thenReturn("testUser");
        when(authorizedClientService.loadAuthorizedClient("spotify", "testUser")).thenReturn(null);

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(principal, authentication);

        // Assert: 結果の検証
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("error");
        assertThat(response.getBody().get("message")).isEqualTo("No access token found");
    }

    /**
     * アクセストークンが短い場合、正しいトークンプレビューを返すことを確認するテスト
     */
    @Test
    void checkSession_WhenAccessTokenIsShort_ReturnsCorrectTokenPreview() {
        // Arrange: テストデータの準備
        String shortTokenValue = "short";
        when(authentication.getName()).thenReturn("testUser");
        when(authorizedClientService.loadAuthorizedClient("spotify", "testUser")).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn(shortTokenValue);
        when(principal.getAttribute("id")).thenReturn("testUserId");

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(principal, authentication);

        // Assert: 結果の検証
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("tokenPreview")).isEqualTo("short...");
    }

    /**
     * ユーザーIDがnullの場合、ユーザーIDなしのレスポンスを返すことを確認するテスト
     */
    @Test
    void checkSession_WhenUserIdIsNull_ReturnsResponseWithoutUserId() {
        // Arrange: テストデータの準備
        when(authentication.getName()).thenReturn("testUser");
        when(authorizedClientService.loadAuthorizedClient("spotify", "testUser")).thenReturn(authorizedClient);
        when(authorizedClient.getAccessToken()).thenReturn(accessToken);
        when(accessToken.getTokenValue()).thenReturn("testAccessToken");
        when(principal.getAttribute("id")).thenReturn(null);

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(principal, authentication);

        // Assert: 結果の検証
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("userId")).isNull();
    }

    /**
     * principalがnullでauthenticationが存在する場合、エラーレスポンスを返すことを確認するテスト
     */
    @Test
    void checkSession_WhenPrincipalIsNullButAuthenticationIsNot_ReturnsErrorResponse() {
        // Arrange: テストデータの準備
        when(authentication.getName()).thenReturn("testUser");

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(null, authentication);

        // Assert: 結果の検証
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("error");
        assertThat(response.getBody().get("message")).isEqualTo("User not authenticated");
    }

    /**
     * authenticationがnullでprincipalが存在する場合、エラーレスポンスを返すことを確認するテスト
     */
    @Test
    void checkSession_WhenAuthenticationIsNullButPrincipalIsNot_ReturnsErrorResponse() {
        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(principal, null);

        // Assert: 結果の検証
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("error");
        assertThat(response.getBody().get("message")).isEqualTo("User not authenticated");
    }

    /**
     * authorizedClientServiceが例外をスローした場合、エラーレスポンスを返すことを確認するテスト
     */
    @Test
    void checkSession_WhenAuthorizedClientServiceThrowsException_ReturnsErrorResponse() {
        // Arrange: テストデータの準備
        when(authentication.getName()).thenReturn("testUser");
        when(authorizedClientService.loadAuthorizedClient("spotify", "testUser")).thenThrow(new RuntimeException("Service error"));

        // Act: テスト対象メソッドの実行
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(principal, authentication);

        // Assert: 結果の検証
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().get("status")).isEqualTo("error");
        assertThat(response.getBody().get("message")).isEqualTo("Error loading authorized client: Service error");
    }
}
