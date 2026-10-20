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

        var errorDto = new ErrorResponseDto("Internal Server Error", "Sorry, something unexpected happened with our server", LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorDto);
    }


    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleEntityNotFound(EntityNotFoundException e) {
        log.error("Handle EntityNotFoundException", e);

        var errorDto = new ErrorResponseDto("Entity not found", e.getMessage(), LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorDto);
    }


    @ExceptionHandler(exception = {IllegalArgumentException.class})
    public ResponseEntity<ErrorResponseDto> handleBadRequest(Exception e) {
        log.error("Handle BadRequest", e);

        var errorDto = new ErrorResponseDto("Bad request", e.getMessage(), LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDto);
    }


    @Override
    protected @Nullable ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException e, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        var errorDto = new ErrorResponseDto(
                HttpStatus.valueOf(status.value()).getReasonPhrase(),
                e.getBindingResult().getFieldErrors().stream()
                        .map(error -> error.getField() + ": " + error.getDefaultMessage())
                        .collect(Collectors.joining("; ")),
                LocalDateTime.now()
        );

        return super.handleExceptionInternal(e, errorDto, headers, status, request);
    }

    @ExceptionHandler(ReservationStateException.class)
    public ResponseEntity<ErrorResponseDto> handleReservationStateException(ReservationStateException e) {
        log.error("Handle ReservationStateException", e);

        var errorDto = new ErrorResponseDto("Conflict", e.getMessage(), LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorDto);
    }

    @Override
    protected @Nullable ResponseEntity<Object> handleExceptionInternal(Exception e, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        var errorDto = new ErrorResponseDto(HttpStatus.valueOf(statusCode.value()).getReasonPhrase(), e.getMessage(), LocalDateTime.now());

        return super.handleExceptionInternal(e, errorDto, headers, statusCode, request);
    }
}

