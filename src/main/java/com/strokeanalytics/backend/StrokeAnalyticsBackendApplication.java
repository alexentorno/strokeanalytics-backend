package com.strokeanalytics.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point of the StrokeAnalytics backend service.
 * <p>
 * Responsibilities of this service:
 * <ul>
 *     <li>import Garmin .fit activity files;</li>
 *     <li>persist parsed training data (speed, heart rate, distance, elevation over time);</li>
 *     <li>expose REST endpoints consumed by the frontend to render interactive charts.</li>
 * </ul>
 */
@SpringBootApplication
public class StrokeAnalyticsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(StrokeAnalyticsBackendApplication.class, args);
    }
}
