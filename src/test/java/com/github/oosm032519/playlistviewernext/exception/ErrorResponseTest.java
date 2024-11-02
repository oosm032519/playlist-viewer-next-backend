package com.github.oosm032519.playlistviewernext.exception;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ErrorResponseTest {

    @Test
    void testConstructorWithThreeParameters() {
        // Given
        HttpStatus status = HttpStatus.BAD_REQUEST;
        String errorCode = "ERR001";
        String message = "Invalid input";

        // When
        ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message);

        // Then
        assertThat(errorResponse.getStatus()).isEqualTo(status);
        assertThat(errorResponse.getMessage()).isEqualTo(message);
        assertThat(errorResponse.getDetails()).isNull();
    }

    @Test
    void testConstructorWithFourParameters() {
        // Given
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        String errorCode = "ERR002";
        String message = "Server error";
        String details = "Detailed error information";

        // When
        ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);

        // Then
        assertThat(errorResponse.getStatus()).isEqualTo(status);
        assertThat(errorResponse.getMessage()).isEqualTo(message);
        assertThat(errorResponse.getDetails()).isEqualTo(details);
    }
}
