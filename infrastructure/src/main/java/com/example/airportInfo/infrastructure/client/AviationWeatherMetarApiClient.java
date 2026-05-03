package com.example.airportInfo.infrastructure.client;

import com.example.airportInfo.infrastructure.dto.MetarResponse;
import com.example.airportInfo.infrastructure.exception.MetarFetchException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
public class AviationWeatherMetarApiClient implements MetarApiClient {

    private final RestClient restClient;

    public AviationWeatherMetarApiClient(@Qualifier("metarRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Async
    @Retryable(
            retryFor = {ResourceAccessException.class},
            maxAttemptsExpression = "${aviation.weather.retry-max-attempts:3}",
            backoff = @Backoff(delayExpression = "${aviation.weather.retry-delay:1000}")
    )
    @Override
    public CompletableFuture<List<MetarResponse>> fetch(String icao) {
        try {
            List<MetarResponse> result = restClient.get()
                    .uri("/api/data/metar?ids={icao}&format=json", icao)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
            return CompletableFuture.completedFuture(result);
        } catch (ResourceAccessException e) {
            log.warn("METAR fetch attempt failed for {}: {}", icao, e.getMessage());
            throw e;
        }
    }

    @Recover
    public CompletableFuture<List<MetarResponse>> recoverFetch(ResourceAccessException ex, String icao) {
        log.error("Failed to fetch METAR for {} after all retries: {}", icao, ex.getMessage());
        return CompletableFuture.failedFuture(new MetarFetchException("External API unavailable for ICAO: " + icao, ex));
    }
}
