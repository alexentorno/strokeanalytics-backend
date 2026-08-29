package com.strokeanalytics.backend.activity;

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

    private final ActivityImportService activityImportService;
    private final ActivityRepository activityRepository;
    private final DataPointRepository dataPointRepository;

    public ActivityController(ActivityImportService activityImportService,
                               ActivityRepository activityRepository,
                               DataPointRepository dataPointRepository) {
        this.activityImportService = activityImportService;
        this.activityRepository = activityRepository;
        this.dataPointRepository = dataPointRepository;
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

    /** Returns the full time-series for one activity, used to render its chart. */
    @GetMapping("/{activityId}/data-points")
    public List<DataPointResponse> getDataPoints(@PathVariable Long activityId) {
        return dataPointRepository.findByActivityIdOrderByTimestampAsc(activityId).stream()
                .map(DataPointResponse::from)
                .toList();
    }
}
