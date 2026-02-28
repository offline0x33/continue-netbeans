package com.bajinho.continuebeans;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Additional tests for ErrorHandler to increase code coverage.
 */
class ErrorHandlerAdditionalTest {

    @Test
    void testIsRetryableWith429StatusCode() {
        assertTrue(ErrorHandler.isRetryable(429, null), "429 should be retryable");
    }

    @Test
    void testIsRetryableWith500StatusCode() {
        assertTrue(ErrorHandler.isRetryable(500, null), "500 should be retryable");
    }

    @Test
    void testIsRetryableWith502StatusCode() {
        assertTrue(ErrorHandler.isRetryable(502, null), "502 should be retryable");
    }

    @Test
    void testIsRetryableWith503StatusCode() {
        assertTrue(ErrorHandler.isRetryable(503, null), "503 should be retryable");
    }

    @Test
    void testIsRetryableWith504StatusCode() {
        assertTrue(ErrorHandler.isRetryable(504, null), "504 should be retryable");
    }

    @Test
    void testIsRetryableWithHttpTimeoutException() {
        HttpTimeoutException timeoutEx = new HttpTimeoutException("timeout");
        assertTrue(ErrorHandler.isRetryable(200, timeoutEx), "HttpTimeoutException should be retryable");
    }

    @Test
    void testIsRetryableWithSocketTimeoutException() {
        SocketTimeoutException socketEx = new SocketTimeoutException("socket timeout");
        assertTrue(ErrorHandler.isRetryable(200, socketEx), "SocketTimeoutException should be retryable");
    }

    @Test
    void testIsRetryableWithConnectionResetIOException() {
        IOException connectionEx = new IOException("Connection reset");
        assertTrue(ErrorHandler.isRetryable(200, connectionEx), "Connection reset should be retryable");
    }

    @Test
    void testIsRetryableWithOtherIOException() {
        IOException ioEx = new IOException("Other IO error");
        assertFalse(ErrorHandler.isRetryable(200, ioEx), "Other IOExceptions should not be retryable");
    }

    @Test
    void testIsRetryableWith400StatusCode() {
        assertFalse(ErrorHandler.isRetryable(400, null), "400 should not be retryable");
    }

    @Test
    void testIsRetryableWith401StatusCode() {
        assertFalse(ErrorHandler.isRetryable(401, null), "401 should not be retryable");
    }

    @Test
    void testIsRetryableWith403StatusCode() {
        assertFalse(ErrorHandler.isRetryable(403, null), "403 should not be retryable");
    }

    @Test
    void testIsRetryableWith404StatusCode() {
        assertFalse(ErrorHandler.isRetryable(404, null), "404 should not be retryable");
    }

    @Test
    void testFormatErrorMessageFor400() {
        String msg = ErrorHandler.formatErrorMessage(400, "http://test.com");
        assertTrue(msg.contains("400"), "Should include status code");
        assertTrue(msg.contains("mal formatada"), "Should indicate bad request");
    }

    @Test
    void testFormatErrorMessageFor401() {
        String msg = ErrorHandler.formatErrorMessage(401, "http://test.com");
        assertTrue(msg.contains("401"), "Should include status code");
        assertTrue(msg.contains("Autenticação"), "Should mention authentication");
    }

    @Test
    void testFormatErrorMessageFor403() {
        String msg = ErrorHandler.formatErrorMessage(403, "http://test.com");
        assertTrue(msg.contains("403"), "Should include status code");
        assertTrue(msg.contains("Acesso negado"), "Should mention access denied");
    }

    @Test
    void testFormatErrorMessageFor404() {
        String msg = ErrorHandler.formatErrorMessage(404, "http://test.com");
        assertTrue(msg.contains("404"), "Should include status code");
        assertTrue(msg.contains("http://test.com"), "Should include URL");
    }

    @Test
    void testFormatErrorMessageFor429() {
        String msg = ErrorHandler.formatErrorMessage(429, "http://test.com");
        assertTrue(msg.contains("429"), "Should include status code");
        assertTrue(msg.contains("Limite"), "Should mention rate limit");
    }

