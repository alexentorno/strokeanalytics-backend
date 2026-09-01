package com.strokeanalytics.backend.datapoint;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Downsamples a time series using the Largest-Triangle-Three-Buckets (LTTB)
 * algorithm (Steinarsson, 2013), so long activities render smoothly in the
 * frontend chart without losing visually significant peaks and valleys —
 * unlike naive "every Nth point" decimation, which can silently drop a
 * short sprint or a heart-rate spike if it happens to fall between samples.
 * <p>
 * LTTB operates on a single (x, y) series. Since a chart here shows several
 * metrics at once (speed, heart rate, distance) that must stay aligned on
 * the same timestamps, this class picks indices using one "shape" metric —
 * speed, or heart rate when a session has no speed data at all (e.g. an
 * indoor session) — and then applies that same set of indices to every
 * metric, so all lines keep sharing exactly the same x-axis points.
 */
@Component
public class DataPointDownsampler {

    /**
     * Returns at most {@code targetPointCount} points from {@code dataPoints},
     * chosen so the overall shape of the primary metric (see class docs) is
     * preserved as closely as possible. Returns the input unchanged if it
     * already has {@code targetPointCount} points or fewer.
     */
    public List<DataPoint> downsample(List<DataPoint> dataPoints, int targetPointCount) {
        if (targetPointCount <= 0) {
            throw new IllegalArgumentException("targetPointCount must be positive");
        }
        if (dataPoints.size() <= targetPointCount) {
            return dataPoints;
        }

        double[] xValues = extractEpochMillis(dataPoints);
        double[] yValues = extractShapeMetric(dataPoints);

        List<Integer> selectedIndices = selectIndicesByLargestTriangle(xValues, yValues, targetPointCount);

        return selectedIndices.stream().map(dataPoints::get).toList();
    }

    private double[] extractEpochMillis(List<DataPoint> dataPoints) {
        double[] values = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            values[i] = dataPoints.get(i).getTimestamp().toEpochMilli();
        }
        return values;
    }

    /** Speed drives the downsampling shape; falls back to heart rate for sessions with no speed data at all. */
    private double[] extractShapeMetric(List<DataPoint> dataPoints) {
        boolean hasAnySpeed = dataPoints.stream().anyMatch(point -> point.getSpeedMetersPerSecond() != null);

        double[] values = new double[dataPoints.size()];
        for (int i = 0; i < dataPoints.size(); i++) {
            DataPoint point = dataPoints.get(i);
            if (hasAnySpeed) {
                values[i] = point.getSpeedMetersPerSecond() != null ? point.getSpeedMetersPerSecond() : 0.0;
            } else {
                values[i] = point.getHeartRateBpm() != null ? point.getHeartRateBpm() : 0.0;
            }
        }
        return values;
    }

    /**
     * Core LTTB selection. Always keeps the first and last point, then picks
     * one point per bucket: whichever point forms the largest triangle with
     * the previously selected point and the average of the next bucket —
     * that triangle's area is a proxy for how visually significant the
     * point is.
     */
    private List<Integer> selectIndicesByLargestTriangle(double[] xValues, double[] yValues, int targetPointCount) {
        int sourceSize = xValues.length;

        if (targetPointCount < 3) {
            // LTTB always reserves the first and last point for itself; below
            // that there is nothing meaningful left for the algorithm to pick.
            return List.of(0, sourceSize - 1);
        }

        double bucketSize = (double) (sourceSize - 2) / (targetPointCount - 2);

        List<Integer> selectedIndices = new ArrayList<>(targetPointCount);
        selectedIndices.add(0);

        int previousSelectedIndex = 0;
        for (int bucketIndex = 0; bucketIndex < targetPointCount - 2; bucketIndex++) {
            int currentBucketStart = bucketRangeStart(bucketIndex, bucketSize);
            int currentBucketEnd = clampBucketEnd(bucketRangeStart(bucketIndex + 1, bucketSize), sourceSize, currentBucketStart);

            double[] nextBucketAverage = averageOfNextBucket(xValues, yValues, bucketIndex, bucketSize, sourceSize);

            int selected = indexOfLargestTriangle(
                    xValues, yValues,
                    currentBucketStart, currentBucketEnd,
                    xValues[previousSelectedIndex], yValues[previousSelectedIndex],
                    nextBucketAverage[0], nextBucketAverage[1]);

            selectedIndices.add(selected);
            previousSelectedIndex = selected;
        }

        selectedIndices.add(sourceSize - 1);
        return selectedIndices;
    }

    private int bucketRangeStart(int bucketIndex, double bucketSize) {
        return (int) Math.floor(bucketIndex * bucketSize) + 1;
    }

    /** Keeps a bucket's end index within bounds and strictly after its start, even for the final, possibly short bucket. */
    private int clampBucketEnd(int candidateEnd, int sourceSize, int bucketStart) {
        int clamped = Math.min(candidateEnd, sourceSize - 1);
        return Math.max(clamped, bucketStart + 1);
    }

    private double[] averageOfNextBucket(double[] xValues, double[] yValues, int bucketIndex, double bucketSize, int sourceSize) {
        int fromInclusive = Math.min(bucketRangeStart(bucketIndex + 1, bucketSize), sourceSize - 1);
        int toExclusive = Math.max(Math.min(bucketRangeStart(bucketIndex + 2, bucketSize), sourceSize), fromInclusive + 1);

        double sumX = 0;
        double sumY = 0;
        for (int i = fromInclusive; i < toExclusive; i++) {
            sumX += xValues[i];
            sumY += yValues[i];
        }
        int count = toExclusive - fromInclusive;
        return new double[] { sumX / count, sumY / count };
    }

    private int indexOfLargestTriangle(double[] xValues, double[] yValues,
                                        int fromInclusive, int toExclusive,
                                        double anchorX, double anchorY,
                                        double nextBucketAverageX, double nextBucketAverageY) {
        int bestIndex = fromInclusive;
        double bestArea = -1;

        for (int i = fromInclusive; i < toExclusive; i++) {
            double area = triangleArea(anchorX, anchorY, xValues[i], yValues[i], nextBucketAverageX, nextBucketAverageY);
            if (area > bestArea) {
                bestArea = area;
                bestIndex = i;
            }
        }
        return bestIndex;
    }

    /** Area of the triangle formed by three (x, y) points, via the shoelace formula. */
    private double triangleArea(double ax, double ay, double bx, double by, double cx, double cy) {
        return Math.abs((ax - cx) * (by - ay) - (ax - bx) * (cy - ay)) * 0.5;
    }
}
