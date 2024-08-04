package com.github.oosm032519.playlistviewernext.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDHDecrypter;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.public.key}")
    private String publicKey;

    @Value("${jwt.private.key}")
    private String privateKey;

    @Getter
    @Value("${backend.url}")
    private String issuer;

    @Getter
    @Value("${frontend.url}")
    private String audience;

    private JWSSigner signer;
    private JWSVerifier verifier;
    private JWEEncrypter encrypter;
    private JWEDecrypter decrypter;

    @PostConstruct
    public void init() {
        logger.info("JwtUtil初期化開始");
        try {
            // HMAC署名用のsignerとverifierを初期化
            this.signer = new MACSigner(secret);
            this.verifier = new MACVerifier(secret);

            // EC キーペアの生成
            ECPublicKey ecPublicKey = (ECPublicKey) KeyFactory.getInstance("EC")
                    .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKey)));

            ECPrivateKey ecPrivateKey = (ECPrivateKey) KeyFactory.getInstance("EC")
                    .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));

            // ECDH暗号化用のencrypterとdecrypterを初期化
            this.encrypter = new ECDHEncrypter(ecPublicKey);
            this.decrypter = new ECDHDecrypter(ecPrivateKey);

            logger.debug("Public Key Length: {}", publicKey.length());
            logger.debug("Private Key Length: {}", privateKey.length());
            logger.debug("署名者、検証者、暗号化器、復号器の生成に成功しました。");
        } catch (JOSEException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            logger.error("初期化中にエラーが発生しました", e);
            if (e.getCause() != null) {
                logger.error("原因: ", e.getCause());
            }
            throw new RuntimeException("JwtUtilの初期化に失敗しました", e);
        }
        logger.info("JwtUtil初期化完了");
    }

    public String generateToken(Map<String, Object> claims) {
        logger.info("トークン生成開始");
        logger.debug("クレーム: {}", claims);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000); // 1時間
        try {
            // JWTクレームセットの構築
            JWTClaimsSet.Builder claimsSetBuilder = new JWTClaimsSet.Builder()
                    .issuer(issuer)
                    .audience(audience)
                    .expirationTime(expiration)
                    .issueTime(now);

            claims.forEach(claimsSetBuilder::claim);

            // 署名付きJWTの作成
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSetBuilder.build());
            signedJWT.sign(signer);

            // JWEオブジェクトの作成と暗号化
            JWEObject jweObject = new JWEObject(
                    new JWEHeader.Builder(JWEAlgorithm.ECDH_ES_A256KW, EncryptionMethod.XC20P)
                            .contentType("JWT")
                            .build(),
                    new Payload(signedJWT)
            );
            jweObject.encrypt(encrypter);

            String token = jweObject.serialize();
            logger.info("トークン生成成功");
            logger.debug("生成されたトークン: {}", token);
            return token;
        } catch (JOSEException e) {
            logger.error("トークン生成中にエラーが発生しました", e);
            throw new RuntimeException("トークンの生成に失敗しました", e);
        }
    }

    public Map<String, Object> validateToken(String token) throws JOSEException, ParseException {
        logger.info("トークン検証開始");
        logger.debug("検証対象トークン: {}", token);
        try {
            // JWEオブジェクトの解析と復号
            JWEObject jweObject = JWEObject.parse(token);
            jweObject.decrypt(decrypter);

            // 署名付きJWTの取得と検証
            SignedJWT signedJWT = jweObject.getPayload().toSignedJWT();
            if (!signedJWT.verify(verifier)) {
                logger.warn("トークンの署名が無効です");
                throw new JOSEException("トークンの署名が無効です");
            }

            // クレームセットの取得と有効期限の確認
            JWTClaimsSet claimsSet = signedJWT.getJWTClaimsSet();
            Date expirationTime = claimsSet.getExpirationTime();
            if (expirationTime != null && expirationTime.before(new Date())) {
                logger.warn("トークンの有効期限が切れています");
                throw new JOSEException("トークンの有効期限が切れています");
            }

            Map<String, Object> claims = claimsSet.getClaims();
            logger.info("トークン検証成功");
            logger.debug("検証されたクレーム: {}", claims);
            return claims;
        } catch (ParseException e) {
            logger.warn("トークンの形式が不正です", e);
            throw new JOSEException("トークンの形式が不正です", e);
        } catch (JOSEException e) {
            logger.error("トークン検証中に予期せぬエラーが発生しました", e);
            throw e;
        }
    }
}
