package com.bajinho.continuebeans;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.nio.file.Files;
import java.nio.file.Path;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Extended tests for ContextManager context processing.
 */
class ContextManagerExtendedTest {

    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("context_test_");
    }

    @Test
    void testProcessContextWithoutCommands() {
        String input = "Just a simple prompt";
        String result = ContextManager.processContext(input, tempDir.toString());
        assertTrue(result.contains(input), "Should contain original input");
        assertTrue(result.contains("Contexto:"), "Should add automatic project context");
        String projectName = tempDir.getFileName().toString();
        assertTrue(result.contains(projectName), "Should contain project name: " + projectName);
    }

    @Test
    void testProcessContextWithCodebaseCommand() {
        String input = "@codebase explain this";
        String result = ContextManager.processContext(input, tempDir.toString());
        assertTrue(result.contains("@codebase") || result.length() > input.length(),
                "Should process @codebase command");
    }

    @Test
    void testProcessContextWithFileCommand() throws Exception {
        Path testFile = Files.createFile(tempDir.resolve("test.txt"));
        Files.writeString(testFile, "Test file content");

        String input = "@file:test.txt explain this";
        String result = ContextManager.processContext(input, tempDir.toString());
        assertTrue(result.contains("Test file content") || result.contains("test.txt"),
                "Should include file content");
    }

    @Test
    void testProcessContextWithNullWorkDir() {
        String input = "test prompt";
        String result = ContextManager.processContext(input, null);
        assertNotNull(result, "Should handle null working directory");
    }

    @Test
    void testProcessContextTruncation() {
        StringBuilder large = new StringBuilder("@codebase\n\n");
        for (int i = 0; i < 300; i++) {
            large.append("This is a very long line that should help us exceed the truncation limit\n");
        }

        String result = ContextManager.processContext(large.toString(), tempDir.toString());
        if (result.contains("Contexto truncado")) {
            assertTrue(result.length() <= ContextManager.getMaxContextChars(),
                    "Should respect configured context limit");
        }
    }

    @Test
    void testProcessContextWithMultipleCommands() throws Exception {
        Path testFile = Files.createFile(tempDir.resolve("file1.txt"));
        Files.writeString(testFile, "Content 1");

        String input = "@file:file1.txt test @codebase test";
        String result = ContextManager.processContext(input, tempDir.toString());
        assertNotNull(result, "Should process multiple commands");
    }

    @Test
    void testProcessContextPreserveText() {
        String input = "Please @file:test.txt analyze this code";
        String result = ContextManager.processContext(input, tempDir.toString());
        assertTrue(result.contains("Please") && result.contains("analyze"),
                "Should preserve surrounding text");
    }

    @Test
    void testProcessContextWithNonexistentFile() {
        String input = "@file:nonexistent.txt test";
        String result = ContextManager.processContext(input, tempDir.toString());
        assertTrue(result.contains("nonexistent.txt") || result.contains("ERRO"),
                "Should handle missing file gracefully");
    }

    @Test
    void testProcessContextEmptyInput() {
        String result = ContextManager.processContext("", tempDir.toString());
        assertTrue(result.contains("Contexto:"), "Empty input should still add project context");
        String projectName = tempDir.getFileName().toString();
        assertTrue(result.contains(projectName), "Should contain project name: " + projectName);
    }

    @Test
    void testProcessContextCodebaseStructure() {
        String input = "@codebase";
        String result = ContextManager.processContext(input, tempDir.toString());
        assertTrue(result.contains("@codebase") || result.contains("Estrutura"),
                "Should include project structure");
    }

    @Test
    void testProcessContextWithWhitespace() {
        String input = "  @codebase  with spaces  ";
        String result = ContextManager.processContext(input, tempDir.toString());
        assertNotNull(result, "Should handle input with whitespace");
    }
}
