package com.example.reservation.users;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyTakenException("Username " + request.username() + " is already taken");
        }

        var user = new UserEntity(
                null,
                request.username(),
                passwordEncoder.encode(request.password()),
                Role.USER
        );

        var saved = userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getUsername(), saved.getRole());
    }
}