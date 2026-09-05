package com.susu.urlshortener.service;

import com.susu.urlshortener.entity.RefreshToken;
import com.susu.urlshortener.entity.User;
import com.susu.urlshortener.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // =========================================================
    // CREATE REFRESH TOKEN
    // =========================================================

    public RefreshToken createRefreshToken(User user) {

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setToken(
                UUID.randomUUID().toString()
        );

        refreshToken.setUser(user);

        refreshToken.setExpiresAt(
                LocalDateTime.now().plusDays(7)
        );

        return refreshTokenRepository.save(refreshToken);
    }


    // =========================================================
    // VERIFY REFRESH TOKEN
    // =========================================================

    public RefreshToken verifyExpiration(
            RefreshToken refreshToken
    ) {

        if (refreshToken.getExpiresAt()
                .isBefore(LocalDateTime.now())) {

            refreshTokenRepository.delete(refreshToken);

            throw new IllegalStateException(
                    "Refresh token expired"
            );
        }

        return refreshToken;
    }
}