package com.susu.urlshortener.repository;

import com.susu.urlshortener.entity.ClickEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ClickEventRepository extends JpaRepository<ClickEvent, Long> {

    long countByUrlId(Long urlId);

    long countByUrlIdAndClickedAtAfter(
            Long urlId,
            LocalDateTime dateTime
    );

    List<ClickEvent> findByUrlId(Long urlId);

    void deleteByUrlId(Long urlId);
}