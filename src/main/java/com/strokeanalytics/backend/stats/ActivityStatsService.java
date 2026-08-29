package com.strokeanalytics.backend.stats;

import com.strokeanalytics.backend.datapoint.DataPoint;
import com.strokeanalytics.backend.datapoint.DataPointRepository;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

/**
 * Computes summary statistics (avg/max speed, avg/max heart rate, distance,
 * elapsed time) for a user-selected segment of an activity's chart.
 * <p>
 * This is the backend counterpart of the "zoom in and see numbers for this
 * segment" feature — each call answers one such selection.
 */
@Service
public class ActivityStatsService {

    /** 1 m/s = 3.6 km/h. */
    private static final double METERS_PER_SECOND_TO_KMH = 3.6;


    private final DataPointRepository dataPointRepository;

    public ActivityStatsService(DataPointRepository dataPointRepository) {
        this.dataPointRepository = dataPointRepository;
    }

    /**
     * Computes stats for the given activity restricted to {@code [from, to]}.
     *
     * @throws InvalidTimeRangeException if {@code from} is not strictly before {@code to}
     * @throws EmptyDataRangeException   if no data points fall within the range
     */
    public ActivityStatsResponse computeStats(Long activityId, Instant from, Instant to) {
        validateRange(from, to);

        List<DataPoint> dataPoints =
                dataPointRepository.findByActivityIdAndTimestampBetweenOrderByTimestampAsc(activityId, from, to);

        if (dataPoints.isEmpty()) {
            throw new EmptyDataRangeException(
                    "No data points found for activity " + activityId + " between " + from + " and " + to);
        }

        return new ActivityStatsResponse(
                from,
                to,
                computeElapsedSeconds(dataPoints),
                dataPoints.size(),
                computeDistanceCovered(dataPoints),
                computeAverageSpeed(dataPoints),
                computeMaxSpeed(dataPoints),
                computeAverageHeartRate(dataPoints),
                computeMaxHeartRate(dataPoints)
        );
    }

    private void validateRange(Instant from, Instant to) {
        if (!from.isBefore(to)) {
            throw new InvalidTimeRangeException("'from' (" + from + ") must be strictly before 'to' (" + to + ")");
        }
    }

    /** Elapsed wall-clock time between the first and last sample actually found in range. */
    private long computeElapsedSeconds(List<DataPoint> dataPoints) {
        Instant first = dataPoints.get(0).getTimestamp();
        Instant last = dataPoints.get(dataPoints.size() - 1).getTimestamp();
        return Duration.between(first, last).getSeconds();
    }

    /**
     * Distance is cumulative in the source data, so the distance covered in
     * a segment is the difference between its last and first known reading.
     * Returns {@code null} if no distance data was recorded in this range.
     */
    private Double computeDistanceCovered(List<DataPoint> dataPoints) {
        List<Double> distances = dataPoints.stream()
                .map(DataPoint::getDistanceMeters)
                .filter(distance -> distance != null)
                .toList();

        if (distances.isEmpty()) {
            return null;
        }
        return distances.get(distances.size() - 1) - distances.get(0);
    }

    private Double computeAverageSpeed(List<DataPoint> dataPoints) {
        OptionalDouble averageMetersPerSecond = dataPoints.stream()
                .map(DataPoint::getSpeedMetersPerSecond)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .average();
        return averageMetersPerSecond.isPresent()
                ? toKmh(averageMetersPerSecond.getAsDouble())
                : null;

    }

    private Double computeMaxSpeed(List<DataPoint> dataPoints) {
        return dataPoints.stream()
                .map(DataPoint::getSpeedMetersPerSecond)
                .filter(Objects::nonNull)
                .max(Comparator.naturalOrder())
                .map(this::toKmh)
                .orElse(null);

    }

    private double toKmh(double speedMetersPerSecond) {
        return speedMetersPerSecond * METERS_PER_SECOND_TO_KMH;
    }


    private Double computeAverageHeartRate(List<DataPoint> dataPoints) {
        OptionalDouble average = dataPoints.stream()
                .map(DataPoint::getHeartRateBpm)
                .filter(heartRate -> heartRate != null)
                .mapToInt(Integer::intValue)
                .average();
        return average.isPresent() ? average.getAsDouble() : null;
    }

    private Integer computeMaxHeartRate(List<DataPoint> dataPoints) {
        OptionalInt max = dataPoints.stream()
                .map(DataPoint::getHeartRateBpm)
                .filter(heartRate -> heartRate != null)
                .mapToInt(Integer::intValue)
                .max();
        return max.isPresent() ? max.getAsInt() : null;
    }
}
