package com.strokeanalytics.backend.datapoint;

import com.strokeanalytics.backend.activity.Activity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * A single time-series sample captured during a training session.
 * <p>
 * Every field except {@code timestamp} is nullable, because not every Garmin
 * sensor is active during every session (e.g. no heart rate strap connected).
 */
@Entity
@Table(
        name = "data_points",
        indexes = @Index(name = "idx_data_points_activity_timestamp", columnList = "activity_id, timestamp")
)
public class DataPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "activity_id", nullable = false)
    private Activity activity;

    private Instant timestamp;

    /** Speed in meters per second. Null if no GPS/speed sensor data is available. */
    private Double speedMetersPerSecond;

    /** Heart rate in beats per minute. Null if no heart rate sensor was connected. */
    private Integer heartRateBpm;

    /** Cumulative distance in meters since the start of the activity. */
    private Double distanceMeters;

    /** Elevation above sea level in meters. Null if not recorded. */
    private Double elevationMeters;

    /** Required by JPA. */
    protected DataPoint() {
    }

    public DataPoint(Instant timestamp,
                      Double speedMetersPerSecond,
                      Integer heartRateBpm,
                      Double distanceMeters,
                      Double elevationMeters) {
        this.timestamp = timestamp;
        this.speedMetersPerSecond = speedMetersPerSecond;
        this.heartRateBpm = heartRateBpm;
        this.distanceMeters = distanceMeters;
        this.elevationMeters = elevationMeters;
    }

    /** Package-private link-back, set only by {@link Activity#addDataPoint}. */
    public void assignToActivity(Activity activity) {
        this.activity = activity;
    }

    public Long getId() {
        return id;
    }

    public Activity getActivity() {
        return activity;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Double getSpeedMetersPerSecond() {
        return speedMetersPerSecond;
    }

    public Integer getHeartRateBpm() {
        return heartRateBpm;
    }

    public Double getDistanceMeters() {
        return distanceMeters;
    }

    public Double getElevationMeters() {
        return elevationMeters;
    }
}
