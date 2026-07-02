package com.loopers.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class PgClientConfig {

    /**
     * PG 전용 RestTemplate.
     * - connectTimeout: 1s (PG 서버 연결 대기)
     * - readTimeout: 3s (PG 응답 대기 — 요청 처리 최대 500ms × 여유)
     */
    @Bean("pgRestTemplate")
    public RestTemplate pgRestTemplate(RestTemplateBuilder builder) {
        return builder
            .connectTimeout(Duration.ofSeconds(1))
            .readTimeout(Duration.ofSeconds(3))
            .build();
    }
}
