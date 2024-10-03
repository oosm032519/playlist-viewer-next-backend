package com.github.oosm032519.playlistviewernext.controller.auth;

import com.github.oosm032519.playlistviewernext.service.auth.SpotifyAuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifyClientCredentialsAuthenticationTest {

    @Mock
    private SpotifyAuthService authService;

    @Mock
    private HttpServletRequest request;

    @InjectMocks
    private SpotifyClientCredentialsAuthentication authController;

    @Test
    void authenticate_Successfully() {
        authController.authenticate();
        verify(authService, times(1)).getClientCredentialsToken();
    }

    @Test
    void authenticate_HandlesSpotifyApiExceptionGracefully() {
        SpotifyApiException spotifyApiException = new SpotifyApiException(
                HttpStatus.BAD_REQUEST,
                "TEST_ERROR",
                "Test error message",
                "Test error details"
        );
        doThrow(spotifyApiException).when(authService).getClientCredentialsToken();

        SpotifyApiException thrownException = assertThrows(SpotifyApiException.class, () -> authController.authenticate());

        verify(authService, times(1)).getClientCredentialsToken();
        assertEquals(spotifyApiException, thrownException);
    }

    @Test
    void authenticate_HandlesGenericExceptionGracefully() {
        doThrow(new RuntimeException("Auth error")).when(authService).getClientCredentialsToken();

        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("testParam", new String[]{"testValue"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        SpotifyApiException exception = assertThrows(SpotifyApiException.class, () -> authController.authenticate());

        verify(authService, times(1)).getClientCredentialsToken();
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getHttpStatus());
        assertEquals("CLIENT_CREDENTIALS_AUTH_ERROR", exception.getErrorCode());
        assertEquals("Spotify APIへの接続中にエラーが発生しました。しばらく時間をおいてから再度お試しください。", exception.getMessage());
        assertEquals("リクエストパラメータ: testParam=testValue", exception.getDetails());
    }

    @Test
    void getRequestParams_ReturnsCorrectString() {
        Map<String, String[]> parameterMap = new HashMap<>();
        parameterMap.put("param1", new String[]{"value1"});
        parameterMap.put("param2", new String[]{"value2", "value3"});
        when(request.getParameterMap()).thenReturn(parameterMap);

        String result = authController.getRequestParams();

        assertTrue(result.contains("param1=value1"));
        assertTrue(result.contains("param2=value2,value3"));
    }

    @Test
    void getRequestParams_ReturnsEmptyStringForNoParams() {
        when(request.getParameterMap()).thenReturn(new HashMap<>());

        String result = authController.getRequestParams();

        assertEquals("", result);
    }
}
