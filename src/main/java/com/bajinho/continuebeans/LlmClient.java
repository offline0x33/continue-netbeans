package com.bajinho.continuebeans;

import com.bajinho.continuebeans.ai.AIToolCallingIntegration;
import java.net.http.HttpClient;
import com.google.gson.Gson;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.time.Duration;
import java.util.regex.Pattern;

public class LlmClient {

    private static final Pattern ABSOLUTE_PATH_PATTERN = Pattern.compile("(?:^|\\s)(?:/home/|/workspace/|/tmp/|/opt/|/var/|[A-Za-z]:\\\\)");

    private static final String TASK_ACTION_WORDS = String.join("|",
            "crie", "criar", "create", "implement", "implemente", "implementar", "adicione", "adicionar",
            "add", "remova", "remover", "remove", "edite", "editar", "edit", "altere", "alterar", "modify",
            "corrija", "corrigir", "fix", "conserte", "refatore", "refatorar", "refactor", "analise", "analisar",
            "analyse", "analyze", "read", "leia", "ler", "liste", "listar", "list", "abra", "abrir", "open", "build",
            "compile", "teste", "testar", "test", "execute", "executa", "executar", "run", "rode",
            "rodar", "configure", "configurar", "deploy", "commit", "git", "instale", "instalar", "install",
            "gere", "gerar", "generate", "escreva", "escrever", "write", "apague", "apagar", "delete",
            "renomeie", "renomear", "rename", "mova", "mover", "move");

    private static final Pattern INFORMATIONAL_PATTERN = Pattern.compile(
            "(?i)^\\s*(olá|ola|oi|hey|hi|hello|bom dia|boa tarde|boa noite|"
                    + "como (você|voce) está|como vai|"
                    + "(me )?(fale|diga|conte|explique|descreva|mostre)( (sobre|desse|deste|do|da|o|a))?|"
                    + "o que (é|e|são|sao)|quem (é|e)|para que serve|"
                    + "(what|who|how|why|when|where)\\b).*$");

    private final HttpClient client;
    private final Gson gson;
    private final AIToolCallingIntegration toolCallingIntegration;
    private LlmProvider provider;

