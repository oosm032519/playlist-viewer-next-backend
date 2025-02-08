package com.github.oosm032519.playlistviewernext.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

class ErrorResponseTest {

    /**
     * 3つのパラメータを持つコンストラクタでErrorResponseオブジェクトを生成し、
     * 各フィールドに正しい値が設定されていることを確認する。
     */
    @Test
    void testConstructorWithThreeParameters() {
        // Arrange: テストデータの準備
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorCode = "ERR001";
        String message = "Invalid input";

        // Act: ErrorResponseオブジェクトの生成
        ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message);

        // Assert: 各フィールドの値が正しいことを確認
        assertThat(errorResponse.getStatus()).isEqualTo(status);
        assertThat(errorResponse.getErrorCode()).isEqualTo(errorCode);
        assertThat(errorResponse.getMessage()).isEqualTo(message);
        assertThat(errorResponse.getDetails()).isNull();
    }

    /**
     * 4つのパラメータを持つコンストラクタでErrorResponseオブジェクトを生成し、
     * 各フィールドに正しい値が設定されていることを確認する。
     */
    @Test
    void testConstructorWithFourParameters() {
        // Arrange: テストデータの準備
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = "ERR002";
        String message = "Server error";
        String details = "Detailed error information";

        // Act: ErrorResponseオブジェクトの生成
        ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);

        // Assert: 各フィールドの値が正しいことを確認
        assertThat(errorResponse.getStatus()).isEqualTo(status);
        assertThat(errorResponse.getErrorCode()).isEqualTo(errorCode);
        assertThat(errorResponse.getMessage()).isEqualTo(message);
        assertThat(errorResponse.getDetails()).isEqualTo(details);
    }
}
