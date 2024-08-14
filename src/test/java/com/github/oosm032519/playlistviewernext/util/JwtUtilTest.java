package com.github.oosm032519.playlistviewernext.util;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

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

        assertThat(validatedClaims).containsKey("aud");
        assertThat(validatedClaims.get("aud")).isInstanceOf(List.class);
        List<?> audList = (List<?>) validatedClaims.get("aud");
        assertThat(audList).hasSize(1);
        assertThat(audList.get(0)).isEqualTo("testAudience");
    }

    @Test
    void testValidateTokenWithInvalidToken() {
        assertThatThrownBy(() -> jwtUtil.validateToken("invalidToken"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("ログイン処理中にエラーが発生しました。再度ログインしてください。");
    }

    @Test
    void testGetIssuerAndAudience() {
        assertThat(jwtUtil.getIssuer()).isEqualTo("testIssuer");
        assertThat(jwtUtil.getAudience()).isEqualTo("testAudience");
    }

    @Test
    void testInitializationFailure() {
        JwtUtil spyJwtUtil = spy(new JwtUtil());
        ReflectionTestUtils.setField(spyJwtUtil, "secret", "testSecret");
        ReflectionTestUtils.setField(spyJwtUtil, "issuer", "testIssuer");
        ReflectionTestUtils.setField(spyJwtUtil, "audience", "testAudience");

        doThrow(new AuthenticationException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "JWT_UTIL_INIT_ERROR",
                "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                new RuntimeException("Initialization failed")
        )).when(spyJwtUtil).init();

        assertThatThrownBy(() -> spyJwtUtil.init())
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。");
    }

    @Test
    void testGenerateTokenFailure() {
        JwtUtil spyJwtUtil = spy(jwtUtil);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");

        doThrow(new AuthenticationException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "TOKEN_GENERATION_ERROR",
                "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                new RuntimeException("Token generation failed")
        )).when(spyJwtUtil).generateToken(any());

        assertThatThrownBy(() -> spyJwtUtil.generateToken(claims))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。");
    }

    @Test
    void testValidateTokenWithExpiredToken() throws JOSEException, ParseException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");

        String token = jwtUtil.generateToken(claims);

        JwtUtil spyJwtUtil = spy(jwtUtil);
        doAnswer(invocation -> {
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "TOKEN_EXPIRED",
                    "セッションが有効期限切れです。再度ログインしてください。"
            );
        }).when(spyJwtUtil).validateToken(token);

        assertThatThrownBy(() -> spyJwtUtil.validateToken(token))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("セッションが有効期限切れです。再度ログインしてください。");
    }

    @Test
    void testValidateTokenWithInvalidSignature() throws JOSEException, ParseException {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");

        String token = jwtUtil.generateToken(claims);

        JwtUtil spyJwtUtil = spy(jwtUtil);
        doAnswer(invocation -> {
            throw new AuthenticationException(
                    HttpStatus.UNAUTHORIZED,
                    "INVALID_TOKEN_SIGNATURE",
                    "セッションが有効期限切れか、無効です。再度ログインしてください。"
            );
        }).when(spyJwtUtil).validateToken(token);

        assertThatThrownBy(() -> spyJwtUtil.validateToken(token))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("セッションが有効期限切れか、無効です。再度ログインしてください。");
    }
}
