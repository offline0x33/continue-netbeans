package com.bajinho.continuebeans;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface LlmProvider {
    void stream(String context, String prompt, String model, String mode,
            Consumer<String> onChunk, Consumer<Throwable> onError, Runnable onComplete);

    CompletableFuture<String> ask(String context, String prompt, String model, String mode);

    CompletableFuture<List<String>> listModels();

    CompletableFuture<Boolean> loadModel(String modelId);
}
