package com.example.airportInfo.api.controller;

import com.example.airportInfo.api.dto.GetMetarRequest;
import com.example.airportInfo.api.dto.GetMetarResponse;
import com.example.airportInfo.api.Interface.MetarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
public class MetarController {

    private final MetarService metarService;

    public MetarController(MetarService metarService) {
        this.metarService = metarService;
    }

    @GetMapping("/metar")
    public CompletableFuture<ResponseEntity<List<GetMetarResponse>>> getMetar(@ModelAttribute GetMetarRequest request) {
        try {
            return metarService.getMetar(request.icao())
                    .thenApply(body -> ResponseEntity.ok()
                            .contentType(MediaType.APPLICATION_JSON)
                            .<List<GetMetarResponse>>body(body));
        } catch (Exception e) {
            log.error("Error handling METAR request for {}: {}", request.icao(), e.getMessage());
            throw e;
        }
    }
}
