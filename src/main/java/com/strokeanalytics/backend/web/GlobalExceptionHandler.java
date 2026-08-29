package com.strokeanalytics.backend.web;

import com.strokeanalytics.backend.fit.FitParsingException;
import com.strokeanalytics.backend.stats.EmptyDataRangeException;
import com.strokeanalytics.backend.stats.InvalidTimeRangeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Central place that converts known domain exceptions into a proper HTTP
 * status and a readable JSON message, instead of a generic 500 with no
 * details.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FitParsingException.class)
    public ResponseEntity<ErrorResponse> handleFitParsingException(FitParsingException exception) {
        return badRequest(exception.getMessage());
    }

    @ExceptionHandler(InvalidTimeRangeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTimeRange(InvalidTimeRangeException exception) {
        return badRequest(exception.getMessage());
    }

    @ExceptionHandler(EmptyDataRangeException.class)
    public ResponseEntity<ErrorResponse> handleEmptyDataRange(EmptyDataRangeException exception) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(exception.getMessage()));
    }

    private ResponseEntity<ErrorResponse> badRequest(String message) {
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
    }
}
