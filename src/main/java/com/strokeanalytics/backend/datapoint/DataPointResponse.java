package com.strokeanalytics.backend.datapoint;

import java.time.Instant;

/** One chart sample, shaped for direct consumption by the frontend charting library. */
public record DataPointResponse(
        Instant timestamp,
        Double speedMetersPerSecond,
        Integer heartRateBpm,
        Double distanceMeters,
        Double elevationMeters
) {
    public static DataPointResponse from(DataPoint dataPoint) {
        return new DataPointResponse(
                dataPoint.getTimestamp(),
                dataPoint.getSpeedMetersPerSecond(),
                dataPoint.getHeartRateBpm(),
                dataPoint.getDistanceMeters(),
                dataPoint.getElevationMeters()
        );
    }
}
