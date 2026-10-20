package com.example.reservation.web;

import com.example.reservation.reservations.ReservationStateException;
import jakarta.persistence.EntityNotFoundException;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.time.LocalDateTime;
import java.util.stream.Collectors;


@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception e) {
        log.error("Handle exception", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(buildErrorDto(HttpStatus.INTERNAL_SERVER_ERROR, "Sorry, something unexpected happened with our server"));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(EntityNotFoundException e) {
        log.warn("Handle EntityNotFoundException: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(buildErrorDto(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(Exception e) {
        log.warn("Handle BadRequest: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(buildErrorDto(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        var errorDto = buildErrorDto(
                status,
                e.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining("; "))
        );
        log.warn("Validation failed: {}", errorDto.errorDetailedMessage());

        return super.handleExceptionInternal(e, errorDto, headers, status, request);
    }

    @ExceptionHandler(ReservationStateException.class)
    public ResponseEntity<ErrorResponseDto> handleReservationStateException(ReservationStateException e) {
        log.warn("Handle ReservationStateException: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(buildErrorDto(HttpStatus.CONFLICT, e.getMessage()));
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(Exception e, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.warn("Handle handleExceptionInternal: {}", e.getMessage());

        var errorDto = buildErrorDto(statusCode, e.getMessage());

        return super.handleExceptionInternal(e, errorDto, headers, statusCode, request);
    }

    private ErrorResponseDto buildErrorDto(HttpStatusCode status, String detail) {
        return new ErrorResponseDto(
                HttpStatus.valueOf(status.value()).getReasonPhrase(),
                detail,
                LocalDateTime.now()
        );
    }
}

