package com.github.oosm032519.playlistviewernext.util;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.ECDHDecrypter;
import com.nimbusds.jose.crypto.ECDHEncrypter;
import com.nimbusds.jose.crypto.Ed25519Signer;
import com.nimbusds.jose.crypto.Ed25519Verifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.OctetKeyPair;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jose.jwk.gen.OctetKeyPairGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
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
    private JWEEncrypter encrypter;
    private JWEDecrypter decrypter;

    @PostConstruct
    public void init() {
        logger.info("JwtUtil初期化開始");
        try {
            // Ed25519キーペアの生成 (署名用)
            OctetKeyPair octetKeyPair = new OctetKeyPairGenerator(Curve.Ed25519)
                    .keyID(secret)
                    .generate();

            // EdDSA署名用のsignerとverifierを初期化
            this.signer = new Ed25519Signer(octetKeyPair);
            this.verifier = new Ed25519Verifier(octetKeyPair.toPublicJWK());

            // ECキーペアの生成 (暗号化用、P-521曲線を使用)
            ECKey ecKey = new ECKeyGenerator(Curve.P_521)
                    .keyID(secret)
                    .generate();
            ECPublicKey ecPublicKey = ecKey.toECPublicKey();
            ECPrivateKey ecPrivateKey = ecKey.toECPrivateKey();

            // ECDH暗号化用のencrypterとdecrypterを初期化
            this.encrypter = new ECDHEncrypter(ecPublicKey);
            this.decrypter = new ECDHDecrypter(ecPrivateKey);

            logger.debug("署名者、検証者、暗号化器、復号器の生成に成功しました。");
        } catch (JOSEException e) {
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
            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.EdDSA), claimsSetBuilder.build());
            signedJWT.sign(signer);

            // JWEヘッダーの作成
            JWEHeader header = new JWEHeader.Builder(JWEAlgorithm.ECDH_ES_A256KW, EncryptionMethod.A256GCM)
                    .contentType("JWT")
                    .build();

            // JWEオブジェクトの作成と暗号化
            JWEObject jweObject = new JWEObject(header, new Payload(signedJWT));
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
