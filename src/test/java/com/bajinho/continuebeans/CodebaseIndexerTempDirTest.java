package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class CodebaseIndexerTempDirTest {

    @Test
    void indexesJavaAndMarkdownFilesFromTemporaryDirectory() throws IOException {
        Path root = Files.createTempDirectory("continue-indexer-");
        Path src = Files.createDirectories(root.resolve("src"));
        Files.writeString(src.resolve("Main.java"), "class Main {}\n");
        Files.writeString(root.resolve("README.md"), "# test\n");
        String result = new CodebaseIndexer(root.toString()).scanDirectory(root.toString());
        assertTrue(result.contains("Main.java"));
        assertTrue(result.contains("README.md"));
        assertTrue(new CodebaseIndexer(root.toString()).getFileCount() >= 0);
    }

    @Test
    void respectsGitignoreDefaultExcludesDepthAndFileLimit() throws IOException {
        Path root = Files.createTempDirectory("continue-indexer-");
        Files.writeString(root.resolve(".gitignore"), "ignored.txt\n");
        Files.writeString(root.resolve("ignored.txt"), "ignored\n");
        Files.writeString(root.resolve("kept.txt"), "kept\n");
        Path nested = Files.createDirectories(root.resolve("a/b/c/d"));
        Files.writeString(nested.resolve("deep.java"), "class Deep {}\n");
        Files.createDirectory(root.resolve("target"));
        Files.writeString(root.resolve("target/build.txt"), "no\n");

        CodebaseIndexer indexer = new CodebaseIndexer(root.toString());
        indexer.setMaxDepth(2);
        indexer.setMaxFiles(2);
        String result = indexer.scanDirectory(root.toString());
        assertFalse(result.contains("ignored.txt"));
        assertFalse(result.contains("build.txt"));
        assertFalse(result.contains("deep.java"));
        assertTrue(indexer.getFileCount() <= 2);
    }

    @Test
    void handlesMissingRootAndNullGitignoreSafely() {
        assertTrue(new CodebaseIndexer(null).scanDirectory("/path/that/does/not/exist").isEmpty());
    }
}
