package com.example.airportInfo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GetMetarResponse(
        Station station,
        Time time,
        Atmosphere atmosphere,
        Wind wind,
        @JsonProperty("visibility_and_sky") VisibilityAndSky visibilityAndSky,
        Metadata metadata
) {
    public record Station(
            @JsonProperty("icao_id") String icaoId,
            String name,
            Location location
    ) {
        public record Location(
                Double latitude,
                Double longitude,
                Double elevation
        ) {}
    }

    public record Time(
            @JsonProperty("receipt_time") String receiptTime,
            @JsonProperty("observation_time_unix") Long observationTimeUnix,
            @JsonProperty("report_time") String reportTime
    ) {}

    public record Atmosphere(
            Double temperature,
            @JsonProperty("dew_point") Double dewPoint,
            Double altimeter,
            @JsonProperty("sea_level_pressure") Double seaLevelPressure
    ) {}

    public record Wind(
            Integer direction,
            Integer speed,
            Integer gust
    ) {}

    public record VisibilityAndSky(
            String visibility,
            String cover,
            @JsonProperty("sky_conditions") List<String> skyConditions,
            @JsonProperty("weather_string") String weatherString
    ) {}

    public record Metadata(
            @JsonProperty("metar_id") Long metarId,
            @JsonProperty("raw_observation") String rawObservation,
            @JsonProperty("qc_field") Integer qcField,
            @JsonProperty("most_recent") Integer mostRecent
    ) {}
}
