package com.strokeanalytics.backend.fit;

import java.time.Instant;

/**
 * A single decoded sample from a .fit "record" message, before it is
 * converted into a persisted {@code DataPoint} entity.
 */
public record ParsedDataPoint(
        Instant timestamp,
        Double speedMetersPerSecond,
        Integer heartRateBpm,
        Double distanceMeters,
        Double elevationMeters
) {
}
