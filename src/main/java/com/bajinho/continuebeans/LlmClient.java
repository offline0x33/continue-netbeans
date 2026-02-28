package com.bajinho.continuebeans;

import java.net.http.HttpClient;
import com.google.gson.Gson;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.time.Duration;

public class LlmClient {

    private final HttpClient client;
    private final Gson gson;
    private LlmProvider provider;

    public LlmClient() {
        this.client = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(20))
                .proxy(java.net.ProxySelector.of(null))
                .build();
        this.gson = new Gson();
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

        provider.stream(contextoCodigo, perguntaUsuario, selectedModel, mode, onChunk, onError, onComplete);
    }

    public CompletableFuture<String> perguntarIAAsync(String contextoCodigo, String perguntaUsuario, String model,
            String mode) {
        String selectedModel = model != null ? model : ContinueSettings.getModel();
        return provider.ask(contextoCodigo, perguntaUsuario, selectedModel, mode);
    }

    public CompletableFuture<List<String>> getModelosDisponiveisAsync() {
        return provider.listModels();
    }

    public CompletableFuture<Boolean> loadModel(String modelId) {
        return provider.loadModel(modelId);
    }
}
