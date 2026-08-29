package com.strokeanalytics.backend.stats;

/** Thrown when a requested time range is malformed, e.g. {@code from} is not before {@code to}. */
public class InvalidTimeRangeException extends RuntimeException {

    public InvalidTimeRangeException(String message) {
        super(message);
    }
}
