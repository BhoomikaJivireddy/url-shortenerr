package com.susu.urlshortener.service;

import org.springframework.data.redis.core.RedisTemplate;
import com.susu.urlshortener.dto.AnalyticsResponse;
import com.susu.urlshortener.dto.CreateUrlRequest;
import com.susu.urlshortener.dto.UpdateStatusRequest;
import com.susu.urlshortener.dto.UpdateUrlRequest;
import com.susu.urlshortener.entity.ClickEvent;
import com.susu.urlshortener.entity.Url;
import com.susu.urlshortener.entity.User;
import com.susu.urlshortener.exception.UnauthorizedException;
import com.susu.urlshortener.exception.ConflictException;
import com.susu.urlshortener.exception.ResourceNotFoundException;
import com.susu.urlshortener.repository.ClickEventRepository;
import com.susu.urlshortener.repository.UrlRepository;
import com.susu.urlshortener.repository.UserRepository;
import com.susu.urlshortener.util.UserAgentParser;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class UrlService {

    private final UrlRepository urlRepository;
    private final ClickEventRepository clickEventRepository;
    private final UserRepository userRepository;
    private final RedisTemplate<String, String> redisTemplate;

    public UrlService(
            UrlRepository urlRepository,
            ClickEventRepository clickEventRepository,
            UserRepository userRepository,
            RedisTemplate<String, String> redisTemplate
    ) {
        this.urlRepository = urlRepository;
        this.clickEventRepository = clickEventRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
    }


    // =========================================================
    // CREATE URL
    // =========================================================

    public Url createUrl(CreateUrlRequest request) {

        String originalUrl = request.getOriginalUrl();

        if (originalUrl == null || originalUrl.isBlank()) {
            throw new IllegalArgumentException(
                    "Original URL is required"
            );
        }

        if (!isValidUrl(originalUrl)) {
            throw new IllegalArgumentException(
                    "Invalid URL. URL must start with http:// or https://"
            );
        }

        String shortCode;

        // Custom short code
        if (request.getCustomCode() != null
                && !request.getCustomCode().isBlank()) {

            shortCode = request.getCustomCode().trim();

            validateCustomCode(shortCode);

            if (urlRepository.existsByShortCode(shortCode)) {
                throw new ConflictException(
                        "Custom short code already exists"
                );
            }

        } else {

            // Automatically generate short code
            shortCode = generateUniqueShortCode();
        }

        // Get currently logged-in user
        User currentUser = getCurrentUser();

        // Create URL
        Url url = new Url(
                originalUrl,
                shortCode
        );

        // Assign ownership
        url.setUser(currentUser);

        return urlRepository.save(url);
    }


    // =========================================================
    // GET ALL URLS FOR CURRENT USER
    // =========================================================

    public List<Url> getAllUrls() {

        User currentUser = getCurrentUser();

        return urlRepository.findByUserId(
                currentUser.getId()
        );
    }


    // =========================================================
    // GET ONE URL
    // =========================================================

    public Url getUrlById(Long id) {

        return getOwnedUrl(id);
    }


    // =========================================================
    // UPDATE URL
    // =========================================================

    public Url updateUrl(
            Long id,
            UpdateUrlRequest request
    ) {

        Url url = getOwnedUrl(id);

        // Update original URL
        if (request.getOriginalUrl() != null
                && !request.getOriginalUrl().isBlank()) {

            if (!isValidUrl(request.getOriginalUrl())) {
                throw new IllegalArgumentException(
                        "Invalid URL. URL must start with http:// or https://"
                );
            }

            url.setOriginalUrl(
                    request.getOriginalUrl()
            );
        }

        // Update expiration
        if (request.getExpiresAt() != null) {

            url.setExpiresAt(
                    request.getExpiresAt()
            );
        }
        evictCache(url.getShortCode());

        return urlRepository.save(url);
    }


    // =========================================================
    // ACTIVATE / DEACTIVATE URL
    // =========================================================

    public Url updateStatus(
            Long id,
            UpdateStatusRequest request
    ) {

        Url url = getOwnedUrl(id);

        url.setActive(
                request.isActive()
        );
        evictCache(url.getShortCode());

        return urlRepository.save(url);
    }


    // =========================================================
    // DELETE URL
    // =========================================================

    @Transactional
    public void deleteUrl(Long id) {

        Url url = getOwnedUrl(id);

        // Delete click history first
        clickEventRepository.deleteByUrlId(id);
        evictCache(url.getShortCode());

        // Delete URL
        urlRepository.delete(url);
    }


    // =========================================================
    // REDIRECT + CLICK TRACKING
    // =========================================================

    @Transactional
    public String getOriginalUrl(
            String shortCode,
            HttpServletRequest request
    ) {

        String cacheKey = "url:" + shortCode;

        // 1. Find URL entity
        Url url = urlRepository.findByShortCode(shortCode)
                .orElseThrow(() ->
                        new ResourceNotFoundException("URL not found")
                );

        // 2. Check whether URL is active
        if (!url.isActive()) {
            throw new IllegalStateException("URL is inactive");
        }

        // 3. Check expiration
        if (url.getExpiresAt() != null &&
                url.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new IllegalStateException("URL has expired");
        }

        // 4. Check Redis
        String cachedUrl =
                redisTemplate.opsForValue().get(cacheKey);

        // 5. Redis MISS → store URL in Redis
        if (cachedUrl == null) {

            redisTemplate.opsForValue().set(
                    cacheKey,
                    url.getOriginalUrl()
            );

            cachedUrl = url.getOriginalUrl();
        }

        // 6. Record click
        ClickEvent clickEvent = new ClickEvent(
                url,
                request.getRemoteAddr(),
                request.getHeader("User-Agent")
        );

        clickEventRepository.save(clickEvent);

        // 7. Increment click count
        url.setClickCount(
                url.getClickCount() + 1
        );

        urlRepository.save(url);

        // 8. Return original URL
        return cachedUrl;
    }



    // =========================================================
    // ANALYTICS
    // =========================================================

    public AnalyticsResponse getAnalytics(Long urlId) {

        // IMPORTANT:
        // Only the owner can access analytics
        Url url = getOwnedUrl(urlId);

        LocalDateTime now =
                LocalDateTime.now();

        // Total clicks
        long totalClicks =
                clickEventRepository.countByUrlId(
                        urlId
                );

        // Today's clicks
        LocalDateTime startOfDay =
                now.toLocalDate()
                        .atStartOfDay();

        long clicksToday =
                clickEventRepository
                        .countByUrlIdAndClickedAtAfter(
                                urlId,
                                startOfDay
                        );

        // Last 7 days
        LocalDateTime sevenDaysAgo =
                now.minusDays(7);

        long clicksThisWeek =
                clickEventRepository
                        .countByUrlIdAndClickedAtAfter(
                                urlId,
                                sevenDaysAgo
                        );

        // Last 30 days
        LocalDateTime thirtyDaysAgo =
                now.minusDays(30);

        long clicksThisMonth =
                clickEventRepository
                        .countByUrlIdAndClickedAtAfter(
                                urlId,
                                thirtyDaysAgo
                        );

        // Get click events
        List<ClickEvent> clickEvents =
                clickEventRepository
                        .findByUrlId(urlId);

        Map<String, Long> browsers =
                new HashMap<>();

        Map<String, Long> operatingSystems =
                new HashMap<>();

        Map<String, Long> devices =
                new HashMap<>();

        // Analyze visitor information
        for (ClickEvent clickEvent : clickEvents) {

            String userAgent =
                    clickEvent.getUserAgent();

            String browser =
                    UserAgentParser
                            .getBrowser(userAgent);

            String operatingSystem =
                    UserAgentParser
                            .getOperatingSystem(userAgent);

            String device =
                    UserAgentParser
                            .getDevice(userAgent);

            browsers.put(
                    browser,
                    browsers.getOrDefault(
                            browser,
                            0L
                    ) + 1
            );

            operatingSystems.put(
                    operatingSystem,
                    operatingSystems.getOrDefault(
                            operatingSystem,
                            0L
                    ) + 1
            );

            devices.put(
                    device,
                    devices.getOrDefault(
                            device,
                            0L
                    ) + 1
            );
        }

        return new AnalyticsResponse(
                url.getId(),
                url.getShortCode(),
                url.getOriginalUrl(),
                totalClicks,
                clicksToday,
                clicksThisWeek,
                clicksThisMonth,
                browsers,
                operatingSystems,
                devices
        );
    }


    // =========================================================
    // GET CURRENT USER
    // =========================================================

    private User getCurrentUser() {

        String email = getCurrentUserEmail();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );
    }


    // =========================================================
    // GET CURRENT USER EMAIL FROM JWT
    // =========================================================

    private String getCurrentUserEmail() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new UnauthorizedException(
                    "User is not authenticated"
            );
        }

        return authentication.getName();
    }


    // =========================================================
    // GET URL ONLY IF CURRENT USER OWNS IT
    // =========================================================

    private Url getOwnedUrl(Long id) {

        User currentUser = getCurrentUser();

        Url url = urlRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "URL not found"
                        )
                );

        if (url.getUser() == null
                || !url.getUser()
                .getId()
                .equals(currentUser.getId())) {

            throw new ResourceNotFoundException(
                    "URL not found"
            );
        }

        return url;
    }


    // =========================================================
    // URL VALIDATION
    // =========================================================

    private boolean isValidUrl(String url) {

        return url.startsWith("http://")
                || url.startsWith("https://");
    }


    // =========================================================
    // CUSTOM CODE VALIDATION
    // =========================================================

    private void validateCustomCode(
            String customCode
    ) {

        if (customCode.length() < 3
                || customCode.length() > 20) {

            throw new IllegalArgumentException(
                    "Custom code must be between 3 and 20 characters"
            );
        }

        if (!customCode.matches(
                "[a-zA-Z0-9_-]+"
        )) {

            throw new IllegalArgumentException(
                    "Custom code can contain only letters, numbers, '-' and '_'"
            );
        }
    }


    // =========================================================
    // GENERATE UNIQUE SHORT CODE
    // =========================================================

    private String generateUniqueShortCode() {

        String shortCode;

        do {

            shortCode =
                    UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 6);

        } while (
                urlRepository
                        .existsByShortCode(shortCode)
        );

        return shortCode;
    }
    private void evictCache(String shortCode) {
        redisTemplate.delete("url:" + shortCode);
    }
}