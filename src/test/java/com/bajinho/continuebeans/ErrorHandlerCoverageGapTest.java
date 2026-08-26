package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.http.HttpTimeoutException;
import org.junit.jupiter.api.Test;

class ErrorHandlerCoverageGapTest {

    @Test
    void formatsAllDocumentedHttpStatuses() {
        int[] statuses = {400, 401, 403, 404, 408, 429, 500, 502, 503, 504};
        for (int status : statuses) {
            String message = ErrorHandler.formatErrorMessage(status, "http://test.local/api");
            assertNotNull(message);
            assertTrue(message.contains(String.valueOf(status)));
        }
    }

    @Test
    void formatsUnknownStatusWithUrl() {
        String message = ErrorHandler.formatErrorMessage(418, "http://test.local/api");
        assertEquals("Erro HTTP 418 em http://test.local/api", message);
    }

    @Test
    void formatsNetworkMessagesWithAndWithoutCause() {
        assertEquals("Erro de conexão: Connection refused", ErrorHandler.formatNetworkError("Connection refused"));
        assertTrue(ErrorHandler.formatNetworkError(null).contains("LM Studio"));
        assertTrue(ErrorHandler.formatTimeoutMessage().contains("Timeout"));
    }

    @Test
    void recognizesRetryableExceptionFamilies() {
        assertTrue(ErrorHandler.isRetryable(0, new HttpTimeoutException("timeout")));
        assertTrue(ErrorHandler.isRetryable(0, new SocketTimeoutException("timeout")));
        assertTrue(ErrorHandler.isRetryable(0, new IOException("Connection reset by peer")));
        assertFalse(ErrorHandler.isRetryable(0, new IOException("permission denied")));
        assertFalse(ErrorHandler.isRetryable(0, new SocketException("host unreachable")));
    }

    @Test
    void retryBoundariesAreStable() {
        assertEquals(1000, ErrorHandler.getRetryDelay(1));
        assertEquals(2000, ErrorHandler.getRetryDelay(2));
        assertEquals(4000, ErrorHandler.getRetryDelay(3));
        assertFalse(ErrorHandler.hasExhaustedRetries(0));
        assertFalse(ErrorHandler.hasExhaustedRetries(2));
        assertTrue(ErrorHandler.hasExhaustedRetries(3));
    }
}
