package com.example.airportInfo.api.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {

    // asyncCacheMode=true: stores resolved value only; failed futures are never cached.
    // Without this, the CompletableFuture object itself gets cached — a failed future
    // would poison the cache for the full TTL.
    @Bean
    CaffeineCacheManager caffeineCacheManager(
            @Value("${spring.cache.caffeine.spec}") String caffeineSpec) {
        CaffeineCacheManager manager = new CaffeineCacheManager("metar");
        manager.setCacheSpecification(caffeineSpec);
        manager.setAsyncCacheMode(true);
        return manager;
    }
}
