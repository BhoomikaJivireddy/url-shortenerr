package com.susu.urlshortener.service;

import com.susu.urlshortener.dto.AuthResponse;
import com.susu.urlshortener.dto.LoginRequest;
import com.susu.urlshortener.dto.RegisterRequest;
import com.susu.urlshortener.entity.User;
import com.susu.urlshortener.repository.UserRepository;
import com.susu.urlshortener.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;


    // =========================================================
    // REGISTER
    // =========================================================

    public void register(RegisterRequest request) {

        // Check whether email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException(
                    "Email already registered"
            );
        }

        // Encode password before saving
        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getName(),
                request.getEmail(),
                encodedPassword,
                "USER"
        );

        userRepository.save(user);
    }


    // =========================================================
    // LOGIN
    // =========================================================

    public AuthResponse login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invalid email or password"
                        )
                );

        // Verify password
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "Invalid email or password"
            );
        }

        // Create access token
        String token =
                jwtService.generateToken(user.getEmail());

        // Create refresh token
        String refreshToken =
                refreshTokenService
                        .createRefreshToken(user)
                        .getToken();

        return new AuthResponse(
                token,
                refreshToken
        );
    }
}