package com.example.reservation.users;

public record UserResponse(
        Long id,
        String username,
        Role role
) {
}
