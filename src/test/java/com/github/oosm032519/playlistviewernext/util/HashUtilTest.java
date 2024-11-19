package com.github.oosm032519.playlistviewernext.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HashUtilTest {

    private HashUtil hashUtil;

    @BeforeEach
    void setUp() {
        hashUtil = new HashUtil();
    }

    @Test
    @DisplayName("正常系: 同じユーザーIDに対して常に同じハッシュ値が生成されることを確認")
    void hashUserId_ShouldReturnConsistentHash() throws NoSuchAlgorithmException {
        // Given
        String userId = "testUser123";

        // When
        String hash1 = hashUtil.hashUserId(userId);
        String hash2 = hashUtil.hashUserId(userId);

        // Then
        assertThat(hash1)
                .isNotEmpty()
                .isEqualTo(hash2);
    }

    @Test
    @DisplayName("正常系: 異なるユーザーIDに対して異なるハッシュ値が生成されることを確認")
    void hashUserId_ShouldReturnDifferentHashForDifferentInputs() throws NoSuchAlgorithmException {
        // Given
        String userId1 = "testUser1";
        String userId2 = "testUser2";

        // When
        String hash1 = hashUtil.hashUserId(userId1);
        String hash2 = hashUtil.hashUserId(userId2);

        // Then
        assertThat(hash1)
                .isNotEmpty()
                .isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("異常系: nullが入力された場合、NullPointerExceptionが発生することを確認")
    void hashUserId_ShouldThrowException_WhenInputIsNull() {
        assertThatThrownBy(() -> hashUtil.hashUserId(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("正常系: 空文字列が入力された場合、有効なハッシュ値が返されることを確認")
    void hashUserId_ShouldReturnValidHash_WhenInputIsEmpty() throws NoSuchAlgorithmException {
        // Given
        String emptyUserId = "";

        // When
        String hashedValue = hashUtil.hashUserId(emptyUserId);

        // Then
        assertThat(hashedValue)
                .isNotEmpty()
                .matches("^[A-Za-z0-9+/]*={0,2}$"); // Base64エンコードパターンに一致することを確認
    }

    @Test
    @DisplayName("正常系: ハッシュ値がBase64エンコードされた文字列であることを確認")
    void hashUserId_ShouldReturnBase64EncodedString() throws NoSuchAlgorithmException {
        // Given
        String userId = "testUser123";

        // When
        String hashedValue = hashUtil.hashUserId(userId);

        // Then
        assertThat(hashedValue)
                .matches("^[A-Za-z0-9+/]*={0,2}$"); // Base64エンコードパターンに一致することを確認
    }
}
