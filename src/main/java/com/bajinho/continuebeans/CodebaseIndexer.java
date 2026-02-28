package com.bajinho.continuebeans;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Advanced codebase scanner with .gitignore support.
 * Respects depth limits and excludes sensitive directories.
 */
public class CodebaseIndexer {

    private static final int DEFAULT_MAX_DEPTH = 5;
    private static final int DEFAULT_MAX_FILES = 100;

    // Default exclusions (even without .gitignore)
    private static final Set<String> DEFAULT_EXCLUDES = Set.of(
            ".git", ".gitignore", ".github",
            "target", "build", "dist",
            "node_modules", "vendor",
            ".idea", ".vscode", ".settings",
            ".gradle", ".m2",
            "__pycache__", ".pytest_cache",
            ".env", ".env.local", ".DS_Store");

    private Set<Pattern> gitignorePatterns = new HashSet<>();
    private int maxDepth = DEFAULT_MAX_DEPTH;
    private int maxFiles = DEFAULT_MAX_FILES;
    private int fileCount = 0;

    /**
     * Initialize indexer with root directory.
     */
    public CodebaseIndexer(String rootPath) {
        loadGitignore(rootPath);
    }

    /**
     * Load .gitignore patterns from root.
     */
    private void loadGitignore(String rootPath) {
        if (rootPath == null) {
            return;
        }

        try {
            Path gitignorePath = new File(rootPath).toPath().resolve(".gitignore");
            if (Files.exists(gitignorePath)) {
                Files.lines(gitignorePath)
                        .filter(line -> !line.trim().isEmpty() && !line.trim().startsWith("#"))
                        .forEach(pattern -> {
                            try {
                                // Convert gitignore pattern to regex (simplified)
                                Pattern regex = globToRegex(pattern);
                                gitignorePatterns.add(regex);
                            } catch (Exception e) {
                                ContinueLogger.warn("Failed to parse gitignore pattern: " + pattern, null);
                            }
                        });
            }
        } catch (IOException e) {
            ContinueLogger.warn("Failed to load .gitignore: " + e.getMessage(), null);
        }
    }

    /**
     * Scan directory and return summary.
     * Respects depth limits and exclusions.
     */
    public String scanDirectory(String rootPath) {
        fileCount = 0;
        StringBuilder sb = new StringBuilder();
        File root = new File(rootPath);
        scanDirectoryRecursive(root, "", sb, 0);
        return sb.toString();
    }

    private void scanDirectoryRecursive(File dir, String indent, StringBuilder sb, int depth) {
        // Respect depth limit
        if (depth > maxDepth) {
            return;
        }

        // Stop if we've collected too many files
        if (fileCount >= maxFiles) {
            if (depth == 0) {  // Only add message at root level to avoid duplicates
                sb.append(indent).append("... [Limite de arquivos atingido]\n");
            }
            return;
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            // Check again inside loop
            if (fileCount >= maxFiles) {
                sb.append(indent).append("... [Limite de arquivos atingido]\n");
                return;
            }

            String name = f.getName();

            // Check default exclusions
            if (DEFAULT_EXCLUDES.contains(name)) {
                continue;
            }

            // Check gitignore patterns
            if (shouldIgnore(name, f.getPath())) {
                continue;
            }

            // Add to result
            sb.append(indent)
                    .append(f.isDirectory() ? "📁 " : "📄 ")
                    .append(name)
                    .append("\n");

            fileCount++;

            // Recurse into directories
            if (f.isDirectory()) {
                scanDirectoryRecursive(f, indent + "  ", sb, depth + 1);
            }
        }
    }

    /**
     * Check if path should be ignored.
     */
    private boolean shouldIgnore(String fileName, String fullPath) {
        // Check against gitignore patterns
        for (Pattern pattern : gitignorePatterns) {
            if (pattern.matcher(fileName).find() || pattern.matcher(fullPath).find()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Simple glob-to-regex converter for gitignore patterns.
     */
    private Pattern globToRegex(String pattern) {
        StringBuilder regex = new StringBuilder("^");

        for (int i = 0; i < pattern.length(); i++) {
            char c = pattern.charAt(i);
            switch (c) {
                case '*':
                    // Handle **
                    if (i + 1 < pattern.length() && pattern.charAt(i + 1) == '*') {
                        regex.append(".*");
                        i++; // Skip next *
                    } else {
                        regex.append("[^/]*");
                    }
                    break;
                case '?':
                    regex.append("[^/]");
                    break;
                case '.':
                case '+':
                case '^':
                case '$':
                case '|':
                case '(':
                case ')':
                case '[':
                case ']':
                case '{':
                case '}':
                    regex.append("\\").append(c);
                    break;
                default:
                    regex.append(c);
            }
        }

        regex.append("$");
        return Pattern.compile(regex.toString());
    }

    /**
     * Set custom max depth.
     */
    public void setMaxDepth(int depth) {
        this.maxDepth = depth;
    }

    /**
     * Set custom max file count.
     */
    public void setMaxFiles(int count) {
        this.maxFiles = count;
    }

    /**
     * Get file count from last scan.
     */
    public int getFileCount() {
        return fileCount;
    }
}
