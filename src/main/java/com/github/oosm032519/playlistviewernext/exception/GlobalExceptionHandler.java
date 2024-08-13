package com.github.oosm032519.playlistviewernext.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(PlaylistViewerNextException.class)
    public ResponseEntity<ErrorResponse> handlePlaylistViewerNextException(PlaylistViewerNextException ex) {
        logger.error("PlaylistViewerNextException が発生しました: {}", ex.getMessage(), ex);

        ErrorResponse errorResponse = new ErrorResponse(ex.getHttpStatus(), ex.getErrorCode(), ex.getMessage(), ex.getDetails());
        return new ResponseEntity<>(errorResponse, ex.getHttpStatus());
    }
}
