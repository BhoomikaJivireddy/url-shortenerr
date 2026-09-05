package com.susu.urlshortener.Controller;

import com.susu.urlshortener.dto.AuthResponse;
import com.susu.urlshortener.dto.LoginRequest;
import com.susu.urlshortener.dto.RegisterRequest;
import com.susu.urlshortener.entity.RefreshToken;
import com.susu.urlshortener.repository.RefreshTokenRepository;
import com.susu.urlshortener.security.JwtService;
import com.susu.urlshortener.service.AuthService;
import com.susu.urlshortener.service.RateLimitService;
import com.susu.urlshortener.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RefreshTokenService refreshTokenService;
    private final JwtService jwtService;
    private final RateLimitService rateLimitService;


    // =========================================================
    // REGISTER
    // =========================================================

    @PostMapping("/register")
    public ResponseEntity<Void> register(
            @Valid @RequestBody RegisterRequest request
    ) {

        authService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }


    // =========================================================
    // LOGIN
    // =========================================================

    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
    ) {

        String ipAddress =
                httpRequest.getRemoteAddr();

        if (!rateLimitService.isAllowed(
                "login",
                ipAddress
        )) {

            return ResponseEntity
                    .status(429)
                    .body("Too many login attempts. Please try again later.");
        }

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    @PostMapping("/refresh")
    public AuthResponse refreshToken(
            @RequestParam String refreshToken
    ) {

        RefreshToken token =
                refreshTokenRepository
                        .findByToken(refreshToken)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Invalid refresh token"
                                )
                        );

        refreshTokenService.verifyExpiration(token);

        String newAccessToken =
                jwtService.generateToken(
                        token.getUser().getEmail()
                );

        return new AuthResponse(
                newAccessToken,
                refreshToken
        );
    }
}