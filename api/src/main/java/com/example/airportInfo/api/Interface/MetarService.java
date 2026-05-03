package com.example.airportInfo.api.Interface;

import com.example.airportInfo.api.dto.GetMetarResponse;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface MetarService {
    CompletableFuture<List<GetMetarResponse>> getMetar(String icao);
}
