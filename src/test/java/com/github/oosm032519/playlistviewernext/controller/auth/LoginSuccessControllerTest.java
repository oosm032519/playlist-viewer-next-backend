package com.github.oosm032519.playlistviewernext.controller.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginSuccessControllerTest {

    private static final String FRONTEND_URL = "http://localhost:3000";
    @Mock
    private HttpServletRequest mockRequest;

    @InjectMocks
    private LoginSuccessController loginSuccessController;
    @Mock
    private HttpSession mockSession;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loginSuccessController, "frontendUrl", FRONTEND_URL);
    }

    @Test
    void loginSuccess_WithValidUserId_ShouldRedirectToFrontend() {
        // Arrange
        String userId = "testUser123";
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("userId")).thenReturn(userId);

        // Act
        String result = loginSuccessController.loginSuccess(mockRequest);

        // Assert
        assertThat(result).isEqualTo("redirect:" + FRONTEND_URL);
    }

    @Test
    void loginSuccess_WithNullUserId_ShouldRedirectToLoginError() {
        // Arrange
        when(mockRequest.getSession()).thenReturn(mockSession);
        when(mockSession.getAttribute("userId")).thenReturn(null);

        // Act
        String result = loginSuccessController.loginSuccess(mockRequest);

        // Assert
        assertThat(result).isEqualTo("redirect:/login?error");
    }
}
