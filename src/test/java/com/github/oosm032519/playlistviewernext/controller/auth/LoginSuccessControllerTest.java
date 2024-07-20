package com.github.oosm032519.playlistviewernext.controller.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@WebMvcTest(LoginSuccessController.class)
@ExtendWith(MockitoExtension.class)
public class LoginSuccessControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OAuth2User principal;

    @InjectMocks
    private LoginSuccessController loginSuccessController;

    @Mock
    private org.slf4j.Logger logger;

    @Test
    public void testLoginSuccessWithValidUser() throws Exception {
        String userId = "testUserId";
        String accessToken = "testAccessToken";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", userId);
        attributes.put("access_token", accessToken);
        when(principal.getAttributes()).thenReturn(attributes);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttribute("access_token")).thenReturn(accessToken);
        when(principal.getName()).thenReturn("testUser");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login().oauth2User(principal)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost:3000"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(302);
        assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("http://localhost:3000");
        verify(logger).info(eq("User successfully authenticated. UserId: {}"), eq(userId));
        verify(logger).debug(eq("Access token: {}"), eq(accessToken));
    }

    @Test
    public void testLoginSuccessWithNullPrincipal() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess"))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/login?error"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(302);
        assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("/login?error");
        verify(logger).error("Authentication failed: OAuth2User is null");
    }

    @Test
    public void testLoginSuccessWithMissingUserInfo() throws Exception {
        Map<String, Object> attributes = new HashMap<>();
        when(principal.getAttributes()).thenReturn(attributes);
        when(principal.getAttribute("id")).thenReturn(null);
        when(principal.getAttribute("access_token")).thenReturn(null);
        when(principal.getName()).thenReturn("testUser");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login().oauth2User(principal)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost:3000"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(302);
        assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("http://localhost:3000");
        verify(logger).warn(eq("Missing user information. UserId: {}, AccessToken: {}"), isNull(), isNull());
    }

    @Test
    public void testLoginSuccessWithPartialUserInfo() throws Exception {
        String userId = "testUserId";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("id", userId);
        when(principal.getAttributes()).thenReturn(attributes);
        when(principal.getAttribute("id")).thenReturn(userId);
        when(principal.getAttribute("access_token")).thenReturn(null);
        when(principal.getName()).thenReturn("testUser");

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/loginSuccess")
                        .with(SecurityMockMvcRequestPostProcessors.oauth2Login().oauth2User(principal)))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrl("http://localhost:3000"))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(302);
        assertThat(result.getResponse().getRedirectedUrl()).isEqualTo("http://localhost:3000");
        verify(logger).warn(eq("Missing user information. UserId: {}, AccessToken: {}"), eq(userId), isNull());
    }
}
