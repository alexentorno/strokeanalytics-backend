package com.strokeanalytics.backend.fit;

/** Thrown when a .fit file is missing, corrupted, or not a recognized activity file. */
public class FitParsingException extends RuntimeException {

    public FitParsingException(String message, Throwable cause) {
        super(message, cause);
    }
}
