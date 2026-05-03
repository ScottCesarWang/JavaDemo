# airportInfo

A Spring Boot REST API for querying real-time **METAR** weather data for airports worldwide, sourced from [aviationweather.gov](https://aviationweather.gov).

## Stack

| | |
|---|---|
| **Runtime** | Java 25 |
| **Framework** | Spring Boot 4.0.6 (Spring MVC) |
| **Build** | Maven (use `./mvnw`, not system `mvn`) |
| **Cache** | Caffeine (in-memory, async mode) |
| **Resilience** | Spring Retry (`@Retryable`) |
| **Async** | `@Async` + `CompletableFuture` |

---

## Requirements

- Java 21+
- No Maven installation needed — the project ships with `./mvnw`

---

## Quick Start

```bash
# Run
./mvnw spring-boot:run -pl api -am

# Build (skip tests)
./mvnw package -DskipTests

# Test
./mvnw test
```

Default port: **8080**  
Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

---

## Project Structure

Multi-module Maven project following Clean Architecture.

```
airportInfo/
├── domain/         ← Domain DTOs and exceptions (not used in this project)
├── infrastructure/ ← External API client
└── api/            ← Controllers, services, filters, app entry point
```
Module dependency: `api` → `infrastructure`

### Request Flow

```
HTTP Request
    │
    ▼
ApiKeyFilter      (Order 1) — validates X-API-Key header → 401 if missing/invalid
    │
RateLimitFilter   (Order 2) — token bucket per key, 10 req/min → 429 if exceeded
    │
MetarController             — returns CompletableFuture (async MVC)
    │
IcaoWeatherMetarServiceImpl — validates ICAO, @Cacheable (Caffeine, TTL 5m)
    │
AviationWeatherMetarApiClient — @Async + @Retryable (3 attempts, 1s backoff)
    │
    ▼
https://aviationweather.gov/api/data/metar
```

---

## API

### Authentication

Every request must include an `X-API-Key` header.

```
X-API-Key: dev-secret-key-change-me
```

Missing or incorrect key → **401 Unauthorized**.

---

### `GET /metar`

Returns real-time METAR data for the given airport.

**Query Parameters**

| Parameter | Type   | Required | Description          |
|-----------|--------|----------|----------------------|
| `icao`    | String | Yes      | ICAO airport code    |

**Example**

```bash
curl -H "X-API-Key: dev-secret-key-change-me" \
     "http://localhost:8080/metar?icao=RCTP"
```

**200 OK**

```json
[
  {
    "station": {
      "icao_id": "RCTP",
      "name": "Taiwan Taoyuan Intl, TW",
      "location": {
        "latitude": 25.07,
        "longitude": 121.23,
        "elevation": 33.0
      }
    },
    "time": {
      "receipt_time": "2026-05-03T13:00:00.000Z",
      "observation_time_unix": 1746277200,
      "report_time": "2026-05-03T13:00:00.000Z"
    },
    "atmosphere": {
      "temperature": 28.0,
      "dew_point": 23.0,
      "altimeter": 29.89,
      "sea_level_pressure": 1013.2
    },
    "wind": {
      "direction": 270,
      "speed": 10,
      "gust": null
    },
    "visibility_and_sky": {
      "visibility": "10+",
      "cover": "FEW",
      "sky_conditions": ["FEW"],
      "weather_string": null
    },
    "metadata": {
      "metar_id": 123456,
      "raw_observation": "RCTP 031300Z 27010KT 9999 FEW030 28/23 A2989",
      "qc_field": 0,
      "most_recent": 1
    }
  }
]
```

---

### Error Responses
**400 — Invalid ICAO code**

```bash
curl -H "X-API-Key: dev-secret-key-change-me" \
     "http://localhost:8080/metar?icao=XXXX"
```

```json
{
  "type": "about:blank",
  "title": "Invalid ICAO Code",
  "status": 400,
  "detail": "Invalid or unsupported ICAO code: XXXX",
  "instance": "/metar"
}
```

**401 — Missing or invalid API key**

```bash
curl "http://localhost:8080/metar?icao=RCTP"
```

```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Invalid or missing API key"
}
```

**429 — Rate limit exceeded**

```json
{
  "type": "about:blank",
  "title": "Too Many Requests",
  "status": 429,
  "detail": "Rate limit exceeded. Max 10 requests per minute."
}
```

**503 — External API unavailable**

```json
{
  "type": "about:blank",
  "title": "METAR Service Unavailable",
  "status": 503,
  "detail": "External API unavailable for ICAO: RCTP",
  "instance": "/metar"
}
```

---

## Configuration

File: `api/src/main/resources/application.properties`

| Property | Default | Description |
|----------|---------|-------------|
| `security.api-key` | `dev-secret-key-change-me` | Required `X-API-Key` header value |
| `security.rate-limit-per-minute` | `10` | Max requests per API key per minute |
| `spring.cache.caffeine.spec` | `expireAfterWrite=5m,maximumSize=500` | Cache TTL and max entries |
| `aviation.weather.base-url` | `https://aviationweather.gov` | External METAR data source |
| `aviation.weather.connect-timeout` | `3000` | Connection timeout (ms) |
| `aviation.weather.read-timeout` | `5000` | Read timeout (ms) |
| `aviation.weather.retry-max-attempts` | `3` | Retry attempts on network error |
| `aviation.weather.retry-delay` | `1000` | Delay between retries (ms) |
| `aviation.weather.valid-icao-codes` | *(see below)* | Comma-separated allowed ICAO codes |

### Allowed ICAO Codes (100 airports)

| Region | Codes |
|--------|-------|
| **East Asia** | RCTP RCSS RCKH VHHH RJTT RJAA RJBB RJCC RKSI RKSS ZBAA ZSPD ZGGG ZSSS |
| **Southeast Asia** | WMKK WSSS VTBS VTSP VVNB VVTS RPLL WIII WADD WBKK |
| **North America** | KJFK KLAX KORD KATL KDFW KDEN KSFO KSEA KMIA KLAS KPHX KIAH KBOS KEWR KMCO KMSP KDTW KPHL KCLT KDCA KIAD CYYZ CYVR CYUL MMMX |
| **South America** | SBGR SBRJ SCEL SKBO |
| **Europe** | EGLL EGKK EGGW EGSS LFPG LFPO LFMN EDDF EDDM EDDB EDDL EHAM EBBR ESSA ENGM EKCH EFHK LEMD LEBL LIMC LIRF LOWW LSZH LPPT LGAV UUDD UUEE |
| **Middle East** | OMDB OTHH OMAA OEJN OERK OKKK OBBI LLBG |
| **Africa** | HECA HAAB FAOR FACT DNMM DGAA HKJK |
| **Oceania** | YSSY YMML YBBN YPPH NZAA NZCH |

---

## Testing

```bash
./mvnw test
```

`MetarIntegrationTest` runs the complete HTTP stack (`@SpringBootTest` + `MockMvc`) with a mocked `MetarApiClient`, covering:

| Scenario | Status |
|----------|--------|
| Valid ICAO + valid API key | 200 with full JSON body |
| Invalid ICAO | 400 ProblemDetail |
| Missing API key | 401 (from filter, before handler) |
| External API failure | 503 ProblemDetail |


# AI Agent
This project relies on an AI agent to finish as I mainly use dotnet/C# to develop backend systems before.
I did the simple implementation of API which fulfills the basic requirement.
Afterward, I enhanced the API according to my past experience to add features such as
- Configurations
- Logging
- Authentication (API key)
- Error Handling / Global Exception Handling
- Rate Limiting
- Caching
- Asynchronous Processing
- Unit Testing
- etc...
which are functions that dotnet supports as well.
The AI agent helps me to add the features, and I did the review and feedback to the AI agent.