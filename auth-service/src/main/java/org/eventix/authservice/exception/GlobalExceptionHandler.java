package org.eventix.authservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.eventix.authservice.exception.base.AccessTokenException;
import org.eventix.authservice.exception.base.NotFoundException;
import org.eventix.authservice.exception.base.RefreshTokenException;
import org.eventix.authservice.model.dto.response.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            Exception ex,
            HttpStatus status,
            String error,
            HttpServletRequest request
    ) {

        log.error("Exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);

        ErrorResponse response = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                error,
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity.status(status).body(response);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ex,
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error",
                request
        );
    }

    @ExceptionHandler(AccessTokenAuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
            AccessTokenAuthenticationException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ex,
                HttpStatus.UNAUTHORIZED,
                "Unauthorized",
                request
        );
    }
    @ExceptionHandler(AccessTokenException.class)
    public ResponseEntity<ErrorResponse> handleAccessTokenException(
            AccessTokenException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ex,
                HttpStatus.UNAUTHORIZED,
                "Access token error",
                request
        );
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ErrorResponse> handleRefreshTokenException(
            RefreshTokenException ex,
            HttpServletRequest request
    ) {
        return buildErrorResponse(
                ex,
                HttpStatus.UNAUTHORIZED,
                "Refresh token error",
                request
        );
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(
            NotFoundException ex,
            HttpServletRequest request) {
        return buildErrorResponse(ex,
                HttpStatus.NOT_FOUND,
                ex.getMessage(),
                request);
    }
}

