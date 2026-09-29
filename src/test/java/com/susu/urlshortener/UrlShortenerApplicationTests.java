package com.susu.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
        properties = {
                "spring.datasource.url=jdbc:mysql://localhost:3306/url_shortener",
                "spring.datasource.username=root",
                "spring.datasource.password=${DB_PASSWORD}",
                "spring.data.redis.url=redis://localhost:6380",
                "JWT_SECRET=${JWT_SECRET}"
        }
)
class UrlShortenerApplicationTests {

    @Test
    void contextLoads() {
    }
}