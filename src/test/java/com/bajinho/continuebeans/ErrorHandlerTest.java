package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ErrorHandlerTest {

    @Test
    public void testIsRetryableFor429() {
        assertTrue(ErrorHandler.isRetryable(429, null));
    }

    @Test
    public void testIsRetryableFor5xx() {
        assertTrue(ErrorHandler.isRetryable(500, null));
        assertTrue(ErrorHandler.isRetryable(502, null));
        assertTrue(ErrorHandler.isRetryable(503, null));
    }

    @Test
    public void testIsRetryableForTimeout() {
        assertTrue(ErrorHandler.isRetryable(0, new java.net.http.HttpTimeoutException("timeout")));
    }

    @Test
    public void testIsNotRetryableFor4xx() {
        assertFalse(ErrorHandler.isRetryable(400, null));
        assertFalse(ErrorHandler.isRetryable(401, null));
        assertFalse(ErrorHandler.isRetryable(403, null));
    }

    @Test
    public void testFormatErrorMessage404() {
        String msg = ErrorHandler.formatErrorMessage(404, "http://localhost:1234/v1/chat");
        assertTrue(msg.contains("404"));
        assertTrue(msg.contains("localhost:1234"));
    }

    @Test
    public void testFormatErrorMessage500() {
        String msg = ErrorHandler.formatErrorMessage(500, "http://lmstudio.local/v1");
        assertTrue(msg.contains("500"));
        assertTrue(msg.contains("servidor"));
    }

    @Test
    public void testFormatTimeoutMessage() {
        String msg = ErrorHandler.formatTimeoutMessage();
        assertTrue(msg.contains("Timeout"));
        assertTrue(msg.toLowerCase().contains("servidor"));
    }

    @Test
    public void testExponentialBackoff() {
        long delay1 = ErrorHandler.getRetryDelay(1); // 1s
        long delay2 = ErrorHandler.getRetryDelay(2); // 2s
        long delay3 = ErrorHandler.getRetryDelay(3); // 4s

        assertEquals(1000, delay1);
        assertEquals(2000, delay2);
        assertEquals(4000, delay3);
    }

    @Test
    public void testHasExhaustedRetries() {
        assertFalse(ErrorHandler.hasExhaustedRetries(1));
        assertFalse(ErrorHandler.hasExhaustedRetries(2));
        assertTrue(ErrorHandler.hasExhaustedRetries(3)); // MAX_RETRIES = 2
    }
}
