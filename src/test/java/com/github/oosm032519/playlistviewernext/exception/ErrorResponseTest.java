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
        assertThat(errorResponse.getErrorCode()).isEqualTo(errorCode);
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
        assertThat(errorResponse.getErrorCode()).isEqualTo(errorCode);
        assertThat(errorResponse.getMessage()).isEqualTo(message);
        assertThat(errorResponse.getDetails()).isEqualTo(details);
    }

    @Test
    void testToString() {
        // Given
        HttpStatus status = HttpStatus.NOT_FOUND;
        String errorCode = "ERR003";
        String message = "Resource not found";
        String details = "The requested resource could not be found";
        ErrorResponse errorResponse = new ErrorResponse(status, errorCode, message, details);

        // When
        String result = errorResponse.toString();

        // Then
        assertThat(result).contains(
                "\"status\":\"" + status + "\"",
                "\"errorCode\":\"" + errorCode + "\"",
                "\"message\":\"" + message + "\"",
                "\"details\":\"" + details + "\""
        );
    }
}
