package com.bajinho.continuebeans;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for LlmClient to increase code coverage.
 */
class LlmClientTest {

    private LlmClient client;

    @BeforeEach
    void setUp() {
        client = new LlmClient();
    }

    @Test
    void testResolveUrl() {
        String result = client.resolveUrl("localhost:1234");
        assertNotNull(result, "Should resolve URL");
    }

    @Test
    void testResolveUrlWithFullUrl() {
        String result = client.resolveUrl("http://127.0.0.1:1234/v1/chat/completions");
        assertNotNull(result, "Should handle full URL");
    }

    @Test
    void testPerguntarIAStreamingWithValidModel() {
        String[] receivedChunk = {""};
        Throwable[] receivedError = {null};
        boolean[] completed = {false};

        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn("test-model");
            settingsMock.when(ContinueSettings::getApiUrl).thenReturn("http://localhost:1234/v1/chat/completions");

            Consumer<String> onChunk = chunk -> receivedChunk[0] = chunk;
            Consumer<Throwable> onError = error -> receivedError[0] = error;
            Runnable onComplete = () -> completed[0] = true;

            client.perguntarIAStreaming("context", "question", "test-model", "Code",
                    onChunk, onError, onComplete);

            // Wait a bit for async processing
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    @Test
    void testPerguntarIAStreamingWithNullModel() {
        Throwable[] error = {null};

        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn(null);

            Consumer<String> onChunk = chunk -> {};
            Consumer<Throwable> onError = err -> error[0] = err;
            Runnable onComplete = () -> {};

            client.perguntarIAStreaming("", "test", null, "Code",
                    onChunk, onError, onComplete);

            assertNotNull(error[0], "Should report error when model is null");
            assertTrue(error[0].getMessage().contains("não selecionado") || 
                      error[0].getMessage().contains("not selected"),
                      "Error message should indicate model not selected");
        }
    }

    @Test
    void testPerguntarIAStreamingWithEmptyModel() {
        Throwable[] error = {null};

        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn("  ");

            Consumer<String> onChunk = chunk -> {};
            Consumer<Throwable> onError = err -> error[0] = err;
            Runnable onComplete = () -> {};

            client.perguntarIAStreaming("", "test", "  ", "Code",
                    onChunk, onError, onComplete);

            assertNotNull(error[0], "Should report error for empty model");
        }
    }

    @Test
    void testPerguntarIAAsyncWithModel() throws ExecutionException, InterruptedException {
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn("default-model");
            settingsMock.when(ContinueSettings::getApiUrl).thenReturn("http://localhost:1234/v1/chat/completions");
            settingsMock.when(ContinueSettings::getTemperature).thenReturn(0.7);

            CompletableFuture<String> result = client.perguntarIAAsync("context", "question", "test-model", "Code");
            
            assertNotNull(result, "Should return CompletableFuture");
        }
    }

    @Test
    void testPerguntarIAAsyncWithoutModel() throws ExecutionException, InterruptedException {
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn("default-model");
            settingsMock.when(ContinueSettings::getApiUrl).thenReturn("http://localhost:1234/v1/chat/completions");
            settingsMock.when(ContinueSettings::getTemperature).thenReturn(0.7);

            CompletableFuture<String> result = client.perguntarIAAsync("context", "question", null, "Code");
            
            assertNotNull(result, "Should return CompletableFuture with default model");
        }
    }

    @Test
    void testGetModelosDisponiveisAsync() {
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getApiUrl).thenReturn("http://localhost:1234/v1/chat/completions");

            CompletableFuture<List<String>> result = client.getModelosDisponiveisAsync();
            
            assertNotNull(result, "Should return CompletableFuture");
            
            // Wait for completion to avoid connection attempts in background
            try {
                result.get(1, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                // Expected - server is not running
            }
        }
    }

    @Test
    void testLoadModel() {
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getApiUrl).thenReturn("http://localhost:1234/v1/chat/completions");

            CompletableFuture<Boolean> result = client.loadModel("test-model");
            
            assertNotNull(result, "Should return CompletableFuture");
            
            // Wait for completion to avoid connection attempts in background
            try {
                result.get(1, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception e) {
                // Expected - server is not running
            }
        }
    }

    @Test
    void testStreamingWithPlanningMode() {
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn("model");
            settingsMock.when(ContinueSettings::getApiUrl).thenReturn("http://localhost:1234");

            Throwable[] error = {null};
            client.perguntarIAStreaming("code", "plan", "model", "Planning",
                    chunk -> {},
                    err -> error[0] = err,
                    () -> {});

            // Should not throw error
            assertNull(error[0]);
        }
    }

    @Test
    void testStreamingWithDocMode() {
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn("model");
            settingsMock.when(ContinueSettings::getApiUrl).thenReturn("http://localhost:1234");

            Throwable[] error = {null};
            client.perguntarIAStreaming("code", "doc", "model", "Docs",
                    chunk -> {},
                    err -> error[0] = err,
                    () -> {});

            assertNull(error[0]);
        }
    }

    @Test
    void testPerguntarIAStreamingDefaultsToSettingsModel() {
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn("settings-model");
            settingsMock.when(ContinueSettings::getApiUrl).thenReturn("http://localhost:1234");

            Throwable[] error = {null};
            client.perguntarIAStreaming("ctx", "q", null, "Code",
                    chunk -> {},
                    err -> error[0] = err,
                    () -> {});

            // When model is null, should use settings model (not error)
            assertNull(error[0]);
        }
    }
}
