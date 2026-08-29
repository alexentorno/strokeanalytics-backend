package com.strokeanalytics.backend.datapoint;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface DataPointRepository extends JpaRepository<DataPoint, Long> {

    /**
     * Returns all data points of the given activity, ordered by time.
     * Used to render the full, un-zoomed chart.
     */
    List<DataPoint> findByActivityIdOrderByTimestampAsc(Long activityId);

    /**
     * Returns data points within a time window, ordered by time.
     * Used to compute avg/max speed, avg/max heart rate, distance and
     * elapsed time for a user-selected (zoomed) chart segment.
     */
    List<DataPoint> findByActivityIdAndTimestampBetweenOrderByTimestampAsc(
            Long activityId, Instant from, Instant to);
}
