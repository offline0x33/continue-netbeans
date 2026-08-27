package com.bajinho.continuebeans.ai;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OllamaIntegrationTest {

    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void processRequestReturnsPlainContentAndSendsOllamaPayload() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(request.contains("\"model\":\"test-model\""));
            assertTrue(request.contains("\"stream\":false"));
            assertTrue(request.contains("hello"));
            send(exchange, 200, "{\"choices\":[{\"message\":{\"content\":\"ollama response\"}}]}");
        });

        OllamaIntegration integration = new OllamaIntegration(baseUrl, "test-model");
        assertEquals("ollama response", integration.processRequest("hello").get());
    }

    @Test
    void processRequestExecutesTextFunction() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 200,
                "{\"choices\":[{\"message\":{\"content\":\"**EXECUTE:** add_dependency(groupId=org.example, artifactId=demo, version=1.0.0)\"}}] }"));

        OllamaIntegration integration = new OllamaIntegration(baseUrl, "test-model");
        String response = integration.processRequest("add dependency").get();

        assertTrue(response.contains("Operação NetBeans executada com sucesso"), response);
        assertTrue(response.contains("org.example"), response);
    }

    @Test
    void processRequestReportsUnknownFunction() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 200,
                "{\"choices\":[{\"message\":{\"content\":\"**EXECUTE:** missing_function()\"}}] }"));

        OllamaIntegration integration = new OllamaIntegration(baseUrl, "test-model");
        String response = integration.processRequest("missing").get();
        assertTrue(response.contains("Erro ao executar função"));
        assertTrue(response.contains("Unknown function"));
    }

    @Test
    void processRequestHandlesServerError() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 500, "boom"));

        OllamaIntegration integration = new OllamaIntegration(baseUrl, "test-model");
        String response = integration.processRequest("hello").get();
        assertTrue(response.startsWith("❌ Erro:"));
        assertTrue(response.contains("500"));
    }

    @Test
    void malformedResponseProducesExtractionError() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 200, "{}"));

        OllamaIntegration integration = new OllamaIntegration(baseUrl, "test-model");
        String response = integration.processRequest("hello").get();
        assertTrue(response.contains("Erro ao extrair resposta"));
    }

    @Test
    void testConnectionSucceedsWithChoices() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 200,
                "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}"));

        OllamaIntegration integration = new OllamaIntegration(baseUrl, "test-model");
        assertTrue(integration.testConnection().get());
    }

    @Test
    void testConnectionReturnsFalseOnFailure() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 503, "unavailable"));

        OllamaIntegration integration = new OllamaIntegration(baseUrl, "test-model");
        assertFalse(integration.testConnection().get());
    }

    @Test
    void directFunctionExecutionWorks() throws Exception {
        OllamaIntegration integration = new OllamaIntegration(baseUrl, "test-model");
        var result = integration.executeFunction("add_dependency", Map.of(
                "groupId", "org.example", "artifactId", "demo", "version", "1.0.0")).get();
        assertTrue(result.isSuccess());
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
