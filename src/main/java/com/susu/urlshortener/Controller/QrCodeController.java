package com.susu.urlshortener.Controller;

import com.google.zxing.WriterException;
import com.susu.urlshortener.entity.Url;
import com.susu.urlshortener.entity.User;
import com.susu.urlshortener.repository.UrlRepository;
import com.susu.urlshortener.repository.UserRepository;
import com.susu.urlshortener.service.QrCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/qr")
@RequiredArgsConstructor
public class QrCodeController {

    private final QrCodeService qrCodeService;
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;

    @GetMapping(value = "/{shortCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(
            @PathVariable String shortCode
    ) throws WriterException, IOException {

        // Get currently logged-in user's email
        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        // Find current user
        User currentUser =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        // Find URL only if it belongs to current user
        Url url =
                urlRepository.findByShortCodeAndUserId(
                                shortCode,
                                currentUser.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "URL not found"
                                )
                        );

        String shortUrl =
                "http://localhost:8080/" + url.getShortCode();

        byte[] qrCode =
                qrCodeService.generateQrCode(
                        shortUrl,
                        300,
                        300
                );

        return ResponseEntity.ok(qrCode);
    }
}