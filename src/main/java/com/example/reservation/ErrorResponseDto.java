package com.example.reservation;

import java.time.LocalDateTime;

public record ErrorResponseDto (
        String message,
        String errorDetailedMessage,
        LocalDateTime errorTime
){

}
