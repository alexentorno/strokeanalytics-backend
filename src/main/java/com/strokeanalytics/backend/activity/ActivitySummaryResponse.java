package com.strokeanalytics.backend.activity;

import java.time.Instant;

/** Activity summary returned by the API; excludes the full time-series for a small payload. */
public record ActivitySummaryResponse(
        Long id,
        String name,
        Instant startTime,
        int dataPointCount
) {
    public static ActivitySummaryResponse from(Activity activity) {
        return new ActivitySummaryResponse(
                activity.getId(),
                activity.getName(),
                activity.getStartTime(),
                activity.getDataPoints().size()
        );
    }
}
