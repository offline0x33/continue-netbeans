package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class LmStudioProviderCoverageTest {

    private final HttpClient client = HttpClient.newHttpClient();
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        ContinueSettings.setApiUrl("http://127.0.0.1:1234/v1/chat/completions");
    }

    @Test
    void streamsChatFormatAndAccumulatesConversation() throws Exception {
        startServer("/v1/chat/completions", exchange -> {
            String body = "data: {\"choices\":[{\"delta\":{\"content\":\"Olá\"}}]}\n"
                    + "data: {\"choices\":[{\"delta\":{\"content\":\" mundo\"}}]}\n"
                    + "data: [DONE]\n";
            respond(exchange, 200, body, "text/event-stream");
        });
        ContinueSettings.setApiUrl(url("/v1/chat/completions"));

        LmStudioProvider provider = new LmStudioProvider(client, new Gson());
        List<String> chunks = new ArrayList<>();
        CountDownLatch complete = new CountDownLatch(1);

        provider.stream("", "oi", "model", "Code", chunks::add,
                error -> { throw new AssertionError(error); }, complete::countDown);

        assertTrue(complete.await(5, TimeUnit.SECONDS));
        assertEquals(java.util.Arrays.asList("Olá", " mundo"), chunks);
    }

    @Test
    void streamsCompletionFormat() throws Exception {
        startServer("/v1/completions", exchange -> {
            String body = "data: {\"choices\":[{\"text\":\"parte 1\"}]}\n"
                    + "data: {\"choices\":[{\"text\":\" parte 2\"}]}\n"
                    + "data: [DONE]\n";
            respond(exchange, 200, body, "text/event-stream");
        });
        ContinueSettings.setApiUrl(url("/v1/completions"));

        LmStudioProvider provider = new LmStudioProvider(client, new Gson());
        List<String> chunks = new ArrayList<>();
        CountDownLatch complete = new CountDownLatch(1);

        provider.stream("ctx", "oi", "model", "Planning", chunks::add,
                error -> { throw new AssertionError(error); }, complete::countDown);

        assertTrue(complete.await(5, TimeUnit.SECONDS));
        assertEquals(java.util.Arrays.asList("parte 1", " parte 2"), chunks);
    }

    @Test
    void retriesOnceOn429AndThenCompletes() throws Exception {
        AtomicInteger calls = new AtomicInteger();
        startServer("/v1/chat/completions", exchange -> {
            if (calls.incrementAndGet() == 1) {
                respond(exchange, 429, "rate limited", "text/plain");
                return;
            }
            respond(exchange, 200,
                    "data: {\"choices\":[{\"delta\":{\"content\":\"ok\"}}]}\n"
                    + "data: [DONE]\n", "text/event-stream");
        });
        ContinueSettings.setApiUrl(url("/v1/chat/completions"));

        LmStudioProvider provider = new LmStudioProvider(client, new Gson());
        List<String> chunks = new ArrayList<>();
        CountDownLatch complete = new CountDownLatch(1);

        provider.stream("", "oi", "model", "Code", chunks::add,
                error -> { throw new AssertionError(error); }, complete::countDown);

        assertTrue(complete.await(5, TimeUnit.SECONDS));
        assertEquals(2, calls.get());
        assertEquals(java.util.Collections.singletonList("ok"), chunks);
    }

    @Test
    void unwrapsConnectionFailureAndReportsNetworkMessage() throws Exception {
        ContinueSettings.setApiUrl("http://127.0.0.1:1/v1/chat/completions");
        LmStudioProvider provider = new LmStudioProvider(client, new Gson());
        CountDownLatch failure = new CountDownLatch(1);
        Throwable[] error = {null};

        provider.stream("", "oi", "model", "Code", chunk -> { },
                err -> {
                    error[0] = err;
                    failure.countDown();
                }, () -> { });

        assertTrue(failure.await(5, TimeUnit.SECONDS));
        assertNotNull(error[0]);
        assertTrue(error[0].getMessage().contains("Erro de conexão"));
    }

    @Test
    void returnsFalseWhenLoadModelEndpointReturnsError() throws Exception {
        startServer("/api/v1/models/load", exchange -> respond(exchange, 400, "bad model", "application/json"));
        ContinueSettings.setApiUrl(url("/v1/chat/completions"));

        LmStudioProvider provider = new LmStudioProvider(client, new Gson());
        assertFalse(provider.loadModel("missing-model").join());
    }

    @Test
    void discoversModelsFromLegacyEndpoint() throws Exception {
        startServer("/v1/models", exchange -> respond(exchange, 200,
                "{\"models\":[{\"id\":\"legacy\"}]}", "application/json"));
        ContinueSettings.setApiUrl(url("/v1/chat/completions"));

        LmStudioProvider provider = new LmStudioProvider(client, new Gson());
        assertEquals(java.util.Collections.singletonList("legacy"), provider.listModels().join());
    }

    @Test
    void returnsEmptyModelsWhenBothDiscoveryEndpointsFail() throws Exception {
        startServer("/v1/models", exchange -> respond(exchange, 500, "error", "text/plain"));
        startServer("/api/v1/models", exchange -> respond(exchange, 500, "error", "text/plain"));
        ContinueSettings.setApiUrl(url("/v1/chat/completions"));

        LmStudioProvider provider = new LmStudioProvider(client, new Gson());
        assertTrue(provider.listModels().join().isEmpty());
    }

    private void startServer(String path, com.sun.net.httpserver.HttpHandler handler) throws IOException {
        if (server == null) {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.start();
        }
        server.createContext(path, handler);
    }

    private String url(String path) {
        return "http://127.0.0.1:" + server.getAddress().getPort() + path;
    }

    private static void respond(HttpExchange exchange, int status, String body, String contentType) throws IOException {
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(status, bytes.length);
        try (java.io.OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
