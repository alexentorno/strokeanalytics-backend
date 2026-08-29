package com.strokeanalytics.backend.fit;

import com.garmin.fit.Decode;
import com.garmin.fit.FitRuntimeException;
import com.garmin.fit.MesgBroadcaster;
import com.garmin.fit.RecordMesg;
import com.garmin.fit.RecordMesgListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link FitFileParser} implementation backed by the official Garmin FIT
 * Java SDK ({@code com.garmin:fit}, see https://github.com/garmin/fit-java-sdk).
 * <p>
 * Only "record" messages are consumed, since they carry the per-second
 * time-series (speed, heart rate, distance, altitude) that the charts need.
 * Other message types (laps, sessions, device info) are ignored for now and
 * can be added later without changing this class's public contract.
 */
@Component
public class GarminFitFileParser implements FitFileParser {

    @Override
    public ParsedActivity parse(InputStream fitFileStream) {
        RecordCollectingListener recordCollector = new RecordCollectingListener();
        runDecoder(fitFileStream, recordCollector);

        List<ParsedDataPoint> dataPoints = recordCollector.getCollectedDataPoints();
        Instant startTime = resolveStartTime(dataPoints);

        return new ParsedActivity(startTime, dataPoints);
    }

    /** Feeds the raw stream through the Garmin decoder, routing record messages to the listener. */
    private void runDecoder(InputStream fitFileStream, RecordMesgListener recordListener) {
        try {
            Decode decoder = new Decode();
            MesgBroadcaster broadcaster = new MesgBroadcaster(decoder);
            broadcaster.addListener(recordListener);
            broadcaster.run(fitFileStream);
        } catch (FitRuntimeException e) {
            throw new FitParsingException("Failed to decode .fit file", e);
        }
    }

    /**
     * The subset of messages we read does not always expose an explicit
     * session start timestamp, so the start time is derived from the first
     * recorded sample instead.
     */
    private Instant resolveStartTime(List<ParsedDataPoint> dataPoints) {
        if (dataPoints.isEmpty()) {
            throw new FitParsingException("File does not contain any record messages", null);
        }
        return dataPoints.get(0).timestamp();
    }

    /**
     * Collects every "record" message emitted by the FIT decoder and
     * converts it into a framework-agnostic {@link ParsedDataPoint}.
     */
    private static class RecordCollectingListener implements RecordMesgListener {

        private final List<ParsedDataPoint> collectedDataPoints = new ArrayList<>();

        @Override
        public void onMesg(RecordMesg record) {
            collectedDataPoints.add(toParsedDataPoint(record));
        }

        private ParsedDataPoint toParsedDataPoint(RecordMesg record) {
            return new ParsedDataPoint(
                    toInstant(record),
                    resolveSpeed(record),
                    record.getHeartRate() != null ? record.getHeartRate().intValue() : null,
                    record.getDistance() != null ? record.getDistance().doubleValue() : null,
                    record.getEnhancedAltitude() != null ? record.getEnhancedAltitude().doubleValue() : null
            );
        }

        /** Prefers the higher-precision "enhanced" speed field when the device provides it. */
        private Double resolveSpeed(RecordMesg record) {
            if (record.getEnhancedSpeed() != null) {
                return record.getEnhancedSpeed().doubleValue();
            }
            return record.getSpeed() != null ? record.getSpeed().doubleValue() : null;
        }

        private Instant toInstant(RecordMesg record) {
            return Instant.ofEpochMilli(record.getTimestamp().getDate().getTime());
        }

        List<ParsedDataPoint> getCollectedDataPoints() {
            return collectedDataPoints;
        }
    }
}
