package com.strokeanalytics.backend.web;

/** Uniform error body returned by every handled exception. */
public record ErrorResponse(String message) {
}
