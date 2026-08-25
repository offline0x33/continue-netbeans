package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ContextManagerTest {

    @Test
    void shouldPreserveBeginningAndEndWhenContextExceedsBudget() throws Exception {
        Path tempDir = Files.createTempDirectory("continue-context-");
        Path file = tempDir.resolve("large.txt");
        Files.writeString(file, "A".repeat(9000) + "END-MARKER");

        String result = ContextManager.processContext("@file:" + file, tempDir.toString());

        assertTrue(result.startsWith("@file:" + file));
        assertTrue(result.contains("END-MARKER"));
        assertTrue(result.contains("[Contexto Truncado para"));
    }

    @Test
    void shouldAllowConfigurableContextBudget() throws Exception {
        String previous = System.getProperty("continue.context.maxChars");
        try {
            System.setProperty("continue.context.maxChars", "2048");
            Path tempDir = Files.createTempDirectory("continue-context-config-");
            Path file = tempDir.resolve("file.txt");
            Files.writeString(file, "X".repeat(5000) + "END");

            String result = ContextManager.processContext("@file:" + file, tempDir.toString());

            assertTrue(result.length() <= 2176);
            assertTrue(result.contains("END"));
        } finally {
            if (previous == null) {
                System.clearProperty("continue.context.maxChars");
            } else {
                System.setProperty("continue.context.maxChars", previous);
            }
        }
    }
}
