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

class LMStudioFunctionCallingIntegrationTest {

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
    void processRequestReturnsPlainTextResponse() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> {
            String request = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(request.contains("\"model\":\"test-model\""));
            assertTrue(request.contains("\"function_call\":\"auto\""));
            assertTrue(request.contains("\"functions\""));
            send(exchange, 200, "{\"choices\":[{\"message\":{\"content\":\"plain response\"}}]}");
        });

        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        assertEquals("plain response", integration.processRequest("hello").get());
    }

    @Test
    void processRequestExecutesFunctionCall() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 200,
                "{\"choices\":[{\"message\":{\"content\":\"Creating dependency\",\"function_call\":{\"name\":\"add_dependency\",\"arguments\":\"{\\\"groupId\\\":\\\"org.example\\\",\\\"artifactId\\\":\\\"demo\\\",\\\"version\\\":\\\"1.0.0\\\"}\"}}}]}}"));

        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        String response = integration.processRequest("add dependency").get();

        assertTrue(response.contains("Função NetBeans executada com sucesso"), response);
        assertTrue(response.contains("org.example"), response);
        assertTrue(response.contains("demo"), response);
    }

    @Test
    void processRequestReportsUnknownFunction() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 200,
                "{\"choices\":[{\"message\":{\"content\":\"x\",\"function_call\":{\"name\":\"missing_function\",\"arguments\":\"{}\"}}}] }"));

        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        String response = integration.processRequest("run missing").get();

        assertTrue(response.contains("Erro ao executar função"), response);
        assertTrue(response.contains("Unknown function"), response);
    }

    @Test
    void processRequestHandlesHttpError() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 500, "boom"));

        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        String response = integration.processRequest("hello").get();

        assertTrue(response.startsWith("❌ Erro:"));
        assertTrue(response.contains("500"));
    }

    @Test
    void malformedResponseFallsBackToExtractionError() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 200, "{}"));

        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        String response = integration.processRequest("hello").get();

        assertTrue(response.contains("Erro ao extrair resposta"));
    }

    @Test
    void testConnectionSucceedsWhenChoicesExist() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 200,
                "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}"));

        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        assertTrue(integration.testConnection().get());
    }

    @Test
    void testConnectionReturnsFalseOnServerError() throws Exception {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 503, "unavailable"));

        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        assertFalse(integration.testConnection().get());
    }

    @Test
    void getAvailableModelsParsesIds() throws Exception {
        server.createContext("/v1/models", exchange -> send(exchange, 200,
                "{\"data\":[{\"id\":\"model-a\"},{\"id\":\"model-b\"}]}"));

        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        assertEquals(java.util.List.of("model-a", "model-b"), integration.getAvailableModels().get());
    }

    @Test
    void getAvailableModelsReturnsErrorEntryOnHttpFailure() throws Exception {
        server.createContext("/v1/models", exchange -> send(exchange, 404, "not found"));

        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        java.util.List<String> models = integration.getAvailableModels().get();
        assertEquals(1, models.size());
        assertTrue(models.get(0).startsWith("Error:"));
    }

    @Test
    void directFunctionExecutionWorks() throws Exception {
        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        var result = integration.executeFunction("add_dependency", Map.of(
                "groupId", "org.example", "artifactId", "demo", "version", "1.0.0")).get();
        assertTrue(result.isSuccess());
    }

    @Test
    void availableFunctionsAreExposed() {
        LMStudioFunctionCallingIntegration integration = new LMStudioFunctionCallingIntegration(baseUrl, "test-model");
        assertFalse(integration.getAvailableFunctions().isEmpty());
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
