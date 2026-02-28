package com.bajinho.continuebeans;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ContextManager {

    private static final Pattern FILE_PATTERN = Pattern.compile("@file:(\\S+)");
    private static final Pattern CODEBASE_PATTERN = Pattern.compile("@codebase");

    public static String processContext(String input, String currentWorkDir) {
        StringBuilder promptWithContext = new StringBuilder(input);

        // Process @codebase
        if (CODEBASE_PATTERN.matcher(input).find()) {
            String structure = getProjectStructure(currentWorkDir);
            promptWithContext.append("\n\nEstrutura do Projeto (@codebase):\n```\n")
                    .append(structure).append("\n```");
        }

        // Process @file:
        Matcher matcher = FILE_PATTERN.matcher(input);

        while (matcher.find()) {
            String filePath = matcher.group(1);
            String content = readFileContent(filePath, currentWorkDir);
            if (content != null) {
                promptWithContext.append("\n\nConteúdo do arquivo @file:").append(filePath).append(":\n```\n")
                        .append(content).append("\n```");
            } else {
                promptWithContext.append("\n\n[ERRO: Não foi possível carregar o arquivo: ").append(filePath)
                        .append("]");
            }
        }

        // Truncation Logic (Enterprise Rule)
        if (promptWithContext.length() > 4000) {
            String truncated = promptWithContext.substring(0, 4000);
            return truncated + "\n... [Contexto Truncado]";
        }

        return promptWithContext.toString();
    }

    private static String readFileContent(String path, String currentWorkDir) {
        try {
            File f = new File(path);
            if (!f.isAbsolute() && currentWorkDir != null) {
                f = new File(currentWorkDir, path);
            }

            if (f.exists() && f.isFile()) {
                return Files.readString(f.toPath());
            }

            // Tentar via FileObject do NetBeans (pode estar no classpath ou projeto aberto)
            FileObject fo = FileUtil.toFileObject(f);
            if (fo != null) {
                return fo.asText();
            }
        } catch (IOException e) {
            ContinueLogger.error("Failed to read context file: " + path, e);
        }
        return null;
    }

    private static String getProjectStructure(String rootPath) {
        if (rootPath == null)
            return "Diretório do projeto não identificado.";

        CodebaseIndexer indexer = new CodebaseIndexer(rootPath);
        indexer.setMaxDepth(5);
        indexer.setMaxFiles(50);
        
        return indexer.scanDirectory(rootPath);
    }
}
