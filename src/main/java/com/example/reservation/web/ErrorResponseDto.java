package com.example.reservation.web;

import java.time.LocalDateTime;

public record ErrorResponseDto (
        String message,
        String errorDetailedMessage,
        LocalDateTime errorTime
){

}
