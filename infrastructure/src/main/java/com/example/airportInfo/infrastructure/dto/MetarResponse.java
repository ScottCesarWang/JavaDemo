package com.example.airportInfo.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record MetarResponse(
        @JsonProperty("metar_id") Long metarId,
        String icaoId,
        String receiptTime,
        Long obsTime,
        String reportTime,
        Double temp,
        Double dewp,
        Integer wdir,
        Integer wspd,
        Integer wgst,
        String visib,
        Double altim,
        Double slp,
        Integer qcField,
        String wxString,
        String presentWx,
        String skyClass,
        String skyCoverage,
        Integer skyBas,
        String sky,
        String cover,
        Integer loCloud,
        Double latitude,
        Double longitude,
        Double elevation,
        String name,
        Integer prior,
        String rawOb,
        Integer mostRecent,
        Integer stationCount
) {}
