package com.strokeanalytics.backend.activity;

import com.strokeanalytics.backend.datapoint.DataPoint;
import com.strokeanalytics.backend.datapoint.DataPointDownsampler;
import com.strokeanalytics.backend.datapoint.DataPointRepository;
import com.strokeanalytics.backend.datapoint.DataPointResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

/**
 * REST endpoints for importing activities and reading their data for chart
 * rendering.
 */
@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    /**
     * Default cap on points returned by /data-points when the caller doesn't
     * specify one. Chosen as a point past which a browser chart stops
     * feeling smooth to drag/zoom, not for any data-accuracy reason — the
     * /stats endpoint always computes from full-resolution stored data,
     * independent of this.
     */
    private static final int DEFAULT_MAX_CHART_POINTS = 2000;

    private final ActivityImportService activityImportService;
    private final ActivityRepository activityRepository;
    private final DataPointRepository dataPointRepository;
    private final DataPointDownsampler dataPointDownsampler;

    public ActivityController(ActivityImportService activityImportService,
                               ActivityRepository activityRepository,
                               DataPointRepository dataPointRepository,
                               DataPointDownsampler dataPointDownsampler) {
        this.activityImportService = activityImportService;
        this.activityRepository = activityRepository;
        this.dataPointRepository = dataPointRepository;
        this.dataPointDownsampler = dataPointDownsampler;
    }

    /** Uploads and imports a single Garmin .fit file. */
    @PostMapping("/import")
    public ResponseEntity<ActivitySummaryResponse> importActivity(@RequestParam("file") MultipartFile file) {
        Activity importedActivity = importFile(file);
        return ResponseEntity.status(HttpStatus.CREATED).body(ActivitySummaryResponse.from(importedActivity));
    }

    private Activity importFile(MultipartFile file) {
        try {
            return activityImportService.importFitFile(file.getInputStream(), file.getOriginalFilename());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read uploaded file", e);
        }
    }

    /** Lists all imported activities as lightweight summaries (no chart data). */
    @GetMapping
    public List<ActivitySummaryResponse> listActivities() {
        return activityRepository.findAll().stream()
                .map(ActivitySummaryResponse::from)
                .toList();
    }

    /**
     * Returns the time-series for one activity, used to render its chart.
     * <p>
     * Downsampled to at most {@code maxPoints} using LTTB when the activity
     * has more raw samples than that, so long sessions stay smooth to
     * render, drag and zoom in the browser. Pass {@code maxPoints=0} (or any
     * value ≥ the raw sample count) to get full-resolution data instead.
     */
    @GetMapping("/{activityId}/data-points")
    public List<DataPointResponse> getDataPoints(
            @PathVariable Long activityId,
            @RequestParam(defaultValue = "" + DEFAULT_MAX_CHART_POINTS) int maxPoints) {

        List<DataPoint> dataPoints = dataPointRepository.findByActivityIdOrderByTimestampAsc(activityId);
        List<DataPoint> pointsToReturn = maxPoints > 0
                ? dataPointDownsampler.downsample(dataPoints, maxPoints)
                : dataPoints;

        return pointsToReturn.stream()
                .map(DataPointResponse::from)
                .toList();
    }
}
