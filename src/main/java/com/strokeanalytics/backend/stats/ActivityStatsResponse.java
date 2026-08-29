package com.strokeanalytics.backend.stats;

import java.time.Instant;

/**
 * Aggregated metrics for a user-selected (zoomed) segment of a chart.
 * <p>
 * Any metric can be {@code null} when the underlying sensor produced no
 * readings in the selected range (e.g. no heart rate strap connected).
 */
public record ActivityStatsResponse(
        Instant from,
        Instant to,
        long elapsedSeconds,
        int sampleCount,
        Double distanceMeters,
        Double averageSpeedKmh,
        Double maxSpeedKmh,
        Double averageHeartRateBpm,
        Integer maxHeartRateBpm
) {
}
