package com.example.airportInfo;

import com.example.airportInfo.infrastructure.client.MetarApiClient;
import com.example.airportInfo.infrastructure.dto.MetarResponse;
import com.example.airportInfo.infrastructure.exception.MetarFetchException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MetarIntegrationTest {

    private static final String VALID_ICAO = "RCTP";
    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY = "dev-secret-key-change-me";

    @Autowired
    MockMvc mockMvc;

    @Autowired
    CacheManager cacheManager;

    @MockitoBean
    MetarApiClient metarApiClient;

    @BeforeEach
    void clearCache() {
        var cache = cacheManager.getCache("metar");
        if (cache != null) cache.clear();
    }

    @Test
    void getMetar_validRequest_returnsOkWithBody() throws Exception {
        when(metarApiClient.fetch(VALID_ICAO))
                .thenReturn(CompletableFuture.completedFuture(List.of(fakeMetarResponse())));

        MvcResult async = mockMvc.perform(get("/metar")
                        .param("icao", VALID_ICAO)
                        .header(API_KEY_HEADER, API_KEY))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(async))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].station.icao_id").value(VALID_ICAO))
                .andExpect(jsonPath("$[0].station.name").value("Taiwan Taoyuan Intl"))
                .andExpect(jsonPath("$[0].atmosphere.temperature").value(25.0))
                .andExpect(jsonPath("$[0].wind.direction").value(270));
    }

    @Test
    void getMetar_invalidIcao_returns400() throws Exception {
        // InvalidIcaoException thrown synchronously in the service — no async dispatch needed
        mockMvc.perform(get("/metar")
                        .param("icao", "XXXX")
                        .header(API_KEY_HEADER, API_KEY))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Invalid ICAO Code"));
    }

    @Test
    void getMetar_missingApiKey_returns401() throws Exception {
        // ApiKeyFilter rejects before the handler runs — no async dispatch needed
        mockMvc.perform(get("/metar")
                        .param("icao", VALID_ICAO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Unauthorized"));
    }

    @Test
    void getMetar_clientFailure_returns503() throws Exception {
        when(metarApiClient.fetch(VALID_ICAO))
                .thenReturn(CompletableFuture.failedFuture(
                        new MetarFetchException("External API unavailable", null)));

        MvcResult async = mockMvc.perform(get("/metar")
                        .param("icao", VALID_ICAO)
                        .header(API_KEY_HEADER, API_KEY))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(async))
                .andExpect(status().isServiceUnavailable())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("METAR Service Unavailable"));
    }

    private MetarResponse fakeMetarResponse() {
        return new MetarResponse(
                123456L, "RCTP", "2024-01-01 00:00:00", 1704067200L, "2024-01-01T00:00:00Z",
                25.0, 18.0, 270, 10, null,
                "10+", 29.92, 1013.2, 0,
                null, null, null, "FEW", null, null, "FEW", null,
                25.07, 121.23, 33.0, "Taiwan Taoyuan Intl",
                null, "RCTP 010000Z 27010KT 9999 FEW030 25/18 A2992",
                1, null
        );
    }
}
