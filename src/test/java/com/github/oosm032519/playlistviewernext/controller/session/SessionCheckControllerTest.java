package com.github.oosm032519.playlistviewernext.controller.session;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpSession;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SessionCheckControllerTest {

    @InjectMocks
    private SessionCheckController sessionCheckController;

    private MockHttpSession mockSession;

    @BeforeEach
    void setUp() {
        mockSession = new MockHttpSession();
    }

    @Test
    void checkSession_WithAuthenticatedUser_ReturnsSuccessResponse() {
        // Given
        String accessToken = "sampleAccessToken";
        String userId = "sampleUserId";
        mockSession.setAttribute("accessToken", accessToken);
        mockSession.setAttribute("userId", userId);

        // When
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(mockSession);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "success")
                .containsEntry("message", "Access token is present")
                .containsEntry("userId", userId)
                .containsEntry("tokenPreview", "sampleAcce...");
    }

    @Test
    void checkSession_WithUnauthenticatedUser_ReturnsErrorResponse() {
        // Given
        // セッション属性を設定しない

        // When
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(mockSession);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "error")
                .containsEntry("message", "User not authenticated");
    }

    @Test
    void checkSession_WithOnlyAccessToken_ReturnsErrorResponse() {
        // Given
        String accessToken = "sampleAccessToken";
        mockSession.setAttribute("accessToken", accessToken);

        // When
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(mockSession);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "error")
                .containsEntry("message", "User not authenticated");
    }

    @Test
    void checkSession_WithOnlyUserId_ReturnsErrorResponse() {
        // Given
        String userId = "sampleUserId";
        mockSession.setAttribute("userId", userId);

        // When
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(mockSession);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "error")
                .containsEntry("message", "User not authenticated");
    }

    @Test
    void checkSession_WithLongAccessToken_ReturnsCorrectTokenPreview() {
        // Given
        String longAccessToken = "thisIsAVeryLongAccessTokenThatShouldBeTruncated";
        String userId = "sampleUserId";
        mockSession.setAttribute("accessToken", longAccessToken);
        mockSession.setAttribute("userId", userId);

        // When
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(mockSession);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "success")
                .containsEntry("tokenPreview", "thisIsAVer...");
    }

    @Test
    void checkSession_WithShortAccessToken_ReturnsFullTokenPreview() {
        // Given
        String shortAccessToken = "short";
        String userId = "sampleUserId";
        mockSession.setAttribute("accessToken", shortAccessToken);
        mockSession.setAttribute("userId", userId);

        // When
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(mockSession);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "success")
                .containsEntry("tokenPreview", "short...");
    }

    @Test
    void checkSession_WithNullAccessToken_ReturnsErrorResponse() {
        // Given
        String userId = "sampleUserId";
        mockSession.setAttribute("accessToken", null);
        mockSession.setAttribute("userId", userId);

        // When
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(mockSession);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "error")
                .containsEntry("message", "User not authenticated");
    }

    @Test
    void checkSession_WithNullUserId_ReturnsErrorResponse() {
        // Given
        String accessToken = "sampleAccessToken";
        mockSession.setAttribute("accessToken", accessToken);
        mockSession.setAttribute("userId", null);

        // When
        ResponseEntity<Map<String, Object>> response = sessionCheckController.checkSession(mockSession);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .containsEntry("status", "error")
                .containsEntry("message", "User not authenticated");
    }
}
