package com.strokeanalytics.backend.fit;

import java.time.Instant;
import java.util.List;

/**
 * Result of decoding a single .fit file: the activity's start time plus its
 * full time-series, not yet converted into persisted entities.
 */
public record ParsedActivity(
        Instant startTime,
        List<ParsedDataPoint> dataPoints
) {
}
