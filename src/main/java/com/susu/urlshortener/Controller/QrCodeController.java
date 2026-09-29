package com.susu.urlshortener.controller;

import com.susu.urlshortener.service.QrCodeService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/qr")
public class QrCodeController {

    private final QrCodeService qrCodeService;

    public QrCodeController(QrCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @GetMapping(value = "/{shortCode}", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> generateQrCode(
            @PathVariable String shortCode) throws Exception {

        String shortUrl =
                "http://localhost:8080/" + shortCode;

        byte[] qrCode =
                qrCodeService.generateQrCode(shortUrl);

        return ResponseEntity.ok(qrCode);
    }
}