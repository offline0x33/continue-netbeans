package com.bajinho.continuebeans.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ToolExecutionPolicyTest {

    private static final String WORKSPACE_PROPERTY = "continuebeans.workspace";
    private static final String ALLOW_BUILD_PROPERTY = "continuebeans.allowBuild";

    @AfterEach
    void clearProperties() {
        System.clearProperty(WORKSPACE_PROPERTY);
        System.clearProperty(ALLOW_BUILD_PROPERTY);
    }

    @Test
    void acceptsPathInsideWorkspace() {
        Path workspace = Path.of(System.getProperty("java.io.tmpdir"), "continuebeans-test");
        System.setProperty(WORKSPACE_PROPERTY, workspace.toString());

        assertDoesNotThrow(() -> ToolExecutionPolicy.requireWorkspacePath("src/Main.java"));
    }

    @Test
    void rejectsPathOutsideWorkspace() {
        Path workspace = Path.of(System.getProperty("java.io.tmpdir"), "continuebeans-test");
        System.setProperty(WORKSPACE_PROPERTY, workspace.toString());

        assertThrows(SecurityException.class,
                () -> ToolExecutionPolicy.requireWorkspacePath("../secret.txt"));
    }

    @Test
    void requiresDeleteConfirmation() {
        Path workspace = Path.of(System.getProperty("java.io.tmpdir"), "continuebeans-test");
        System.setProperty(WORKSPACE_PROPERTY, workspace.toString());

        assertThrows(SecurityException.class,
                () -> ToolExecutionPolicy.validate("delete_file", Map.of("filePath", "file.txt")));
    }

    @Test
    void requiresLocalBuildOptIn() {
        Path workspace = Path.of(System.getProperty("java.io.tmpdir"), "continuebeans-test");
        System.setProperty(WORKSPACE_PROPERTY, workspace.toString());

        assertThrows(SecurityException.class,
                () -> ToolExecutionPolicy.validate("build_project", Map.of("projectPath", ".")));

        System.setProperty(ALLOW_BUILD_PROPERTY, "true");
        assertDoesNotThrow(() -> ToolExecutionPolicy.validate("build_project", Map.of("projectPath", ".")));
    }
}
