package com.strokeanalytics.backend.stats;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Exposes aggregated stats for a time range within one activity — the data
 * shown when a user zooms into a segment of the chart on the frontend.
 * <p>
 * Example: {@code GET /api/activities/1/stats?from=2026-08-20T07:00:00Z&to=2026-08-20T07:05:00Z}
 */
@RestController
@RequestMapping("/api/activities/{activityId}/stats")
public class ActivityStatsController {

    private final ActivityStatsService activityStatsService;

    public ActivityStatsController(ActivityStatsService activityStatsService) {
        this.activityStatsService = activityStatsService;
    }

    @GetMapping
    public ActivityStatsResponse getStats(@PathVariable Long activityId,
                                           @RequestParam Instant from,
                                           @RequestParam Instant to) {
        return activityStatsService.computeStats(activityId, from, to);
    }
}
