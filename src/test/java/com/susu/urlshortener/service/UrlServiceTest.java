package com.susu.urlshortener.service;

import com.susu.urlshortener.dto.CreateUrlRequest;
import com.susu.urlshortener.entity.Url;
import com.susu.urlshortener.entity.User;
import com.susu.urlshortener.exception.ConflictException;
import com.susu.urlshortener.exception.ResourceNotFoundException;
import com.susu.urlshortener.exception.UnauthorizedException;
import com.susu.urlshortener.repository.ClickEventRepository;
import com.susu.urlshortener.repository.UrlRepository;
import com.susu.urlshortener.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UrlServiceTest {

    @Mock
    private UrlRepository urlRepository;

    @Mock
    private ClickEventRepository clickEventRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;


    // ============================================================
    // TEST 1
    // Create URL successfully
    // ============================================================

    @Test
    void createUrl_shouldCreateUrlSuccessfully() {

        // Mock authentication
        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("su@gmail.com");

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        // Create test user
        User user = new User(
                "Susu",
                "su@gmail.com",
                "password",
                "USER"
        );

        when(userRepository.findByEmail("su@gmail.com"))
                .thenReturn(Optional.of(user));

        // Mock short code check
        when(urlRepository.existsByShortCode(anyString()))
                .thenReturn(false);

        // Mock saved URL
        Url savedUrl = new Url(
                "https://google.com",
                "abc123"
        );

        savedUrl.setUser(user);

        when(urlRepository.save(any(Url.class)))
                .thenReturn(savedUrl);

        // Create request
        CreateUrlRequest request =
                new CreateUrlRequest();

        request.setOriginalUrl(
                "https://google.com"
        );

        // Mock SecurityContextHolder
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
                     mockStatic(SecurityContextHolder.class)) {

            mockedSecurityContextHolder
                    .when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            // Create service
            UrlService urlService =
                    new UrlService(
                            urlRepository,
                            clickEventRepository,
                            userRepository,
                            redisTemplate
                    );

            // Execute
            Url result =
                    urlService.createUrl(request);

            // Verify result
            assertNotNull(result);

            assertEquals(
                    "https://google.com",
                    result.getOriginalUrl()
            );

            assertEquals(
                    user,
                    result.getUser()
            );

            // Verify repository calls
            verify(userRepository)
                    .findByEmail("su@gmail.com");

            verify(urlRepository)
                    .save(any(Url.class));
        }
    }


    // ============================================================
    // TEST 2
    // Reject invalid URL
    // ============================================================

    @Test
    void createUrl_shouldRejectInvalidUrl() {

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("invalid-url");

        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> urlService.createUrl(request)
        );

        verifyNoInteractions(userRepository);
        verify(urlRepository, never())
                .save(any(Url.class));
    }


    // ============================================================
    // TEST 3
    // Create URL with custom code
    // ============================================================

    @Test
    void createUrl_shouldCreateUrlWithCustomCode() {

        // Mock authentication
        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("su@gmail.com");

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        // Create test user
        User user = new User(
                "Susu",
                "su@gmail.com",
                "password",
                "USER"
        );

        when(userRepository.findByEmail("su@gmail.com"))
                .thenReturn(Optional.of(user));

        // Custom code does not already exist
        when(urlRepository.existsByShortCode("google"))
                .thenReturn(false);

        // Mock saved URL
        Url savedUrl = new Url(
                "https://google.com",
                "google"
        );

        savedUrl.setUser(user);

        when(urlRepository.save(any(Url.class)))
                .thenReturn(savedUrl);

        // Create request
        CreateUrlRequest request =
                new CreateUrlRequest();

        request.setOriginalUrl(
                "https://google.com"
        );

        request.setCustomCode(
                "google"
        );

        // Mock SecurityContextHolder
        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder =
                     mockStatic(SecurityContextHolder.class)) {

            mockedSecurityContextHolder
                    .when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            // Create service
            UrlService urlService =
                    new UrlService(
                            urlRepository,
                            clickEventRepository,
                            userRepository,
                            redisTemplate
                    );

            // Execute
            Url result =
                    urlService.createUrl(request);

            // Verify
            assertNotNull(result);

            assertEquals(
                    "https://google.com",
                    result.getOriginalUrl()
            );

            assertEquals(
                    "google",
                    result.getShortCode()
            );

            assertEquals(
                    user,
                    result.getUser()
            );

            // Verify repositories
            verify(urlRepository)
                    .existsByShortCode("google");

            verify(urlRepository)
                    .save(any(Url.class));
        }
    }


    // ============================================================
    // TEST 4
    // Reject duplicate custom code
    // ============================================================

    @Test
    void createUrl_shouldRejectDuplicateCustomCode() {

        when(urlRepository.existsByShortCode("mycode"))
                .thenReturn(true);

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://github.com");
        request.setCustomCode("mycode");

        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        assertThrows(
                ConflictException.class,
                () -> urlService.createUrl(request)
        );

        verify(urlRepository, never())
                .save(any(Url.class));
    }


    // ============================================================
    // TEST 5
    // Reject custom code that is too short
    // ============================================================

    @Test
    void createUrl_shouldRejectCustomCodeThatIsTooShort() {

        // Create request
        CreateUrlRequest request =
                new CreateUrlRequest();

        request.setOriginalUrl(
                "https://google.com"
        );

        request.setCustomCode(
                "ab"
        );

        // Create service
        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        // Execute and verify exception
        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> urlService.createUrl(request)
                );

        // Verify message
        assertEquals(
                "Custom code must be between 3 and 20 characters",
                exception.getMessage()
        );

        // Database should not be accessed
        verifyNoInteractions(urlRepository);
        verifyNoInteractions(userRepository);
    }


    // ============================================================
    // TEST 6
    // Reject custom code with invalid characters
    // ============================================================

    @Test
    void createUrl_shouldRejectCustomCodeWithInvalidCharacters() {

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://github.com");
        request.setCustomCode("my code!");

        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> urlService.createUrl(request)
        );

        verifyNoInteractions(userRepository);
        verify(urlRepository, never())
                .save(any(Url.class));
    }


    // ============================================================
    // TEST 7
    // Reject custom code that is too long
    // ============================================================

    @Test
    void createUrl_shouldRejectCustomCodeThatIsTooLong() {

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://github.com");

        request.setCustomCode(
                "abcdefghijklmnopqrstuvwxyz1234567890"
        );

        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> urlService.createUrl(request)
        );

        verifyNoInteractions(userRepository);
        verify(urlRepository, never())
                .save(any(Url.class));
    }


    // ============================================================
    // TEST 8
    // Reject blank original URL
    // ============================================================

    @Test
    void createUrl_shouldRejectBlankOriginalUrl() {

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("");

        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> urlService.createUrl(request)
        );

        verifyNoInteractions(userRepository);
        verify(urlRepository, never())
                .save(any(Url.class));
    }


    // ============================================================
    // TEST 9
    // Reject when user not found
    // ============================================================

    @Test
    void createUrl_shouldRejectWhenUserNotFound() {

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("unknown@gmail.com");

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        when(userRepository.findByEmail("unknown@gmail.com"))
                .thenReturn(Optional.empty());

        when(urlRepository.existsByShortCode(anyString()))
                .thenReturn(false);

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://google.com");

        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        try (MockedStatic<SecurityContextHolder> mocked =
                     mockStatic(SecurityContextHolder.class)) {

            mocked.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            assertThrows(
                    ResourceNotFoundException.class,
                    () -> urlService.createUrl(request)
            );

            verify(userRepository)
                    .findByEmail("unknown@gmail.com");

            verify(urlRepository, never())
                    .save(any(Url.class));
        }
    }


    // ============================================================
    // TEST 10
    // Reject unauthenticated user
    // ============================================================

    @Test
    void createUrl_shouldRejectUnauthenticatedUser() {

        when(authentication.isAuthenticated())
                .thenReturn(false);

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        when(urlRepository.existsByShortCode(anyString()))
                .thenReturn(false);

        CreateUrlRequest request = new CreateUrlRequest();
        request.setOriginalUrl("https://google.com");

        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        try (MockedStatic<SecurityContextHolder> mocked =
                     mockStatic(SecurityContextHolder.class)) {

            mocked.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            UnauthorizedException exception =
                    assertThrows(
                            UnauthorizedException.class,
                            () -> urlService.createUrl(request)
                    );

            assertEquals(
                    "User is not authenticated",
                    exception.getMessage()
            );

            verifyNoInteractions(userRepository);

            verify(urlRepository, never())
                    .save(any(Url.class));
        }
    }


    // ============================================================
    // TEST 11
    // Get all URLs of current user
    // ============================================================

    @Test
    void getAllUrls_shouldReturnCurrentUsersUrls() {

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("su@gmail.com");

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        User user =
                spy(new User(
                        "Susu",
                        "su@gmail.com",
                        "password",
                        "USER"
                ));

        doReturn(1L).when(user).getId();

        when(userRepository.findByEmail("su@gmail.com"))
                .thenReturn(Optional.of(user));

        Url url1 =
                new Url(
                        "https://google.com",
                        "abc123"
                );

        Url url2 =
                new Url(
                        "https://github.com",
                        "git123"
                );

        when(urlRepository.findByUserId(1L))
                .thenReturn(List.of(url1, url2));

        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        try (MockedStatic<SecurityContextHolder> mocked =
                     mockStatic(SecurityContextHolder.class)) {

            mocked.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            List<Url> result =
                    urlService.getAllUrls();

            assertNotNull(result);
            assertEquals(2, result.size());

            assertEquals(
                    "abc123",
                    result.get(0).getShortCode()
            );

            assertEquals(
                    "git123",
                    result.get(1).getShortCode()
            );

            verify(userRepository)
                    .findByEmail("su@gmail.com");

            verify(urlRepository)
                    .findByUserId(1L);
        }
    }


    // ============================================================
    // TEST 12
    // Get URL by ID when owned by current user
    // ============================================================

    @Test
    void getUrlById_shouldReturnOwnedUrl() {

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("su@gmail.com");

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        User user =
                spy(new User(
                        "Susu",
                        "su@gmail.com",
                        "password",
                        "USER"
                ));

        doReturn(1L).when(user).getId();

        when(userRepository.findByEmail("su@gmail.com"))
                .thenReturn(Optional.of(user));

        Url url =
                spy(new Url(
                        "https://github.com",
                        "git123"
                ));

        doReturn(10L).when(url).getId();

        url.setUser(user);

        when(urlRepository.findById(10L))
                .thenReturn(Optional.of(url));

        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        try (MockedStatic<SecurityContextHolder> mocked =
                     mockStatic(SecurityContextHolder.class)) {

            mocked.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            Url result =
                    urlService.getUrlById(10L);

            assertNotNull(result);

            assertEquals(
                    10L,
                    result.getId()
            );

            assertEquals(
                    "git123",
                    result.getShortCode()
            );

            assertEquals(
                    "https://github.com",
                    result.getOriginalUrl()
            );

            verify(userRepository)
                    .findByEmail("su@gmail.com");

            verify(urlRepository)
                    .findById(10L);
        }
    }


    // ============================================================
    // TEST 13
    // Reject URL owned by another user
    // ============================================================

    @Test
    void getUrlById_shouldRejectUrlOwnedByAnotherUser() {

        when(authentication.isAuthenticated())
                .thenReturn(true);

        when(authentication.getName())
                .thenReturn("su@gmail.com");

        when(securityContext.getAuthentication())
                .thenReturn(authentication);

        // Current user -> ID 1
        User currentUser =
                spy(new User(
                        "Susu",
                        "su@gmail.com",
                        "password",
                        "USER"
                ));

        doReturn(1L).when(currentUser).getId();

        when(userRepository.findByEmail("su@gmail.com"))
                .thenReturn(Optional.of(currentUser));

        // Another user -> ID 2
        User anotherUser =
                spy(new User(
                        "Another User",
                        "another@gmail.com",
                        "password",
                        "USER"
                ));

        doReturn(2L).when(anotherUser).getId();

        // URL belongs to another user
        Url url =
                new Url(
                        "https://github.com",
                        "git123"
                );

        url.setUser(anotherUser);

        when(urlRepository.findById(10L))
                .thenReturn(Optional.of(url));

        UrlService urlService =
                new UrlService(
                        urlRepository,
                        clickEventRepository,
                        userRepository,
                        redisTemplate
                );

        try (MockedStatic<SecurityContextHolder> mocked =
                     mockStatic(SecurityContextHolder.class)) {

            mocked.when(SecurityContextHolder::getContext)
                    .thenReturn(securityContext);

            ResourceNotFoundException exception =
                    assertThrows(
                            ResourceNotFoundException.class,
                            () -> urlService.getUrlById(10L)
                    );

            assertEquals(
                    "URL not found",
                    exception.getMessage()
            );

            verify(userRepository)
                    .findByEmail("su@gmail.com");

            verify(urlRepository)
                    .findById(10L);

            verify(urlRepository, never())
                    .save(any(Url.class));
        }
    }
}