package com.github.oosm032519.playlistviewernext.util;

import com.github.oosm032519.playlistviewernext.exception.AuthenticationException;
import com.github.oosm032519.playlistviewernext.exception.InvalidRequestException;
import com.google.crypto.tink.Aead;
import com.google.crypto.tink.KeyTemplates;
import com.google.crypto.tink.KeysetHandle;
import com.google.crypto.tink.aead.AeadConfig;
import com.google.crypto.tink.subtle.Hex;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Getter
    @Value("${backend.url}")
    private String issuer;

    @Getter
    @Value("${frontend.url}")
    private String audience;

    private JWSSigner signer;
    private JWSVerifier verifier;
    private Aead aead;

    @PostConstruct
    public void init() {
        logger.info("JwtUtil初期化開始");
        try {
            OctetKeyPair octetKeyPair = new OctetKeyPairGenerator(Curve.Ed25519)
                    .keyID(secret)
                    .generate();

            this.signer = new Ed25519Signer(octetKeyPair);
            this.verifier = new Ed25519Verifier(octetKeyPair.toPublicJWK());

            AeadConfig.register();
            KeysetHandle keysetHandle = KeysetHandle.generateNew(
                    KeyTemplates.get("XCHACHA20_POLY1305"));
            this.aead = keysetHandle.getPrimitive(Aead.class);

            logger.debug("署名者、検証者、暗号化器の生成に成功しました。");
        } catch (JOSEException | GeneralSecurityException e) {
            logger.error("初期化中にエラーが発生しました", e);
            if (e.getCause() != null) {
                logger.error("原因: ", e.getCause());
            }
            throw new AuthenticationException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "JWT_UTIL_INIT_ERROR",
                    "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    e
            );
        }
        logger.info("JwtUtil初期化完了");
    }

    public String generateToken(Map<String, Object> claims) {
        logger.info("トークン生成開始");
        logger.debug("クレーム: {}", claims);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000); // 1時間
        try {
            JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .audience(audience)
                    .expirationTime(expiration)
                    .issueTime(now);

            claims.forEach(claimsSetBuilder::claim);

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.EdDSA), claimsSetBuilder.build());
            signedJWT.sign(signer);

            byte[] associatedData = "JWT".getBytes();
            byte[] ciphertext = aead.encrypt(signedJWT.serialize().getBytes(), associatedData);

            String token = Hex.encode(ciphertext);
            logger.info("トークン生成成功");
            logger.debug("生成されたトークン: {}", token);
            return token;
        } catch (JOSEException | GeneralSecurityException e) {
            logger.error("トークン生成中にエラーが発生しました", e);
            throw new AuthenticationException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "TOKEN_GENERATION_ERROR",
                    "システムエラーが発生しました。しばらく時間をおいてから再度お試しください。",
                    e
            );
        }
    }

    public Map<String, Object> validateToken(String token) {
        logger.info("トークン検証開始");
        logger.debug("検証対象トークン: {}", token);
        try {
            byte[] ciphertext = Hex.decode(token);
            byte[] associatedData = "JWT".getBytes();
            byte[] plaintext = aead.decrypt(ciphertext, associatedData);

            SignedJWT signedJWT = SignedJWT.parse(new String(plaintext));
            if (!signedJWT.verify(verifier)) {
                logger.warn("トークンの署名が無効です");
                throw new AuthenticationException(
                        HttpStatus.UNAUTHORIZED,
                        "INVALID_TOKEN_SIGNATURE",
                        "セッションが有効期限切れか、無効です。再度ログインしてください。"
                );
            }

            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            Date expirationTime = claimsSet.getExpirationTime();
            if (expirationTime != null && expirationTime.before(new Date())) {
                logger.warn("トークンの有効期限が切れています");
                throw new AuthenticationException(
                        HttpStatus.UNAUTHORIZED,
                        "TOKEN_EXPIRED",
                        "セッションが有効期限切れです。再度ログインしてください。"
                );
            }

            Map<String, Object> claims = claimsSet.getClaims();
            logger.info("トークン検証成功");
            logger.debug("検証されたクレーム: {}", claims);
            return claims;
        } catch (IllegalArgumentException e) {
            // IllegalArgumentException を InvalidRequestException にラップ
            logger.error("トークン検証中にエラーが発生しました: 不正なトークン形式です。", e);
            throw new InvalidRequestException(
                    HttpStatus.BAD_REQUEST,
                    "TOKEN_VALIDATION_ERROR",
                    "ログイン処理中にエラーが発生しました。再度ログインしてください。",
                    e
            );
        } catch (JOSEException | ParseException | GeneralSecurityException e) {
            logger.error("トークン検証中にエラーが発生しました", e);
            throw new InvalidRequestException(
                    HttpStatus.BAD_REQUEST,
                    "TOKEN_VALIDATION_ERROR",
                    "ログイン処理中にエラーが発生しました。再度ログインしてください。",
                    e
            );
        }
    }
}
