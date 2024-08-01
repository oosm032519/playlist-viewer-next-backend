package com.github.oosm032519.playlistviewernext.controller.auth;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class LogoutControllerTest {

    private LogoutController logoutController;

    @Mock
    private SecurityContextLogoutHandler securityContextLogoutHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private ListAppender<ILoggingEvent> listAppender;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        logoutController = new LogoutController(securityContextLogoutHandler);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        Logger logger = (Logger) LoggerFactory.getLogger(LogoutController.class);
        listAppender = new ListAppender<>();
        logger.addAppender(listAppender);
        listAppender.start();
    }

    @Test
    void logout_正常系_ログアウト成功() {
        request.setRemoteAddr("192.168.1.1");

        ResponseEntity<String> responseEntity = logoutController.logout(request, response);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(200);
        assertThat(responseEntity.getBody()).isEqualTo("Logged out successfully");

        String setCookieHeader = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).contains("JWT=");
        assertThat(setCookieHeader).contains("Max-Age=0");
        assertThat(setCookieHeader).contains("Path=/");
        assertThat(setCookieHeader).contains("Secure");
        assertThat(setCookieHeader).contains("HttpOnly");
        assertThat(setCookieHeader).contains("SameSite=None");
    }

    @Test
    void logout_異常系_例外発生時() {
        request.setRemoteAddr("192.168.1.1");
        doThrow(new RuntimeException("Simulated error")).when(securityContextLogoutHandler).logout(any(), any(), any());

        ResponseEntity<String> responseEntity = logoutController.logout(request, response);

        assertThat(responseEntity.getStatusCodeValue()).isEqualTo(500);
        assertThat(responseEntity.getBody()).isEqualTo("ログアウト処理中にエラーが発生しました");
    }

    @Test
    void logout_ログ出力の検証() {
        request.setRemoteAddr("192.168.1.1");

        logoutController.logout(request, response);

        List<ILoggingEvent> logsList = listAppender.list;
        assertThat(logsList).extracting("formattedMessage")
                .contains("ログアウト処理を開始します。リクエスト元IP: 192.168.1.1")
                .contains("セキュリティコンテキストのクリアが完了しました")
                .contains("JWTクッキーの無効化が完了しました。クッキー設定: JWT=; Path=/; Max-Age=0; Expires=Thu, 01 Jan 1970 00:00:00 GMT; Secure; HttpOnly; SameSite=None")
                .contains("ログアウト処理が正常に完了しました");
    }

    @Test
    void logout_セキュリティコンテキストクリアの検証() {
        request.setRemoteAddr("192.168.1.1");

        logoutController.logout(request, response);

        verify(securityContextLogoutHandler, times(1)).logout(eq(request), eq(response), eq(null));
    }

    @Test
    void logout_クッキー属性の詳細検証() {
        request.setRemoteAddr("192.168.1.1");

        ResponseEntity<String> responseEntity = logoutController.logout(request, response);

        String setCookieHeader = responseEntity.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        assertThat(setCookieHeader).contains("JWT=");
        assertThat(setCookieHeader).contains("Max-Age=0");
        assertThat(setCookieHeader).contains("Path=/");
        assertThat(setCookieHeader).contains("Secure");
        assertThat(setCookieHeader).contains("HttpOnly");
        assertThat(setCookieHeader).contains("SameSite=None");

        assertThat(setCookieHeader).matches("JWT=;.*");
    }
}
