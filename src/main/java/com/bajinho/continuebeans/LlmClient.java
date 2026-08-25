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
        // Inicialmente defaulting para LM Studio, mas pronto para expansão
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

        if (shouldUseWorkspaceTools(perguntaUsuario)) {
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
        if (shouldUseWorkspaceTools(perguntaUsuario)) {
            return toolCallingIntegration.processRequestWithToolCalling(perguntaUsuario, "lmstudio")
                    .thenApply(AIToolCallingIntegration.AIResponse::getContent);
        }
        return provider.ask(contextoCodigo, perguntaUsuario, selectedModel, mode);
    }

    /**
     * Execute an explicit workspace request through the real NetBeans tool layer.
     * This is intentionally limited to requests that clearly ask the model to inspect
     * or mutate files/project state, so ordinary chat keeps the existing streaming path.
     */
    private boolean shouldUseWorkspaceTools(String message) {
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
