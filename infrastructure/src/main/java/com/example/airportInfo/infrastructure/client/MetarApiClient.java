package com.example.airportInfo.infrastructure.client;

import com.example.airportInfo.infrastructure.dto.MetarResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MetarApiClient {
    CompletableFuture<List<MetarResponse>> fetch(String icao);
}
