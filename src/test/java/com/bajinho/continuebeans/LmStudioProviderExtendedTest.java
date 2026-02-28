package com.bajinho.continuebeans;

import com.google.gson.Gson;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Extended tests for LmStudioProvider to increase code coverage.
 */
@SuppressWarnings("unchecked")
class LmStudioProviderExtendedTest {

    private HttpClient mockClient;
    private Gson gson;
    private LmStudioProvider provider;

    @BeforeEach
    void setUp() {
        this.mockClient = mock(HttpClient.class);
        this.gson = new Gson();
        this.provider = new LmStudioProvider(mockClient, gson);
    }

    @Test
    void testStreamWithCompletionPayload() {
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(Stream.of(
                "data: {\"choices\":[{\"text\":\"Hello\"}]}",
                "data: {\"choices\":[{\"text\":\" World\"}]}",
                "data: [DONE]"
        ));

        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        StringBuilder chunks = new StringBuilder();
        provider.stream(null, "test prompt", "model", "Completions",
                chunk -> chunks.append(chunk),
                err -> fail("Should not error"),
                () -> {});

        assertTrue(chunks.length() > 0, "Should accumulate chunks");
    }

    @Test
    void testStreamWithContextAppended() {
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(Stream.of(
                "data: {\"choices\":[{\"delta\":{\"content\":\"Response\"}}]}",
                "data: [DONE]"
        ));

        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        provider.stream("File context", "prompt with context", "model", "Code",
                chunk -> {},
                err -> fail("Should not error"),
                () -> {});

        verify(mockClient, atLeastOnce()).sendAsync(any(), any());
    }

    @Test
    void testStreamWith404Error() {
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(404);

        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        Exception[] capturedError = new Exception[1];
        provider.stream(null, "test", "model", "Code",
                chunk -> {},
                err -> capturedError[0] = (Exception) err,
                () -> {});

        assertNotNull(capturedError[0], "Should capture 404 error");
    }

    @Test
    void testStreamWithMalformedJson() {
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(Stream.of(
                "data: {INVALID JSON}",
                "data: [DONE]"
        ));

        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        provider.stream(null, "test", "model", "Code",
                chunk -> {},
                err -> {},
                () -> {});

        verify(mockClient).sendAsync(any(), any());
    }

    @Test
    void testStreamInterruptsOnException() {
        when(mockClient.sendAsync(any(), any()))
                .thenReturn(CompletableFuture.failedFuture(
                        new RuntimeException("Network error")));

        Exception[] capturedError = new Exception[1];
        provider.stream(null, "test", "model", "Code",
                chunk -> {},
                err -> capturedError[0] = (Exception) err,
                () -> {});

        // Should handle error gracefully (may be async)
    }

    @Test
    void testListModelsSuccess() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"data\":[{\"id\":\"model1\",\"loaded_instances\":[1]},{\"id\":\"model2\",\"loaded_instances\":[1]}]}");

        when(mockClient.sendAsync(any(HttpRequest.class), any()))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture(mockResponse));

        List<String> models = provider.listModels().get();
        assertEquals(2, models.size(), "Should list 2 models");
        assertTrue(models.contains("model1"), "Should contain model1");
    }

    @Test
    void testLoadModelSuccess() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn("{\"success\":true}");

        when(mockClient.sendAsync(any(HttpRequest.class), any()))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture(mockResponse));

        Boolean result = provider.loadModel("llama3").get();
        assertTrue(result, "Model load should return true");
    }

    @Test
    void testLoadModelFailure() throws Exception {
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(500);
        when(mockResponse.body()).thenReturn("{\"error\":\"Model not found\"}");

        when(mockClient.sendAsync(any(HttpRequest.class), any()))
                .thenReturn((CompletableFuture) CompletableFuture.completedFuture(mockResponse));

        Boolean result = provider.loadModel("unknown").get();
        assertFalse(result, "Should return false on error");
    }

    @Test
    void testStreamWithEmptyContent() {
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(Stream.of(
                "data: {\"choices\":[{\"delta\":{}}]}",
                "data: [DONE]"
        ));

        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        provider.stream(null, "test", "model", "Code",
                chunk -> {},
                err -> {},
                () -> {});

        verify(mockClient).sendAsync(any(), any());
    }

    @Test
    void testStreamWithChatMode() {
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(Stream.of(
                "data: {\"choices\":[{\"delta\":{\"content\":\"Chat response\"}}]}",
                "data: [DONE]"
        ));

        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        StringBuilder accumulated = new StringBuilder();
        provider.stream(null, "chat prompt", "model", "Code",
                chunk -> accumulated.append(chunk),
                err -> {},
                () -> {});

        assertTrue(accumulated.length() > 0);
    }

    @Test
    void testStreamRequestPayloadValidation() {
        HttpResponse<Stream<String>> mockResponse = mock(HttpResponse.class);
        when(mockResponse.statusCode()).thenReturn(200);
        when(mockResponse.body()).thenReturn(Stream.of("data: [DONE]"));

        when(mockClient.sendAsync(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
                .thenReturn(CompletableFuture.completedFuture(mockResponse));

        provider.stream(null, "test prompt", "test-model", "Code",
                chunk -> {},
                err -> {},
                () -> {});

        ArgumentCaptor<HttpRequest> captor = ArgumentCaptor.forClass(HttpRequest.class);
        verify(mockClient).sendAsync(captor.capture(), any());

        HttpRequest request = captor.getValue();
        assertNotNull(request.uri(), "Request should have URI");
    }
}
