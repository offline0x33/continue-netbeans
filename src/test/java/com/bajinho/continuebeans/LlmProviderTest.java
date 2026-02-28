package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for LlmProvider interface to achieve 100% coverage.
 */
class LlmProviderTest {

    @Test
    void testLlmProviderInterfaceMethods() {
        // Create a test implementation of LlmProvider
        TestLlmProvider provider = new TestLlmProvider();
        
        // Test that all methods exist and can be called
        assertDoesNotThrow(() -> {
            provider.stream("context", "prompt", "model", "mode", 
                chunk -> {}, error -> {}, () -> {});
        });
        
        assertDoesNotThrow(() -> {
            CompletableFuture<String> future = provider.ask("context", "prompt", "model", "mode");
            assertNotNull(future);
        });
        
        assertDoesNotThrow(() -> {
            CompletableFuture<List<String>> future = provider.listModels();
            assertNotNull(future);
        });
        
        assertDoesNotThrow(() -> {
            CompletableFuture<Boolean> future = provider.loadModel("modelId");
            assertNotNull(future);
        });
    }
    
    /**
     * Test implementation of LlmProvider for testing interface methods.
     */
    private static class TestLlmProvider implements LlmProvider {
        
        @Override
        public void stream(String context, String prompt, String model, String mode,
                          Consumer<String> onChunk, Consumer<Throwable> onError, Runnable onComplete) {
            // Simple implementation for testing
            onChunk.accept("test chunk");
            onComplete.run();
        }
        
        @Override
        public CompletableFuture<String> ask(String context, String prompt, String model, String mode) {
            return CompletableFuture.completedFuture("test response");
        }
        
        @Override
        public CompletableFuture<List<String>> listModels() {
            return CompletableFuture.completedFuture(List.of("model1", "model2"));
        }
        
        @Override
        public CompletableFuture<Boolean> loadModel(String modelId) {
            return CompletableFuture.completedFuture(true);
        }
    }
}
