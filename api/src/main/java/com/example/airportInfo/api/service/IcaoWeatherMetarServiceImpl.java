package com.example.airportInfo.api.service;

import com.example.airportInfo.api.Interface.MetarService;
import com.example.airportInfo.api.dto.GetMetarResponse;
import com.example.airportInfo.api.exception.InvalidIcaoException;
import com.example.airportInfo.infrastructure.client.MetarApiClient;
import com.example.airportInfo.infrastructure.config.AviationWeatherProperties;
import com.example.airportInfo.infrastructure.dto.MetarResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Service
public class IcaoWeatherMetarServiceImpl implements MetarService {

    private final MetarApiClient metarApiClient;
    private final List<String> validIcaoCodes;

    public IcaoWeatherMetarServiceImpl(MetarApiClient metarApiClient, AviationWeatherProperties properties) {
        this.metarApiClient = metarApiClient;
        this.validIcaoCodes = properties.validIcaoCodes();
    }

    @Cacheable(cacheNames = "metar", key = "#icao.toUpperCase()")
    @Override
    public CompletableFuture<List<GetMetarResponse>> getMetar(String icao) {
        try {
            String normalized = icao.toUpperCase();
            if (!validIcaoCodes.contains(normalized)) {
                throw new InvalidIcaoException(icao);
            }
            return metarApiClient.fetch(normalized)
                    .thenApply(results -> {
                        log.info("Successfully fetched METAR for {}", normalized);
                        return results.stream().map(this::toGetMetarResponse).toList();
                    })
                    .exceptionally(ex -> {
                        Throwable cause = (ex instanceof CompletionException ce) ? ce.getCause() : ex;
                        log.error("Failed to process METAR for {}: {}", normalized, cause.getMessage());
                        if (cause instanceof RuntimeException re) throw re;
                        throw new CompletionException(cause);
                    });
        } catch (InvalidIcaoException e) {
            log.warn("Rejected invalid ICAO: {}", icao);
            throw e;
        }
    }

    private GetMetarResponse toGetMetarResponse(MetarResponse raw) {
        var location = new GetMetarResponse.Station.Location(raw.latitude(), raw.longitude(), raw.elevation());
        var station = new GetMetarResponse.Station(raw.icaoId(), raw.name(), location);
        var time = new GetMetarResponse.Time(raw.receiptTime(), raw.obsTime(), raw.reportTime());
        var atmosphere = new GetMetarResponse.Atmosphere(raw.temp(), raw.dewp(), raw.altim(), raw.slp());
        var wind = new GetMetarResponse.Wind(raw.wdir(), raw.wspd(), raw.wgst());
        List<String> skyConditions = (raw.skyCoverage() != null && !raw.skyCoverage().isBlank())
                ? List.of(raw.skyCoverage())
                : List.of();
        var visibilityAndSky = new GetMetarResponse.VisibilityAndSky(raw.visib(), raw.cover(), skyConditions, raw.wxString());
        var metadata = new GetMetarResponse.Metadata(raw.metarId(), raw.rawOb(), raw.qcField(), raw.mostRecent());
        return new GetMetarResponse(station, time, atmosphere, wind, visibilityAndSky, metadata);
    }
}