    public LlmClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .proxy(java.net.ProxySelector.of(null))
                .build();
        this.gson = new Gson();
        this.toolCallingIntegration = new AIToolCallingIntegration();
        this.provider = new LmStudioProvider(client, gson);
    }

    public String resolveUrl(String url) {
        return UrlUtils.resolveUrl(url);
    }

    public void perguntarIAStreaming(String contextoCodigo, String perguntaUsuario, String model, String mode,
            Consumer<String> onChunk, Consumer<Throwable> onError, Runnable onComplete) {

        String selectedModel = model != null ? model : ContinueSettings.getModel();
        if (selectedModel == null || selectedModel.trim().isEmpty()) {
            onError.accept(new Exception("Modelo não selecionado."));
            return;
        }

        // Docs/Planning must stay chat-only: never route to workspace tool calling.
        AgentMode agentMode = mode == null || mode.isBlank()
                ? ContinueSettings.getAgentMode()
                : AgentMode.fromLabel(mode);
        if (!agentMode.isChatOnly() && shouldUseWorkspaceTools(perguntaUsuario)) {
            toolCallingIntegration.setWorkspaceRoot(EditorUtils.getCurrentProjectDirectory());
            toolCallingIntegration.processRequestWithToolCalling(perguntaUsuario, "lmstudio")
                    .thenAccept(response -> {
                        if (response == null || response.getContent() == null || response.getContent().isBlank()) {
                            onError.accept(new IllegalStateException("A integração de workspace não retornou conteúdo."));
                            return;
                        }
                        onChunk.accept(response.getContent());
                        onComplete.run();
                    })
                    .exceptionally(error -> {
                        Throwable cause = error.getCause() != null ? error.getCause() : error;
                        onError.accept(cause);
                        return null;
                    });
            return;
        }

        provider.stream(contextoCodigo, perguntaUsuario, selectedModel, mode, onChunk, onError, onComplete);
    }

    public CompletableFuture<String> perguntarIAAsync(String contextoCodigo, String perguntaUsuario, String model,
            String mode) {
        String selectedModel = model != null ? model : ContinueSettings.getModel();
        AgentMode agentMode = mode == null || mode.isBlank()
                ? ContinueSettings.getAgentMode()
                : AgentMode.fromLabel(mode);
        if (!agentMode.isChatOnly() && shouldUseWorkspaceTools(perguntaUsuario)) {
            toolCallingIntegration.setWorkspaceRoot(EditorUtils.getCurrentProjectDirectory());
            return toolCallingIntegration.processRequestWithToolCalling(perguntaUsuario, "lmstudio")
                    .thenApply(AIToolCallingIntegration.AIResponse::getContent);
        }
        return provider.ask(contextoCodigo, perguntaUsuario, selectedModel, mode);
    }

    /**
     * Mode-aware routing: whether this message should enter the task orchestrator.
     */
    public boolean shouldUseTaskOrchestrator(String message) {
        return shouldUseTaskOrchestrator(message, ContinueSettings.getAgentMode());
    }

    public boolean shouldUseTaskOrchestrator(String message, AgentMode mode) {
        AgentMode effective = mode == null ? ContinueSettings.getAgentMode() : mode;

        if (effective.isChatOnly()) {
            return false;
        }

        if (message == null || message.isBlank()) {
            return false;
        }

        String normalized = message.toLowerCase(java.util.Locale.ROOT).trim();

        // Greetings / pure Q&A never become a task plan in any mode.
        if (isInformationalOrGreeting(normalized)) {
            return false;
        }

        if (effective.prefersTaskGraph()) {
            // Agent mode: any non-informational request becomes a task plan.
            return true;
        }

        // CODE (hybrid): only clear engineering intents.
        if (normalized.contains("@file:") || normalized.contains("@codebase")) {
            return true;
        }
        if (ABSOLUTE_PATH_PATTERN.matcher(message).find()) {
            return true;
        }

        String compact = normalized.replaceAll("[^\\p{L}\\p{N}_-]+", " ").trim();
        if (compact.isEmpty()) {
            return false;
        }
        for (String word : compact.split("\\s+")) {
            if (word.matches(TASK_ACTION_WORDS)) {
                return true;
            }
        }
        return false;
    }

    private boolean isInformationalOrGreeting(String normalized) {
        if (INFORMATIONAL_PATTERN.matcher(normalized).matches()) {
            return true;
        }
        if (normalized.matches(".*\\b(projeto|project|workspace|código|codigo|code)\\b.*")
                && !hasActionVerb(normalized)
                && normalized.matches(".*\\b(fale|diga|conte|explique|descreva|mostre|sobre|o que|qual|quais)\\b.*")) {
            return true;
        }
        return false;
    }

    private boolean hasActionVerb(String normalized) {
        String compact = normalized.replaceAll("[^\\p{L}\\p{N}_-]+", " ").trim();
        for (String word : compact.split("\\s+")) {
            if (word.matches(TASK_ACTION_WORDS)) {
                return true;
            }
        }
        return false;
    }

    boolean shouldUseWorkspaceTools(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }

        String normalized = message.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("@file:") || normalized.contains("@codebase")) {
            return true;
        }

        if (ABSOLUTE_PATH_PATTERN.matcher(message).find()) {
            return normalized.contains("leia")
                    || normalized.contains("ler")
                    || normalized.contains("read")
                    || normalized.contains("liste")
                    || normalized.contains("listar")
                    || normalized.contains("list")
                    || normalized.contains("abra")
                    || normalized.contains("open")
                    || normalized.contains("analise")
                    || normalized.contains("analisar")
                    || normalized.contains("analyse")
                    || normalized.contains("edite")
                    || normalized.contains("editar")
                    || normalized.contains("alter")
                    || normalized.contains("corrija")
                    || normalized.contains("corrigir")
                    || normalized.contains("build")
                    || normalized.contains("compile")
                    || normalized.contains("execut")
                    || normalized.contains("crie")
                    || normalized.contains("criar");
        }

        return false;
    }

    public CompletableFuture<List<String>> getModelosDisponiveisAsync() {
        return provider.listModels();
    }

    public CompletableFuture<Boolean> loadModel(String modelId) {
        return provider.loadModel(modelId);
    }
}
