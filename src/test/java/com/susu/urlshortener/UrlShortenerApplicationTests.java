package com.susu.urlshortener;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "JWT_SECRET=${JWT_SECRET}",
        "spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/url_shortener}",
        "spring.datasource.username=${DB_USERNAME:root}",
        "spring.datasource.password=${DB_PASSWORD}"
})
class UrlShortenerApplicationTests {

    @Test
    void contextLoads() {
    }
}