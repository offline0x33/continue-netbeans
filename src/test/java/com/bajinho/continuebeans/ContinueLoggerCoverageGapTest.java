package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;

class ContinueLoggerCoverageGapTest {

    @Test
    void loggingMethodsAcceptNormalMessages() {
        assertDoesNotThrow(() -> ContinueLogger.info("info"));
        assertDoesNotThrow(() -> ContinueLogger.warn("warn", null));
        assertDoesNotThrow(() -> ContinueLogger.error("error", null));
    }

    @Test
    void loggingMethodsAcceptNullMessagesAndExceptions() {
        RuntimeException exception = new RuntimeException("test");
        assertDoesNotThrow(() -> ContinueLogger.info(null));
        assertDoesNotThrow(() -> ContinueLogger.warn(null, exception));
        assertDoesNotThrow(() -> ContinueLogger.error(null, exception));
    }
}
