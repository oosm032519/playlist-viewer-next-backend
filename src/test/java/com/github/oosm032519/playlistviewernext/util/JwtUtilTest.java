package com.github.oosm032519.playlistviewernext.util;

import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecret");
        ReflectionTestUtils.setField(jwtUtil, "issuer", "testIssuer");
        ReflectionTestUtils.setField(jwtUtil, "audience", "testAudience");
        jwtUtil.init();
    }

    @Test
    void testGenerateAndValidateToken() throws JOSEException, ParseException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");
        claims.put("username", "testUser");

        String token = jwtUtil.generateToken(claims);
        assertThat(token).isNotEmpty();

        Map<String, Object> validatedClaims = jwtUtil.validateToken(token);

        assertThat(validatedClaims)
                .containsEntry("userId", "123")
                .containsEntry("username", "testUser")
                .containsKey("exp")
                .containsKey("iat")
                .containsEntry("iss", "testIssuer");

        // audienceの検証を修正
        assertThat(validatedClaims).containsKey("aud");
        assertThat(validatedClaims.get("aud")).isInstanceOf(List.class);
        List<?> audList = (List<?>) validatedClaims.get("aud");
        assertThat(audList).hasSize(1);
        assertThat(audList.get(0)).isEqualTo("testAudience");
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        assertThatThrownBy(() -> jwtUtil.validateToken("invalidToken"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("input is not hexadecimal");
    }

    @Test
    void testGetIssuerAndAudience() {
        assertThat(jwtUtil.getIssuer()).isEqualTo("testIssuer");
        assertThat(jwtUtil.getAudience()).isEqualTo("testAudience");
    }
}
