package com.github.oosm032519.playlistviewernext.util;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * ユーザーIDのハッシュ化を行うユーティリティクラス。
 * Spring Componentとして管理され、ユーザーIDの安全な保存や比較に使用される。
 */
@Component
public class HashUtil {

    /**
     * ユーザーIDをSHA-256アルゴリズムでハッシュ化し、Base64エンコードした文字列を返す。
     *
     * @param userId ハッシュ化対象のユーザーID
     * @return ハッシュ化およびBase64エンコードされた文字列
     * @throws NoSuchAlgorithmException SHA-256アルゴリズムが利用できない場合に発生
     */
    public String hashUserId(String userId) throws NoSuchAlgorithmException {
        // SHA-256ハッシュアルゴリズムのインスタンスを取得
        MessageDigest md = MessageDigest.getInstance("SHA-256");

        // ユーザーIDをバイト配列に変換してハッシュ化
        byte[] hashedBytes = md.digest(userId.getBytes());

        // ハッシュ化されたバイト配列をBase64エンコードして返却
        return Base64.getEncoder().encodeToString(hashedBytes);
    }
}
