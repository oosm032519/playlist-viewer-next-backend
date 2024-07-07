package com.github.oosm032519.playlistviewernext.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private OAuth2User principal;

    private final AuthService authService = new AuthService();

    @Test
    void getAccessToken_成功時() {
        // Arrange
        String accessToken = "validAccessToken";
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("access_token", accessToken);
        when(principal.getAttributes()).thenReturn(attributes);

        // Act
        String result = authService.getAccessToken(principal);

        // Assert
        assertThat(result).isEqualTo(accessToken);
    }

    @Test
    void getAccessToken_認証されていない場合() {
        // Act
        String result = authService.getAccessToken(null);

        // Assert
        assertThat(result).isNull();
    }

    @Test
    void getAccessToken_アクセストークンがない場合() {
        // Arrange
        when(principal.getAttributes()).thenReturn(new HashMap<>());

        // Act
        String result = authService.getAccessToken(principal);

        // Assert
        assertThat(result).isNull();
    }
}
