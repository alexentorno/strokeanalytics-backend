package com.strokeanalytics.backend.activity;

import com.strokeanalytics.backend.datapoint.DataPoint;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single imported training session (e.g. one Garmin .fit file).
 * <p>
 * An Activity owns an ordered collection of {@link DataPoint} records that
 * together form the time-series used to render charts.
 */
@Entity
@Table(name = "activities")
public class Activity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Human-readable name. Defaults to the source file name at import time. */
    @Column(nullable = false)
    private String name;

    /** Moment the activity started, derived from the first record in the .fit file. */
    @Column(nullable = false)
    private Instant startTime;

    /** Original uploaded file name, kept for traceability. */
    @Column(nullable = false)
    private String sourceFileName;

    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private List<DataPoint> dataPoints = new ArrayList<>();

    /** Required by JPA. */
    protected Activity() {
    }

    public Activity(String name, Instant startTime, String sourceFileName) {
        this.name = name;
        this.startTime = startTime;
        this.sourceFileName = sourceFileName;
    }

    /**
     * Attaches a data point to this activity and keeps both sides of the
     * bidirectional relationship in sync.
     */
    public void addDataPoint(DataPoint dataPoint) {
        dataPoints.add(dataPoint);
        dataPoint.assignToActivity(this);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public String getSourceFileName() {
        return sourceFileName;
    }

    public List<DataPoint> getDataPoints() {
        return dataPoints;
    }
}
