package com.susu.urlshortener.Controller;

import com.susu.urlshortener.dto.AnalyticsResponse;
import com.susu.urlshortener.service.UrlService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/urls")
public class AnalyticsController {

    private final UrlService urlService;

    public AnalyticsController(UrlService urlService) {
        this.urlService = urlService;
    }

    @GetMapping("/{urlId}/analytics")
    public AnalyticsResponse getAnalytics(@PathVariable Long urlId) {

        return urlService.getAnalytics(urlId);
    }
}