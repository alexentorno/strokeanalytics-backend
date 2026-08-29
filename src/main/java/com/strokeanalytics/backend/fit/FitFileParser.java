package com.strokeanalytics.backend.fit;

import java.io.InputStream;

/**
 * Decodes a Garmin .fit binary file into an in-memory representation.
 * <p>
 * Implementations must not persist anything — parsing and persistence are
 * kept as separate concerns, so the parser can be unit-tested without a
 * database and swapped out independently (e.g. a mock parser for tests).
 */
public interface FitFileParser {

    /**
     * Parses the given .fit file stream.
     *
     * @param fitFileStream raw bytes of a .fit file; not closed by this method
     * @return the decoded activity with its full time-series
     * @throws FitParsingException if the file is not a valid .fit activity file
     */
    ParsedActivity parse(InputStream fitFileStream);
}
