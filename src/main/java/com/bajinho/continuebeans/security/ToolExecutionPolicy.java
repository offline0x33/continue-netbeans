package com.bajinho.continuebeans.security;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

/**
 * Central policy for AI initiated tool execution.
 */
public final class ToolExecutionPolicy {

    private static final String WORKSPACE_PROPERTY = "continuebeans.workspace";
    private static final String ALLOW_BUILD_PROPERTY = "continuebeans.allowBuild";
    private static final Set<String> FILE_TOOLS = Set.of(
            "read_file", "create_file", "update_file", "delete_file", "list_directory",
            "open_editor", "analyze_code", "get_syntax_errors", "get_code_metrics", "refactor_code");

    private ToolExecutionPolicy() {
    }

    public static Path workspaceRoot() {
        String configured = System.getProperty(WORKSPACE_PROPERTY);
        Path root = configured == null || configured.isBlank()
                ? Paths.get(System.getProperty("user.dir"))
                : Paths.get(configured);
        return root.toAbsolutePath().normalize();
    }

    public static Path requireWorkspacePath(Object value) {
        if (value == null || String.valueOf(value).isBlank()) {
            throw new SecurityException("Caminho é obrigatório.");
        }
        Path candidate = Paths.get(String.valueOf(value));
        if (!candidate.isAbsolute()) {
            candidate = workspaceRoot().resolve(candidate);
        }
        candidate = candidate.toAbsolutePath().normalize();
        Path root = workspaceRoot();
        if (!candidate.startsWith(root)) {
            throw new SecurityException("Operação fora do workspace bloqueada: " + candidate);
        }
        return candidate;
    }

    public static void validate(String functionName, Map<String, Object> arguments) {
        if (functionName == null || functionName.isBlank()) {
            throw new SecurityException("Tool sem nome bloqueada.");
        }

        Map<String, Object> args = arguments == null ? Map.of() : arguments;
        if (FILE_TOOLS.contains(functionName)) {
            String key = "list_directory".equals(functionName) ? "directoryPath" : "filePath";
            if (args.containsKey(key)) {
                requireWorkspacePath(args.get(key));
            }
        }

        switch (functionName) {
            case "get_project_info":
            case "open_project":
                if (args.containsKey("projectPath")) {
                    requireWorkspacePath(args.get("projectPath"));
                }
                break;
            case "build_project":
                if (args.containsKey("projectPath")) {
                    requireWorkspacePath(args.get("projectPath"));
                }
                if (!Boolean.getBoolean(ALLOW_BUILD_PROPERTY)) {
                    throw new SecurityException(
                            "Build por agente bloqueado. Habilite -D" + ALLOW_BUILD_PROPERTY + "=true para permitir.");
                }
                break;
            case "create_project":
                if (args.containsKey("location")) {
                    requireWorkspacePath(args.get("location"));
                }
                break;
            case "delete_file":
                requireWorkspacePath(args.get("filePath"));
                if (!Boolean.TRUE.equals(args.get("confirm"))) {
                    throw new SecurityException("Confirmação explícita obrigatória para delete_file.");
                }
                break;
            default:
                break;
        }
    }
}
