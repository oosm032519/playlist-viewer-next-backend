package com.github.oosm032519.playlistviewernext.controller.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebMvcTest(LoginSuccessController.class)
@ExtendWith(OutputCaptureExtension.class)
public class LoginSuccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2User principal;

    @InjectMocks
    private LoginSuccessController loginSuccessController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testLoginSuccess(CapturedOutput output) throws Exception {
        // Arrange
        String userId = "testUserId";
        String accessToken = "testAccessToken";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", userId);
        attributes.put("access_token", accessToken);
        when(principal.getAttributes()).thenReturn(attributes);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttribute("access_token")).thenReturn(accessToken);
        when(principal.getName()).thenReturn("testUser");

        // Act
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login().oauth2User(principal)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost:3000/"))
                .andReturn();

        // Assert
        assertThat(output).contains("User successfully authenticated: testUserId");
        assertThat(output).contains("Access token: testAccessToken");
        assertThat(result.getResponse().getStatus()).isEqualTo(302);
        assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("http://localhost:3000/");
    }
}