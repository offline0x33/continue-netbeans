package com.bajinho.continuebeans;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LlmClient. Provider and network interactions are mocked.
 */
class LlmClientTest {
    private LlmClient client;
    private MockedConstruction<LmStudioProvider> providerConstruction;
    private LmStudioProvider provider;

    @BeforeEach
    void setUp() {
        providerConstruction = mockConstruction(LmStudioProvider.class, (mock, context) -> {
            when(mock.ask(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(CompletableFuture.completedFuture("mock response"));
            when(mock.listModels()).thenReturn(CompletableFuture.completedFuture(List.of("mock-model")));
            when(mock.loadModel(anyString())).thenReturn(CompletableFuture.completedFuture(true));
            doAnswer(invocation -> {
                Consumer<String> onChunk = invocation.getArgument(4);
                Runnable onComplete = invocation.getArgument(6);
                if (onChunk != null) onChunk.accept("mock chunk");
                if (onComplete != null) onComplete.run();
                return null;
            }).when(mock).stream(anyString(), anyString(), anyString(), anyString(), any(), any(), any());
        });
        client = new LlmClient();
        provider = providerConstruction.constructed().get(0);
    }

    @AfterEach
    void tearDown() { providerConstruction.close(); }

    @Test
    void testResolveUrl() {
        String endpoint = "localhost:1234";
        try (MockedStatic<UrlUtils> urlUtils = mockStatic(UrlUtils.class)) {
            urlUtils.when(() -> UrlUtils.resolveUrl(endpoint)).thenReturn(endpoint);
            assertEquals(endpoint, client.resolveUrl(endpoint));
            urlUtils.verify(() -> UrlUtils.resolveUrl(endpoint));
        }
    }

    @Test
    void testResolveUrlWithFullUrl() {
        String endpoint = "http://127.0.0.1:1234/v1/chat/completions";
        try (MockedStatic<UrlUtils> urlUtils = mockStatic(UrlUtils.class)) {
            urlUtils.when(() -> UrlUtils.resolveUrl(endpoint)).thenReturn(endpoint);
            assertEquals(endpoint, client.resolveUrl(endpoint));
            urlUtils.verify(() -> UrlUtils.resolveUrl(endpoint));
        }
    }

    @Test
    void testPerguntarIAStreamingWithValidModel() throws Exception {
        String[] receivedChunk = {null};
        String[] receivedError = {null};
        CountDownLatch completion = new CountDownLatch(1);
        client.perguntarIAStreaming("context", "question", "test-model", "Code",
                chunk -> receivedChunk[0] = chunk,
                error -> { receivedError[0] = error.getMessage(); completion.countDown(); },
                completion::countDown);
        assertTrue(completion.await(1, TimeUnit.SECONDS));
        assertEquals("mock chunk", receivedChunk[0]);
        assertNull(receivedError[0]);
        verify(provider).stream(eq("context"), eq("question"), eq("test-model"), eq("Code"), any(), any(), any());
    }

    @Test
    void testPerguntarIAStreamingWithNullModel() {
        Throwable[] error = {null};
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn(null);
            client.perguntarIAStreaming("", "test", null, "Code", chunk -> {}, err -> error[0] = err, () -> {});
        }
        assertNotNull(error[0]);
        assertTrue(error[0].getMessage().contains("não selecionado"));
        verifyNoInteractions(provider);
    }

    @Test
    void testPerguntarIAStreamingWithEmptyModel() {
        Throwable[] error = {null};
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn("  ");
            client.perguntarIAStreaming("", "test", "  ", "Code", chunk -> {}, err -> error[0] = err, () -> {});
        }
        assertNotNull(error[0]);
        verifyNoInteractions(provider);
    }

    @Test
    void testPerguntarIAAsyncWithModel() throws Exception {
        CompletableFuture<String> result = client.perguntarIAAsync("context", "question", "test-model", "Code");
        assertEquals("mock response", result.get(1, TimeUnit.SECONDS));
        verify(provider).ask("context", "question", "test-model", "Code");
    }

    @Test
    void testPerguntarIAAsyncWithoutModel() throws Exception {
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn("default-model");
            CompletableFuture<String> result = client.perguntarIAAsync("context", "question", null, "Code");
            assertEquals("mock response", result.get(1, TimeUnit.SECONDS));
            verify(provider).ask("context", "question", "default-model", "Code");
        }
    }

    @Test
    void testGetModelosDisponiveisAsync() throws Exception {
        assertEquals(List.of("mock-model"), client.getModelosDisponiveisAsync().get(1, TimeUnit.SECONDS));
        verify(provider).listModels();
    }

    @Test
    void testLoadModel() throws Exception {
        assertTrue(client.loadModel("test-model").get(1, TimeUnit.SECONDS));
        verify(provider).loadModel("test-model");
    }

    @Test
    void testStreamingWithPlanningMode() throws Exception {
        doAnswer(invocation -> {
            Consumer<Throwable> onError = invocation.getArgument(5);
            Runnable onComplete = invocation.getArgument(6);
            onError.accept(new IllegalStateException("mock provider failure"));
            onComplete.run();
            return null;
        }).when(provider).stream(anyString(), anyString(), anyString(), anyString(), any(), any(), any());
        Throwable[] error = {null};
        CountDownLatch completion = new CountDownLatch(1);
        client.perguntarIAStreaming("code", "plan", "model", "Planning", chunk -> {}, err -> {
            error[0] = err;
            completion.countDown();
        }, completion::countDown);
        assertTrue(completion.await(1, TimeUnit.SECONDS));
        assertNotNull(error[0]);
        assertEquals("mock provider failure", error[0].getMessage());
    }

    @Test
    void testStreamingWithDocMode() throws Exception {
        CountDownLatch completion = new CountDownLatch(1);
        client.perguntarIAStreaming("code", "doc", "model", "Docs", chunk -> {},
                err -> fail("Mock provider should not fail: " + err), completion::countDown);
        assertTrue(completion.await(1, TimeUnit.SECONDS));
    }

    @Test
    void testPerguntarIAStreamingDefaultsToSettingsModel() throws Exception {
        try (MockedStatic<ContinueSettings> settingsMock = mockStatic(ContinueSettings.class)) {
            settingsMock.when(ContinueSettings::getModel).thenReturn("settings-model");
            CountDownLatch completion = new CountDownLatch(1);
            client.perguntarIAStreaming("ctx", "q", null, "Code", chunk -> {}, failConsumer(), completion::countDown);
            assertTrue(completion.await(1, TimeUnit.SECONDS));
            verify(provider).stream(eq("ctx"), eq("q"), eq("settings-model"), eq("Code"), any(), any(), any());
        }
    }

    @Test
    void conversationalMessagesBypassTaskOrchestrator() {
        assertFalse(client.shouldUseTaskOrchestrator("Olá"));
        assertFalse(client.shouldUseTaskOrchestrator("Como você está?"));
        assertFalse(client.shouldUseTaskOrchestrator("me explique o que é dependency injection"));
        assertFalse(client.shouldUseTaskOrchestrator("me fale desse projeto"));
    }

    @Test
    void engineeringMessagesUseTaskOrchestrator() {
        assertTrue(client.shouldUseTaskOrchestrator("corrija o pom.xml"));
        assertTrue(client.shouldUseTaskOrchestrator("leia /home/bajinho/project/pom.xml"));
        assertTrue(client.shouldUseTaskOrchestrator("crie uma classe UserService"));
    }

    private static Consumer<Throwable> failConsumer() {
        return error -> fail("Mock provider should not fail: " + error);
    }
}
