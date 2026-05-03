package com.example.airportInfo.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@ConfigurationProperties(prefix = "aviation.weather")
public record AviationWeatherProperties(
        String baseUrl,
        List<String> validIcaoCodes,
        int connectTimeout,
        int readTimeout,
        int retryMaxAttempts,
        long retryDelay
) {}