    @Test
    void testFormatErrorMessageFor500() {
        String msg = ErrorHandler.formatErrorMessage(500, "http://test.com");
        assertTrue(msg.contains("500"), "Should include status code");
        assertTrue(msg.contains("servidor"), "Should mention server");
    }

    @Test
    void testFormatErrorMessageFor502() {
        String msg = ErrorHandler.formatErrorMessage(502, "http://test.com");
        assertTrue(msg.contains("502"), "Should include status code");
        assertTrue(msg.contains("Gateway"), "Should mention gateway");
    }

    @Test
    void testFormatErrorMessageFor503() {
        String msg = ErrorHandler.formatErrorMessage(503, "http://test.com");
        assertTrue(msg.contains("503"), "Should include status code");
        assertTrue(msg.contains("indisponível"), "Should mention unavailable");
    }

    @Test
    void testFormatErrorMessageFor504() {
        String msg = ErrorHandler.formatErrorMessage(504, "http://test.com");
        assertTrue(msg.contains("504"), "Should include status code");
    }

    @Test
    void testFormatErrorMessageForUnknownStatus() {
        String msg = ErrorHandler.formatErrorMessage(418, "http://test.com");
        assertTrue(msg.contains("418"), "Should include status code");
        assertTrue(msg.contains("http://test.com"), "Should include URL");
    }

    @Test
    void testFormatTimeoutMessage() {
        String msg = ErrorHandler.formatTimeoutMessage();
        assertTrue(msg.contains("Timeout"), "Should contain Timeout");
        assertTrue(msg.contains("LM Studio"), "Should mention LM Studio");
    }

    @Test
    void testFormatNetworkErrorWithMessage() {
        String msg = ErrorHandler.formatNetworkError("Network unreachable");
        assertTrue(msg.contains("Erro de conexão"), "Should indicate connection error");
        assertTrue(msg.contains("Network unreachable"), "Should include the error message");
    }

    @Test
    void testFormatNetworkErrorWithNullMessage() {
        String msg = ErrorHandler.formatNetworkError(null);
        assertTrue(msg.contains("Erro de conexão"), "Should indicate connection error");
        assertTrue(msg.contains("conectar"), "Should indicate connection");
    }

    @Test
    void testGetRetryDelayFirstAttempt() {
        long delay = ErrorHandler.getRetryDelay(1);
        assertEquals(1000L, delay, "First retry should be 1000ms");
    }

    @Test
    void testGetRetryDelaySecondAttempt() {
        long delay = ErrorHandler.getRetryDelay(2);
        assertEquals(2000L, delay, "Second retry should be 2000ms (exponential backoff)");
    }

    @Test
    void testGetRetryDelayThirdAttempt() {
        long delay = ErrorHandler.getRetryDelay(3);
        assertEquals(4000L, delay, "Third retry should be 4000ms (exponential backoff)");
    }

    @Test
    void testGetRetryDelayFourthAttempt() {
        long delay = ErrorHandler.getRetryDelay(4);
        assertEquals(8000L, delay, "Fourth retry should be 8000ms");
    }

    @Test
    void testShouldRetryWithinMaxRetries() {
        assertFalse(ErrorHandler.hasExhaustedRetries(1), "Should not be exhausted at attempt 1");
        assertFalse(ErrorHandler.hasExhaustedRetries(2), "Should not be exhausted at attempt 2");
        assertTrue(ErrorHandler.hasExhaustedRetries(3), "Should be exhausted at attempt 3 (MAX_RETRIES=2)");
        assertTrue(ErrorHandler.hasExhaustedRetries(4), "Should be exhausted at attempt 4");
    }

    @Test
    void testHasExhaustedRetriesWithZeroAttempts() {
        assertFalse(ErrorHandler.hasExhaustedRetries(0), "Should not be exhausted at zero attempts");
    }

    @Test
    void testHasExhaustedRetriesWithNegativeAttempts() {
        assertFalse(ErrorHandler.hasExhaustedRetries(-1), "Should not be exhausted for negative attempts");
    }
}
