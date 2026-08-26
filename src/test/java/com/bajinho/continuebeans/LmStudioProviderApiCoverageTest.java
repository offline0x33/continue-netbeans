package com.bajinho.continuebeans;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CountDownLatch;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class LmStudioProviderApiCoverageTest {
    private HttpServer server;
    private String originalUrl;

    @BeforeEach
    void setUp() {
        originalUrl = ContinueSettings.getApiUrl();
    }

    @AfterEach
    void tearDown() {
        ContinueSettings.setApiUrl(originalUrl);
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void askExtractsAssistantContentAndSendsChatPayload() throws Exception {
        AtomicReference<String> requestBody = new AtomicReference<>();
        startServer(exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            send(exchange, 200, "{\"choices\":[{\"message\":{\"content\":\"Hello API\"}}]}");
        });
        ContinueSettings.setApiUrl(serverUrl() + "/v1/chat/completions");
        String result = new LmStudioProvider(HttpClient.newHttpClient(), new Gson())
                .ask("ctx", "question", "model", "Planning").get(2, TimeUnit.SECONDS);
        assertEquals("Hello API", result);
        assertTrue(requestBody.get().contains("\"model\":\"model\""));
        assertTrue(requestBody.get().contains("Planeje antes de codar"));
    }

    @Test
    void askReturnsHttpErrorAndFriendlyMalformedJsonMessage() throws Exception {
        startServer(exchange -> send(exchange, 404, "missing"));
        ContinueSettings.setApiUrl(serverUrl() + "/v1/chat/completions");
        LmStudioProvider provider = new LmStudioProvider(HttpClient.newHttpClient(), new Gson());
        assertEquals("Erro HTTP 404", provider.ask("", "q", "m", "Code").get(2, TimeUnit.SECONDS));

        server.stop(0);
        startServer(exchange -> send(exchange, 200, "not-json"));
        ContinueSettings.setApiUrl(serverUrl() + "/v1/chat/completions");
        assertEquals("Erro ao processar JSON.", provider.ask("", "q", "m", "Docs").get(2, TimeUnit.SECONDS));
    }

    @Test
    void listModelsUsesFirstEndpointAndParsesLoadedInstances() throws Exception {
        startServer(exchange -> {
            if (exchange.getRequestURI().getPath().equals("/v1/models")) {
                send(exchange, 200, "{\"data\":[{\"id\":\"loaded\",\"loaded_instances\":[{}]},{\"id\":\"idle\",\"loaded_instances\":[]}]}");
            } else {
                send(exchange, 500, "fallback");
            }
        });
        ContinueSettings.setApiUrl(serverUrl() + "/v1/chat/completions");
        List<String> models = new LmStudioProvider(HttpClient.newHttpClient(), new Gson())
                .listModels().get(2, TimeUnit.SECONDS);
        assertEquals(List.of("loaded"), models);
    }

    @Test
    void listModelsFallsBackAndHandlesInvalidJsonAndBothFailures() throws Exception {
        startServer(exchange -> {
            if (exchange.getRequestURI().getPath().equals("/v1/models")) {
                send(exchange, 500, "error");
            } else {
                send(exchange, 200, "{\"data\":[{\"id\":\"fallback\"}]}");
            }
        });
        ContinueSettings.setApiUrl(serverUrl() + "/v1/chat/completions");
        LmStudioProvider provider = new LmStudioProvider(HttpClient.newHttpClient(), new Gson());
        assertEquals(List.of("fallback"), provider.listModels().get(2, TimeUnit.SECONDS));

        server.stop(0);
        startServer(exchange -> send(exchange, 200, "{invalid"));
        ContinueSettings.setApiUrl(serverUrl() + "/v1/chat/completions");
        assertTrue(provider.listModels().get(2, TimeUnit.SECONDS).isEmpty());

        server.stop(0);
        startServer(exchange -> send(exchange, 500, "error"));
        ContinueSettings.setApiUrl(serverUrl() + "/v1/chat/completions");
        assertTrue(provider.listModels().get(2, TimeUnit.SECONDS).isEmpty());
    }

    @Test
    void streamHappyPathCompletesOnceAndAccumulatesChunks() throws Exception {
        startServer(exchange -> {
            byte[] body = ("data: {\"choices\":[{\"delta\":{\"content\":\"Hi \"}}]}\n"
                    + "data: {\"choices\":[{\"delta\":{\"content\":\"there\"}}]}\n"
                    + "data: [DONE]\n").getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        ContinueSettings.setApiUrl(serverUrl() + "/v1/chat/completions");
        StringBuilder content = new StringBuilder();
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        new LmStudioProvider(HttpClient.newHttpClient(), new Gson()).stream("", "q", "m", "Code",
                content::append, error::set, done::countDown);
        assertTrue(done.await(3, TimeUnit.SECONDS));
        assertEquals("Hi there", content.toString());
        assertTrue(error.get() == null);
    }

    @Test
    void streamNon200ReportsErrorAnd429RetriesOnce() throws Exception {
        AtomicReference<Throwable> error = new AtomicReference<>();
        CountDownLatch done = new CountDownLatch(1);
        startServer(exchange -> send(exchange, 500, "error"));
        ContinueSettings.setApiUrl(serverUrl() + "/v1/chat/completions");
        new LmStudioProvider(HttpClient.newHttpClient(), new Gson()).stream("", "q", "m", "Code",
                chunk -> { }, error::set, done::countDown);
        assertTrue(done.await(2, TimeUnit.SECONDS));
        assertFalse(error.get() == null);

        server.stop(0);
        java.util.concurrent.atomic.AtomicInteger calls = new java.util.concurrent.atomic.AtomicInteger();
        CountDownLatch retryDone = new CountDownLatch(1);
        AtomicReference<String> retryContent = new AtomicReference<>("");
        startServer(exchange -> {
            if (calls.incrementAndGet() == 1) {
                send(exchange, 429, "rate");
                return;
            }
            byte[] body = "data: {\"choices\":[{\"delta\":{\"content\":\"ok\"}}]}\n".getBytes(java.nio.charset.StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) { out.write(body); }
        });
        ContinueSettings.setApiUrl(serverUrl() + "/v1/chat/completions");
        new LmStudioProvider(HttpClient.newHttpClient(), new Gson()).stream("", "q", "m", "Code",
                retryContent::set, error, retryDone::countDown);
        assertTrue(retryDone.await(2, TimeUnit.SECONDS));
        assertEquals(2, calls.get());
        assertEquals("ok", retryContent.get());
    }

    private void startServer(com.sun.net.httpserver.HttpHandler handler) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", handler);
        server.start();
    }

    private String serverUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort();
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
