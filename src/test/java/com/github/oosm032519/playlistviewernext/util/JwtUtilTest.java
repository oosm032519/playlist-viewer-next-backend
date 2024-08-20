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

import java.security.GeneralSecurityException;
import java.util.Date;
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
    void testGenerateAndValidateToken() {
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
    void testInitializationWithJOSEException() {
        JwtUtil spyJwtUtil = spy(new JwtUtil());
        ReflectionTestUtils.setField(spyJwtUtil, "secret", "testSecret");
        ReflectionTestUtils.setField(spyJwtUtil, "issuer", "testIssuer");
        ReflectionTestUtils.setField(spyJwtUtil, "audience", "testAudience");

        doAnswer(invocation -> {
            throw new AuthenticationException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "JWT_UTIL_INIT_ERROR",
                    "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    new JOSEException("JOSE Exception")
            );
        }).when(spyJwtUtil).init();

        assertThatThrownBy(spyJwtUtil::init)
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。")
                .hasCauseInstanceOf(JOSEException.class);
    }

    @Test
    void testInitializationWithGeneralSecurityException() {
        JwtUtil spyJwtUtil = spy(new JwtUtil());
        ReflectionTestUtils.setField(spyJwtUtil, "secret", "testSecret");
        ReflectionTestUtils.setField(spyJwtUtil, "issuer", "testIssuer");
        ReflectionTestUtils.setField(spyJwtUtil, "audience", "testAudience");

        doAnswer(invocation -> {
            throw new AuthenticationException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "JWT_UTIL_INIT_ERROR",
                    "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    new GeneralSecurityException("Security Exception")
            );
        }).when(spyJwtUtil).init();

        assertThatThrownBy(spyJwtUtil::init)
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。")
                .hasCauseInstanceOf(GeneralSecurityException.class);
    }

    @Test
    void testGenerateTokenWithJOSEException() {
        JwtUtil spyJwtUtil = spy(jwtUtil);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");

        doAnswer(invocation -> {
            throw new AuthenticationException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TOKEN_GENERATION_ERROR",
                    "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    new JOSEException("JOSE Exception")
            );
        }).when(spyJwtUtil).generateToken(any());

        assertThatThrownBy(() -> spyJwtUtil.generateToken(claims))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。")
                .hasCauseInstanceOf(JOSEException.class);
    }

    @Test
    void testGenerateTokenWithGeneralSecurityException() {
        JwtUtil spyJwtUtil = spy(jwtUtil);
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");

        doAnswer(invocation -> {
            throw new AuthenticationException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TOKEN_GENERATION_ERROR",
                    "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    new GeneralSecurityException("Security Exception")
            );
        }).when(spyJwtUtil).generateToken(any());

        assertThatThrownBy(() -> spyJwtUtil.generateToken(claims))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。")
                .hasCauseInstanceOf(GeneralSecurityException.class);
    }

    @Test
    void testValidateTokenWithInvalidSignature() {
        JwtUtil spyJwtUtil = spy(jwtUtil);
        String invalidToken = "invalidToken";

        doAnswer(invocation -> {
            throw new InvalidRequestException(
                    HttpStatus.BAD_REQUEST,
                    "INVALID_TOKEN",
                    "ログイン処理中にエラーが発生しました。再度ログインしてください。",
                    new JOSEException("Invalid signature")
            );
        }).when(spyJwtUtil).validateToken(invalidToken);

        assertThatThrownBy(() -> spyJwtUtil.validateToken(invalidToken))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("ログイン処理中にエラーが発生しました。再度ログインしてください。")
                .hasCauseInstanceOf(JOSEException.class);
    }

    @Test
    void testValidateTokenWithExpiredToken() {
        JwtUtil spyJwtUtil = spy(jwtUtil);
        String expiredToken = "expiredToken";

        doThrow(new AuthenticationException(
                HttpStatus.UNAUTHORIZED,
                "TOKEN_EXPIRED",
                "セッションが有効期限切れです。再度ログインしてください。"
        )).when(spyJwtUtil).validateToken(expiredToken);

        assertThatThrownBy(() -> spyJwtUtil.validateToken(expiredToken))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("セッションが有効期限切れです。再度ログインしてください。");
    }

    @Test
    void testSecretIssuerAudienceSetCorrectly() {
        assertThat(ReflectionTestUtils.getField(jwtUtil, "secret")).isEqualTo("testSecret");
        assertThat(jwtUtil.getIssuer()).isEqualTo("testIssuer");
        assertThat(jwtUtil.getAudience()).isEqualTo("testAudience");
    }

    @Test
    void testGenerateTokenDetails() {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");
        claims.put("username", "testUser");

        String token = jwtUtil.generateToken(claims);
        Map<String, Object> validatedClaims = jwtUtil.validateToken(token);

        assertThat(validatedClaims).containsEntry("iss", "testIssuer");
        assertThat(validatedClaims).containsEntry("aud", List.of("testAudience"));
        assertThat(validatedClaims).containsKey("exp");
        assertThat(validatedClaims).containsKey("iat");
        assertThat((Date) validatedClaims.get("exp")).isAfter(new Date());
    }

    @Test
    void testValidateTokenWithMalformedToken() {
        assertThatThrownBy(() -> jwtUtil.validateToken("malformedToken"))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("ログイン処理中にエラーが発生しました。再度ログインしてください。");
    }

    @Test
    void testGenerateTokenWithEmptyClaims() {
        Map<String, Object> emptyClaims = new HashMap<>();
        String token = jwtUtil.generateToken(emptyClaims);
        assertThat(token).isNotEmpty();

        Map<String, Object> validatedClaims = jwtUtil.validateToken(token);
        assertThat(validatedClaims)
                .containsKey("exp")
                .containsKey("iat")
                .containsEntry("iss", "testIssuer")
                .containsEntry("aud", List.of("testAudience"));
    }

    @Test
    void testValidateTokenWithInvalidIssuer() {
        JwtUtil invalidIssuerJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(invalidIssuerJwtUtil, "secret", "testSecret");
        ReflectionTestUtils.setField(invalidIssuerJwtUtil, "issuer", "invalidIssuer");
        ReflectionTestUtils.setField(invalidIssuerJwtUtil, "audience", "testAudience");
        invalidIssuerJwtUtil.init();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");
        String token = invalidIssuerJwtUtil.generateToken(claims);

        assertThatThrownBy(() -> jwtUtil.validateToken(token))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("ログイン処理中にエラーが発生しました。再度ログインしてください。");
    }

    @Test
    void testValidateTokenWithInvalidAudience() {
        JwtUtil invalidAudienceJwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(invalidAudienceJwtUtil, "secret", "testSecret");
        ReflectionTestUtils.setField(invalidAudienceJwtUtil, "issuer", "testIssuer");
        ReflectionTestUtils.setField(invalidAudienceJwtUtil, "audience", "invalidAudience");
        invalidAudienceJwtUtil.init();

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", "123");
        String token = invalidAudienceJwtUtil.generateToken(claims);

        assertThatThrownBy(() -> jwtUtil.validateToken(token))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("ログイン処理中にエラーが発生しました。再度ログインしてください。");
    }
}
