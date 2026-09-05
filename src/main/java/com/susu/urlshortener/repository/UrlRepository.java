package com.susu.urlshortener.repository;

import com.susu.urlshortener.entity.Url;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UrlRepository
        extends JpaRepository<Url, Long> {

    Optional<Url> findByShortCode(
            String shortCode
    );

    boolean existsByShortCode(
            String shortCode
    );

    List<Url> findByUserId(
            Long userId
    );
    Optional<Url> findByShortCodeAndUserId(
            String shortCode,
            Long userId
    );
}