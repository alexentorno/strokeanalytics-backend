package com.strokeanalytics.backend.stats;

/** Thrown when the requested time range contains no data points for the activity. */
public class EmptyDataRangeException extends RuntimeException {

    public EmptyDataRangeException(String message) {
        super(message);
    }
}
