package com.bajinho.continuebeans.ai;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class RealCodeToolsTest {
    @TempDir
    Path workspace;

    @AfterEach
    void clearWorkspaceProperty() {
        System.clearProperty("continuebeans.workspace");
    }

    @Test
    void generatedTestContainsRealAssertion() throws Exception {
        System.setProperty("continuebeans.workspace", workspace.toString());
        Files.createDirectories(workspace.resolve("src/test/java"));

        NetBeansFunctionExecutor.FunctionResult result = RealCodeTools.execute(
                "generate_test_method",
                Map.of("className", "Calculator", "testMethods", List.of("assertEquals(2, 1 + 1)")));

        assertTrue(result.isSuccess());
        String content = Files.readString(
                workspace.resolve("src/test/java/CalculatorTest.java"), StandardCharsets.UTF_8);
        assertTrue(content.contains("assertEquals(2, 1 + 1);"));
        assertTrue(!content.contains("TODO"));
    }

    @Test
    void emptyTestMethodsAreRejected() throws Exception {
        System.setProperty("continuebeans.workspace", workspace.toString());
        Files.createDirectories(workspace.resolve("src/test/java"));

        NetBeansFunctionExecutor.FunctionResult result = RealCodeTools.execute(
                "generate_test_method",
                Map.of("className", "Calculator", "testMethods", List.of()));

        assertTrue(!result.isSuccess());
        assertTrue(result.getMessage().contains("asserção Java"));
    }

    @Test
    void nonAssertionTestMethodIsRejected() throws Exception {
        System.setProperty("continuebeans.workspace", workspace.toString());
        Files.createDirectories(workspace.resolve("src/test/java"));

        NetBeansFunctionExecutor.FunctionResult result = RealCodeTools.execute(
                "generate_test_method",
                Map.of("className", "Calculator", "testMethods", List.of("shouldAdd")));

        assertTrue(!result.isSuccess());
        assertTrue(result.getMessage().contains("asserções JUnit"));
    }
}
