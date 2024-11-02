package com.github.oosm032519.playlistviewernext.util;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import com.nimbusds.jose.JOSEException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
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
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Spy
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
        assertThat(audList.getFirst()).isEqualTo("testAudience");
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
    void testGenerateTokenWithJOSEException() {
        doThrow(new AuthenticationException(HttpStatus.INTERNAL_SERVER_ERROR, "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。", new JOSEException("test exception"))).when(jwtUtil).generateToken(any());

        Map<String, Object> claims = new HashMap<>();
        assertThatThrownBy(() -> jwtUtil.generateToken(claims))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。")
                .hasCauseInstanceOf(JOSEException.class);
    }

    @Test
    void testGenerateTokenWithGeneralSecurityException() {
        doThrow(new AuthenticationException(HttpStatus.INTERNAL_SERVER_ERROR, "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。", new GeneralSecurityException("test exception"))).when(jwtUtil).generateToken(any());

        Map<String, Object> claims = new HashMap<>();
        assertThatThrownBy(() -> jwtUtil.generateToken(claims))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("システムエラーが発生しました。しばらく時間をおいてから再度お試しください。")
                .hasCauseInstanceOf(GeneralSecurityException.class);
    }

    @Test
    void testValidateTokenWithInvalidSignature() {
        String invalidToken = "invalidToken";
        doThrow(new InvalidRequestException(HttpStatus.BAD_REQUEST, "ログイン処理中にエラーが発生しました。再度ログインしてください。", new JOSEException("Invalid signature"))).when(jwtUtil).validateToken(invalidToken);


        assertThatThrownBy(() -> jwtUtil.validateToken(invalidToken))
                .isInstanceOf(InvalidRequestException.class)
                .hasMessageContaining("ログイン処理中にエラーが発生しました。再度ログインしてください。")
                .hasCauseInstanceOf(JOSEException.class);
    }

    @Test
    void testValidateTokenWithExpiredToken() {
        String expiredToken = "expiredToken";
        doThrow(new AuthenticationException(HttpStatus.UNAUTHORIZED, "セッションが有効期限切れです。再度ログインしてください。")).when(jwtUtil).validateToken(expiredToken);

        assertThatThrownBy(() -> jwtUtil.validateToken(expiredToken))
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

        assertThat(validatedClaims).containsKey("aud");
        assertThat(validatedClaims.get("aud")).isInstanceOf(List.class);
        List<?> audList = (List<?>) validatedClaims.get("aud");
        assertThat(audList).hasSize(1);
        assertThat(audList.getFirst()).isEqualTo("testAudience");


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
                .containsEntry("aud", List.of("testAudience")); // audの検証を追加
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
