package com.github.oosm032519.playlistviewernext.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
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

    private Key key;

    @PostConstruct
    public void init() {
        logger.info("JwtUtil初期化開始");
        try {
            this.key = Keys.hmacShaKeyFor(secret.getBytes());
            logger.debug("秘密鍵の生成に成功しました。キーアルゴリズム: {}", key.getAlgorithm());
        } catch (Exception e) {
            logger.error("秘密鍵の生成中にエラーが発生しました", e);
            throw new RuntimeException("秘密鍵の初期化に失敗しました", e);
        }
        logger.info("JwtUtil初期化完了");
    }

    public String generateToken(Map<String, Object> claims) {
        logger.info("トークン生成開始");
        logger.debug("クレーム: {}", claims);
        Date now = new Date();
        Date expiration = new Date(now.getTime() + 3600000); // 1時間
        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(now)
                    .setExpiration(expiration)
                    .setIssuer(issuer)
                    .setAudience(audience)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
            logger.info("トークン生成成功");
            logger.debug("生成されたトークン: {}", token);
            return token;
        } catch (Exception e) {
            logger.error("トークン生成中にエラーが発生しました", e);
            throw new JwtException("トークンの生成に失敗しました", e);
        }
    }

    public Map<String, Object> validateToken(String token) throws JwtException {
        logger.info("トークン検証開始");
        logger.debug("検証対象トークン: {}", token);
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            Map<String, Object> claims = claimsJws.getBody();
            logger.info("トークン検証成功");
            logger.debug("検証されたクレーム: {}", claims);
            return claims;
        } catch (ExpiredJwtException e) {
            logger.warn("トークンの有効期限が切れています", e);
            throw new JwtException("トークンの有効期限が切れています", e);
        } catch (SignatureException e) {
            logger.warn("トークンの署名が無効です", e);
            throw new JwtException("トークンの署名が無効です", e);
        } catch (MalformedJwtException e) {
            logger.warn("トークンの形式が不正です", e);
            throw new JwtException("トークンの形式が不正です", e);
        } catch (UnsupportedJwtException e) {
            logger.warn("サポートされていないトークンです", e);
            throw new JwtException("サポートされていないトークンです", e);
        } catch (IllegalArgumentException e) {
            logger.warn("トークンが空または無効です", e);
            throw new JwtException("トークンが空または無効です", e);
        } catch (Exception e) {
            logger.error("トークン検証中に予期せぬエラーが発生しました", e);
            throw new JwtException("トークンの検証に失敗しました", e);
        }
    }
}
