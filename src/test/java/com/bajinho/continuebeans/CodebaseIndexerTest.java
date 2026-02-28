package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CodebaseIndexerTest {

    @Test
    public void testScanBasicStructure() throws IOException {
        Path tmpDir = Files.createTempDirectory("codebase_test");
        Files.createDirectory(tmpDir.resolve("src"));
        Files.writeString(tmpDir.resolve("README.md"), "# Test");
        Files.writeString(tmpDir.resolve("src").resolve("main.java"), "public class Main {}");

        CodebaseIndexer indexer = new CodebaseIndexer(tmpDir.toString());
        String result = indexer.scanDirectory(tmpDir.toString());

        assertTrue(result.contains("README.md"));
        assertTrue(result.contains("src"));
        assertTrue(result.contains("main.java"));
    }

    @Test
    public void testExcludesDefaultDirectories() throws IOException {
        Path tmpDir = Files.createTempDirectory("codebase_exclude_test");
        Files.createDirectory(tmpDir.resolve(".git"));
        Files.createDirectory(tmpDir.resolve("node_modules"));
        Files.createDirectory(tmpDir.resolve("target"));
        Files.writeString(tmpDir.resolve("src.java"), "public class Src {}");

        CodebaseIndexer indexer = new CodebaseIndexer(tmpDir.toString());
        String result = indexer.scanDirectory(tmpDir.toString());

        assertFalse(result.contains(".git"));
        assertFalse(result.contains("node_modules"));
        assertFalse(result.contains("target"));
        assertTrue(result.contains("src.java"));
    }

    @Test
    public void testRespectDepthLimit() throws IOException {
        Path tmpDir = Files.createTempDirectory("codebase_depth_test");
        Path currentDir = tmpDir;

        // Create nested structure
        for (int i = 0; i < 8; i++) {
            currentDir = currentDir.resolve("level_" + i);
            Files.createDirectory(currentDir);
            Files.writeString(currentDir.resolve("file_" + i + ".txt"), "content");
        }

        CodebaseIndexer indexer = new CodebaseIndexer(tmpDir.toString());
        indexer.setMaxDepth(3);
        String result = indexer.scanDirectory(tmpDir.toString());

        // Should include level_0 to level_3
        assertTrue(result.contains("level_0"));
        assertTrue(result.contains("level_3"));

        // Should not include level_5+
        assertFalse(result.contains("level_6"));
    }

    @Test
    public void testRespectMaxFileCount() throws IOException {
        Path tmpDir = Files.createTempDirectory("codebase_maxfiles_test");

        // Create many files
        for (int i = 0; i < 50; i++) {
            Files.writeString(tmpDir.resolve("file_" + i + ".txt"), "content");
        }

        CodebaseIndexer indexer = new CodebaseIndexer(tmpDir.toString());
        indexer.setMaxFiles(10);
        String result = indexer.scanDirectory(tmpDir.toString());

        // Should truncate after 10 files
        assertTrue(result.contains("Limite de arquivos"));
    }

    @Test
    public void testRespectGitignorePatterns() throws IOException {
        Path tmpDir = Files.createTempDirectory("codebase_gitignore_test");

        // Create .gitignore
        Files.writeString(tmpDir.resolve(".gitignore"), "*.log\n*.tmp\nbuild/");

        // Create files
        Files.writeString(tmpDir.resolve("README.md"), "# Test");
        Files.writeString(tmpDir.resolve("debug.log"), "logs");
        Files.writeString(tmpDir.resolve("cache.tmp"), "temp");
        Files.createDirectory(tmpDir.resolve("build"));
        Files.writeString(tmpDir.resolve("build").resolve("output.jar"), "jar");

        CodebaseIndexer indexer = new CodebaseIndexer(tmpDir.toString());
        String result = indexer.scanDirectory(tmpDir.toString());

        assertTrue(result.contains("README.md"));
        assertFalse(result.contains("debug.log"));
        assertFalse(result.contains("cache.tmp"));
        assertFalse(result.contains("build"));
    }

    @Test
    public void testFileCountTracker() throws IOException {
        Path tmpDir = Files.createTempDirectory("codebase_filecount_test");
        Files.writeString(tmpDir.resolve("file1.txt"), "1");
        Files.writeString(tmpDir.resolve("file2.txt"), "2");
        Files.writeString(tmpDir.resolve("file3.txt"), "3");

        CodebaseIndexer indexer = new CodebaseIndexer(tmpDir.toString());
        indexer.scanDirectory(tmpDir.toString());

        assertEquals(3, indexer.getFileCount());
    }
}
