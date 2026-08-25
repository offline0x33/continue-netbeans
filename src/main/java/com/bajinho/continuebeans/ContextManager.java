package com.bajinho.continuebeans;

import com.bajinho.continuebeans.netbeans.NetBeansLanguageService;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

public class ContextManager {

    private static final Pattern FILE_PATTERN = Pattern.compile("@file:(\\S+)");
    private static final Pattern CODEBASE_PATTERN = Pattern.compile("@codebase");
    private static final int DEFAULT_MAX_CONTEXT_CHARS = 16_000;
    private static final int DEFAULT_MAX_CODEBASE_FILES = 200;

    public static String processContext(String input, String currentWorkDir) {
        StringBuilder promptWithContext = new StringBuilder(input);

        boolean hasExplicitContext = CODEBASE_PATTERN.matcher(input).find()
                || FILE_PATTERN.matcher(input).find();

        if (!hasExplicitContext && currentWorkDir != null) {
            String projectName = new File(currentWorkDir).getName();
            promptWithContext.insert(0,
                    "Contexto: Você está assistindo no projeto '" + projectName + "'. ");

            CodebaseIndexer indexer = new CodebaseIndexer(currentWorkDir);
            indexer.setMaxDepth(3);
            indexer.setMaxFiles(20);
            String structure = indexer.scanDirectory(currentWorkDir);

            if (structure != null && !structure.trim().isEmpty()) {
                String[] lines = structure.split("\\n");
                StringBuilder briefStructure = new StringBuilder();
                for (int i = 0; i < Math.min(lines.length, 10); i++) {
                    briefStructure.append(lines[i]).append("\\n");
                }

                promptWithContext.append("\\n\\nEstrutura resumida do projeto:\\n```\\n")
                        .append(briefStructure.toString().trim()).append("\\n```");
            }
        }

        if (CODEBASE_PATTERN.matcher(input).find()) {
            String structure = getProjectStructure(currentWorkDir);
            promptWithContext.append("\\n\\nEstrutura completa do Projeto (@codebase):\\n```\\n")
                    .append(structure).append("\\n```");
        }

        Matcher matcher = FILE_PATTERN.matcher(input);
        while (matcher.find()) {
            String filePath = matcher.group(1);
            String content = readFileContent(filePath, currentWorkDir);
            if (content != null) {
                promptWithContext.append("\\n\\nConteúdo do arquivo @file:").append(filePath)
                        .append(":\\n```\\n")
                        .append(content).append("\\n```");
                appendNativeJavaLanguageContext(promptWithContext, filePath, currentWorkDir);
            } else {
                promptWithContext.append("\\n\\n[ERRO: Não foi possível carregar o arquivo: ")
                        .append(filePath).append("]");
            }
        }

        return limitContext(promptWithContext.toString(), input);
    }

    private static void appendNativeJavaLanguageContext(StringBuilder prompt, String path, String currentWorkDir) {
        String resolvedPath = resolvePath(path, currentWorkDir);
        if (!resolvedPath.endsWith(".java")) {
            return;
        }

        try {
            Map<String, Object> analysis = NetBeansLanguageService.analyzeJavaFile(resolvedPath);
            Object symbolsObject = analysis.get("symbols");
            if (!(symbolsObject instanceof List)) {
                return;
            }

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> symbols = (List<Map<String, Object>>) symbolsObject;
            prompt.append("\\n\\nSemântica Java resolvida pelo Language Service do NetBeans:\\n```text\\n");
            prompt.append("package=").append(analysis.get("package")).append('\\n');
            for (Map<String, Object> symbol : symbols) {
                prompt.append(symbol.get("kind"))
                        .append(" ")
                        .append(symbol.get("name"))
                        .append(" @")
                        .append(symbol.get("line"))
                        .append(":")
                        .append(symbol.get("column"));
                Object qualifiedName = symbol.get("qualifiedName");
                if (qualifiedName != null) {
                    prompt.append(" [").append(qualifiedName).append("]");
                }
                prompt.append('\\n');
            }
            prompt.append("```");
        } catch (IOException e) {
            ContinueLogger.error("Failed to obtain native NetBeans language context for: " + resolvedPath, e);
        }
    }

    private static String resolvePath(String path, String currentWorkDir) {
        File file = new File(path);
        if (!file.isAbsolute() && currentWorkDir != null) {
            file = new File(currentWorkDir, path);
        }
        return file.getAbsolutePath();
    }

    static String limitContext(String context) {
        return limitContext(context, context);
    }

    static String limitContext(String context, String originalInput) {
        int maxChars = getMaxContextChars();
        if (context.length() <= maxChars) {
            return context;
        }

        int markerLength = 96;
        String prefix = originalInput == null ? "" : originalInput;
        int available = Math.max(0, maxChars - prefix.length() - markerLength);
        int tailChars = Math.min(available, Math.max(0, context.length() - prefix.length()));

        String marker = "\\n... [Contexto Truncado para " + maxChars + " caracteres; "
                + "pedido original preservado e fim do contexto preservado] ...\\n";

        if (prefix.length() >= context.length()) {
            return prefix;
        }

        return prefix + marker + context.substring(context.length() - tailChars);
    }

    static int getMaxContextChars() {
        String configured = System.getProperty("continue.context.maxChars");
        if (configured == null || configured.isBlank()) {
            return DEFAULT_MAX_CONTEXT_CHARS;
        }

        try {
            int value = Integer.parseInt(configured);
            return value > 512 ? value : DEFAULT_MAX_CONTEXT_CHARS;
        } catch (NumberFormatException ignored) {
            return DEFAULT_MAX_CONTEXT_CHARS;
        }
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
        if (rootPath == null) {
            return "Diretório do projeto não identificado.";
        }

        CodebaseIndexer indexer = new CodebaseIndexer(rootPath);
        indexer.setMaxDepth(8);
        indexer.setMaxFiles(DEFAULT_MAX_CODEBASE_FILES);
        return indexer.scanDirectory(rootPath);
    }
}
