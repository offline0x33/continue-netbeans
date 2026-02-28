package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended tests for ErrorHandler to increase code coverage.
 */
class ErrorHandlerExtendedTest {

    @Test
    void testIsRetryableFor429() {
        assertTrue(ErrorHandler.isRetryable(429, null), "429 should be retryable");
    }

    @Test
    void testIsRetryableFor500() {
        assertTrue(ErrorHandler.isRetryable(500, null), "500 should be retryable");
    }

    @Test
    void testIsRetryableFor502() {
        assertTrue(ErrorHandler.isRetryable(502, null), "502 should be retryable");
    }

    @Test
    void testIsRetryableFor503() {
        assertTrue(ErrorHandler.isRetryable(503, null), "503 should be retryable");
    }

    @Test
    void testIsRetryableFor504() {
        assertTrue(ErrorHandler.isRetryable(504, null), "504 should be retryable");
    }

    @Test
    void testIsNotRetryableFor400() {
        assertFalse(ErrorHandler.isRetryable(400, null), "400 should not be retryable");
    }

    @Test
    void testIsNotRetryableFor401() {
        assertFalse(ErrorHandler.isRetryable(401, null), "401 should not be retryable");
    }

    @Test
    void testIsNotRetryableFor403() {
        assertFalse(ErrorHandler.isRetryable(403, null), "403 should not be retryable");
    }

    @Test
    void testIsNotRetryableFor404() {
        assertFalse(ErrorHandler.isRetryable(404, null), "404 should not be retryable");
    }

    @Test
    void testIsRetryableWithTimeoutException() {
        java.net.http.HttpTimeoutException ex = new java.net.http.HttpTimeoutException("Timeout");
        assertTrue(ErrorHandler.isRetryable(0, ex), "Timeout exception should be retryable");
    }

    @Test
    void testIsRetryableWithIOException() {
        java.io.IOException ex = new java.io.IOException("Connection reset by peer");
        assertTrue(ErrorHandler.isRetryable(0, ex), "IOException with 'Connection reset' should be retryable");
    }

    @Test
    void testFormatErrorMessageFor400() {
        String msg = ErrorHandler.formatErrorMessage(400, "http://test.com");
        assertTrue(msg.contains("400") || msg.contains("mal formatada"),
                   "400 error message should be formatted");
    }

    @Test
    void testFormatErrorMessageFor500() {
        String msg = ErrorHandler.formatErrorMessage(500, "http://test.com");
        assertTrue(msg.contains("500") || msg.contains("Internal Server Error") || msg.contains("interno"),
                   "500 error message should be formatted");
    }

    @Test
    void testFormatErrorMessageFor429() {
        String msg = ErrorHandler.formatErrorMessage(429, "http://test.com");
        assertTrue(msg.contains("429") || msg.contains("Rate Limit") || msg.contains("limite de requisições"),
                   "429 error message should mention rate limit");
    }

    @Test
    void testFormatErrorMessageForUnknownStatus() {
        String msg = ErrorHandler.formatErrorMessage(999, "http://test.com");
        assertNotNull(msg, "Should handle unknown status codes");
        assertFalse(msg.isEmpty(), "Error message should not be empty");
    }

    @Test
    void testGetRetryDelayFirstAttempt() {
        long delay = ErrorHandler.getRetryDelay(1);
        assertTrue(delay > 0, "First retry should have positive delay");
        assertEquals(1000, delay, "First retry delay should be 1000ms");
    }

    @Test
    void testGetRetryDelayIncreasing() {
        long delay1 = ErrorHandler.getRetryDelay(1);
        long delay2 = ErrorHandler.getRetryDelay(2);
        assertTrue(delay2 >= delay1, "Retry delay should increase with attempts");
    }

    @Test
    void testGetRetryDelayExponential() {
        long delay1 = ErrorHandler.getRetryDelay(1);
        long delay2 = ErrorHandler.getRetryDelay(2);
        long delay3 = ErrorHandler.getRetryDelay(3);
        
        // Should double each time (exponential backoff)
        assertEquals(1000, delay1, "First delay should be 1000ms");
        assertEquals(2000, delay2, "Second delay should be 2000ms");
        assertEquals(4000, delay3, "Third delay should be 4000ms");
    }

    @Test
    void testFormatTimeoutMessage() {
        String msg = ErrorHandler.formatTimeoutMessage();
        assertNotNull(msg, "Timeout message should not be null");
        assertFalse(msg.isEmpty(), "Timeout message should not be empty");
        assertTrue(msg.toLowerCase().contains("timeout") || msg.contains("tempo"),
                   "Message should indicate timeout");
    }

    @Test
    void testFormatErrorMessageWithNullUrl() {
        String msg = ErrorHandler.formatErrorMessage(500, null);
        assertNotNull(msg, "Should handle null URL");
        assertFalse(msg.isEmpty(), "Error message should not be empty");
    }

    @Test
    void testIsRetryableWith200Status() {
        assertFalse(ErrorHandler.isRetryable(200, null), "200 success should not be retryable");
    }

    @Test
    void testIsRetryableWith201Status() {
        assertFalse(ErrorHandler.isRetryable(201, null), "201 success should not be retryable");
    }
}
