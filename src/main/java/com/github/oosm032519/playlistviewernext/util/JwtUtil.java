package com.github.oosm032519.playlistviewernext.util;

import com.nimbusds.jose.*;
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

    @PostConstruct
    public void init() {
        logger.info("JwtUtil初期化開始");
        try {
            this.signer = new MACSigner(secret);
            this.verifier = new MACVerifier(secret);
            logger.debug("署名者と検証者の生成に成功しました。");
        } catch (JOSEException e) {
            logger.error("署名者と検証者の生成中にエラーが発生しました", e);
            throw new RuntimeException("署名者と検証者の初期化に失敗しました", e);
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

            SignedJWT signedJWT = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSetBuilder.build());
            signedJWT.sign(signer);

            String token = signedJWT.serialize();
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
            SignedJWT signedJWT = SignedJWT.parse(token);
            if (!signedJWT.verify(verifier)) {
                logger.warn("トークンの署名が無効です");
                throw new JOSEException("トークンの署名が無効です");
            }

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
