# StrokeAnalytics — Backend

Spring Boot service that imports Garmin `.fit` activity files, stores their
time-series data (speed, heart rate, distance, elevation), and exposes a REST
API for the frontend charts.

## Prerequisites

- JDK 17+
- Maven 3.9+
- PostgreSQL 14+ running locally (or update `application.yml` to point elsewhere)

## Database setup

```sql
CREATE DATABASE strokeanalytics;
CREATE USER strokeanalytics WITH PASSWORD 'strokeanalytics';
GRANT ALL PRIVILEGES ON DATABASE strokeanalytics TO strokeanalytics;
```

Credentials match the defaults in `src/main/resources/application.yml` —
change both if you use different values.

## Running locally

```bash
mvn spring-boot:run
```

The API starts on `http://localhost:8080`.

> **Note on this build:** this project was scaffolded in a sandboxed
> environment without access to Maven Central, so it has **not** been
> compiled or run here. Everything is written against the confirmed public
> API of the official Garmin FIT Java SDK (see
> https://github.com/garmin/fit-java-sdk), but do a `mvn compile` locally as
> a first sanity check before building further on top of it.

## API

| Method | Path                                       | Description                                     |
|--------|---------------------------------------------|--------------------------------------------------|
| POST   | `/api/activities/import`                    | Upload a `.fit` file (multipart field `file`)     |
| GET    | `/api/activities`                            | List imported activities                          |
| GET    | `/api/activities/{id}/data-points?maxPoints=N` | Time-series for one activity, downsampled to at most `N` points via LTTB (default 2000; pass `maxPoints=0` for full resolution) |
| GET    | `/api/activities/{id}/stats?from=...&to=...` | Avg/max speed & heart rate, distance, elapsed time for a selected segment — always computed from full-resolution data, independent of the downsampling above (`from`/`to` as ISO-8601 instants, e.g. `2026-08-20T07:00:00Z`) |

Example upload (Linux/macOS):

```bash
curl -F "file=@morning_paddle.fit" http://localhost:8080/api/activities/import
```

Example upload (Windows PowerShell — `curl` is aliased to `Invoke-WebRequest`
there, so either call the real curl binary or use `-Form`):

```powershell
curl.exe -F "file=@morning_paddle.fit" http://localhost:8080/api/activities/import
# or
Invoke-RestMethod -Uri "http://localhost:8080/api/activities/import" -Method Post -Form @{ file = Get-Item "morning_paddle.fit" }
```

Example stats query:

```bash
curl "http://localhost:8080/api/activities/1/stats?from=2026-08-20T07:00:00Z&to=2026-08-20T07:05:00Z"
```

Errors (invalid range, no data in range, bad `.fit` file) now return a
readable JSON body instead of a bare 500, e.g.:

```json
{ "message": "No data points found for activity 1 between 2026-08-20T07:00:00Z and 2026-08-20T07:05:00Z" }
```

## Where to get a `.fit` file to test with

Garmin Connect → an activity → **⋯** menu → **Export Original**.

## Project structure

```
src/main/java/com/strokeanalytics/backend/
├── activity/     # Activity entity, repository, import service, REST controller
├── datapoint/    # DataPoint entity, repository, response DTO, LTTB downsampler
└── fit/          # Format-specific .fit decoding (Garmin FIT SDK), isolated
                  # behind the FitFileParser interface so it stays swappable
                  # and unit-testable without a real .fit file
```

## Next steps (see overall project plan)

1. Add unit tests for `GarminFitFileParser` and `ActivityStatsService` using
   a small sample `.fit` file committed to `src/test/resources`.
2. Switch `hibernate.ddl-auto` to `validate` and introduce Flyway migrations
   once the schema stabilizes.
