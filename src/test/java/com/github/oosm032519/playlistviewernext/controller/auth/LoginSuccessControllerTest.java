package com.github.oosm032519.playlistviewernext.controller.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoginSuccessControllerTest {

    @InjectMocks
    private LoginSuccessController loginSuccessController;

    @Mock
    private OAuth2User principal;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loginSuccessController, "frontendUrl", "http://localhost:3000");
    }

    @Test
    public void testLoginSuccessWithValidUser() {
        String userId = "testUserId";
        String accessToken = "testAccessToken";

        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttribute("access_token")).thenReturn(accessToken);

        String result = loginSuccessController.loginSuccess(principal);

        assertThat(result).isEqualTo("redirect:http://localhost:3000");
    }

    @Test
    public void testLoginSuccessWithNullPrincipal() {
        String result = loginSuccessController.loginSuccess(null);

        assertThat(result).isEqualTo("redirect:/login?error");
    }

    @Test
    public void testLoginSuccessWithMissingUserInfo() {
        when(principal.getAttribute("id")).thenReturn(null);
        when(principal.getAttribute("access_token")).thenReturn(null);

        String result = loginSuccessController.loginSuccess(principal);

        assertThat(result).isEqualTo("redirect:http://localhost:3000");
    }

    @Test
    public void testLoginSuccessWithNullUserId() {
        when(principal.getAttribute("id")).thenReturn(null);
        when(principal.getAttribute("access_token")).thenReturn("testAccessToken");

        String result = loginSuccessController.loginSuccess(principal);

        assertThat(result).isEqualTo("redirect:http://localhost:3000");
    }

    @Test
    public void testLoginSuccessWithNullAccessToken() {
        when(principal.getAttribute("id")).thenReturn("testUserId");
        when(principal.getAttribute("access_token")).thenReturn(null);

        String result = loginSuccessController.loginSuccess(principal);

        assertThat(result).isEqualTo("redirect:http://localhost:3000");
    }
}
