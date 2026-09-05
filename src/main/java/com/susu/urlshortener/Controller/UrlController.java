package com.susu.urlshortener.Controller;

import com.susu.urlshortener.dto.CreateUrlRequest;
import com.susu.urlshortener.dto.UpdateStatusRequest;
import com.susu.urlshortener.dto.UpdateUrlRequest;
import com.susu.urlshortener.entity.Url;
import com.susu.urlshortener.service.UrlService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.susu.urlshortener.service.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/urls")
public class UrlController {

    private final UrlService urlService;
    private final RateLimitService rateLimitService;

    public UrlController(
            UrlService urlService,
            RateLimitService rateLimitService
    ) {
        this.urlService = urlService;
        this.rateLimitService = rateLimitService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<?> createUrl(
            @Valid @RequestBody CreateUrlRequest request,
            HttpServletRequest httpRequest
    ) {

        String ipAddress =
                httpRequest.getRemoteAddr();

        if (!rateLimitService.isAllowed(
                "create-url",
                ipAddress
        )) {

            return ResponseEntity
                    .status(429)
                    .body(
                            "Too many URL creation requests. Please try again later."
                    );
        }

        return ResponseEntity.ok(
                urlService.createUrl(request)
        );
    }


    // GET ALL
    @GetMapping
    public List<Url> getAllUrls() {

        return urlService.getAllUrls();
    }


    // GET ONE
    @GetMapping("/{id}")
    public Url getUrlById(
            @PathVariable Long id
    ) {

        return urlService.getUrlById(id);
    }


    // UPDATE
    @PutMapping("/{id}")
    public Url updateUrl(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUrlRequest request
    ) {

        return urlService.updateUrl(id, request);
    }


    // ACTIVATE / DEACTIVATE
    @PatchMapping("/{id}/status")
    public Url updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest request
    ) {

        return urlService.updateStatus(id, request);
    }


    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUrl(
            @PathVariable Long id
    ) {

        urlService.deleteUrl(id);

        return ResponseEntity.noContent().build();
    }
}