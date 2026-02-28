package com.bajinho.continuebeans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ContinueLogger utility class.
 */
class ContinueLoggerTest {

    @BeforeEach
    void setUp() {
        // ContinueLogger is a static utility class
    }

    @Test
    void testInfoLogging() {
        // Should not throw exception
        ContinueLogger.info("Test info message");
        assertTrue(true, "info() should execute without exception");
    }

    @Test
    void testWarnLogging() {
        // Should not throw exception
        ContinueLogger.warn("Test warning message", null);
        assertTrue(true, "warn() should execute without exception");
    }

    @Test
    void testWarnLoggingWithException() {
        Exception ex = new RuntimeException("Test exception");
        ContinueLogger.warn("Warning with exception", ex);
        assertTrue(true, "warn() with exception should execute without exception");
    }

    @Test
    void testErrorLogging() {
        // Should not throw exception
        ContinueLogger.error("Test error message", null);
        assertTrue(true, "error() should execute without exception");
    }

    @Test
    void testErrorLoggingWithException() {
        Exception ex = new RuntimeException("Test error exception");
        ContinueLogger.error("Error with exception", ex);
        assertTrue(true, "error() with exception should execute without exception");
    }

    @Test
    void testInfoWithEmptyMessage() {
        ContinueLogger.info("");
        assertTrue(true, "info() with empty message should work");
    }

    @Test
    void testWarnWithNullMessage() {
        // Should handle null gracefully
        assertDoesNotThrow(() -> ContinueLogger.warn(null, null));
    }

    @Test
    void testErrorWithNullMessage() {
        // Should handle null gracefully
        assertDoesNotThrow(() -> ContinueLogger.error(null, null));
    }

    @Test
    void testMultipleLogsInSequence() {
        ContinueLogger.info("First");
        ContinueLogger.warn("Second", null);
        ContinueLogger.error("Third", new Exception());
        assertTrue(true, "Multiple logs should work");
    }
}
