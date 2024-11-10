package com.github.oosm032519.playlistviewernext.util;

import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class HashUtil {

    public String hashUserId(String userId) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(userId.getBytes());
        return Base64.getEncoder().encodeToString(hashedBytes);
    }
}
