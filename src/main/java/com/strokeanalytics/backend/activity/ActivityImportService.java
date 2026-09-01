package com.strokeanalytics.backend.activity;

import com.strokeanalytics.backend.datapoint.DataPoint;
import com.strokeanalytics.backend.fit.FitFileParser;
import com.strokeanalytics.backend.fit.ParsedActivity;
import com.strokeanalytics.backend.fit.ParsedDataPoint;
import org.springframework.stereotype.Service;

import java.io.InputStream;

/**
 * Orchestrates the import of a .fit file: parsing it, converting the result
 * into persistent entities, and saving the whole activity in one call.
 * <p>
 * Kept separate from {@link FitFileParser}
 * on purpose: parsing has no knowledge of JPA entities, and this class has
 * no knowledge of the .fit binary format.
 */
@Service
public class ActivityImportService {

    private final FitFileParser fitFileParser;
    private final ActivityRepository activityRepository;

    public ActivityImportService(FitFileParser fitFileParser, ActivityRepository activityRepository) {
        this.fitFileParser = fitFileParser;
        this.activityRepository = activityRepository;
    }

    /**
     * Imports a single .fit file and persists it as a new {@link Activity}.
     *
     * @param fitFileStream  raw .fit file bytes
     * @param sourceFileName original uploaded file name, used as the default activity name
     * @return the persisted activity, including its generated id
     */
    public Activity importFitFile(InputStream fitFileStream, String sourceFileName) {
        ParsedActivity parsedActivity = fitFileParser.parse(fitFileStream);
        Activity activity = buildActivity(parsedActivity, sourceFileName);
        return activityRepository.save(activity);
    }

    private Activity buildActivity(ParsedActivity parsedActivity, String sourceFileName) {
        Activity activity = new Activity(sourceFileName, parsedActivity.startTime(), sourceFileName);
        for (ParsedDataPoint parsedDataPoint : parsedActivity.dataPoints()) {
            activity.addDataPoint(toDataPoint(parsedDataPoint));
        }
        return activity;
    }

    private DataPoint toDataPoint(ParsedDataPoint parsedDataPoint) {
        return new DataPoint(
                parsedDataPoint.timestamp(),
                parsedDataPoint.speedMetersPerSecond(),
                parsedDataPoint.heartRateBpm(),
                parsedDataPoint.distanceMeters(),
                parsedDataPoint.elevationMeters()
        );
    }
}
