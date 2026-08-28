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
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LMStudioFunctionCallingIntegrationTest {
    private HttpServer server;
    private String baseUrl;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort();
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void processRequestReturnsNormalAssistantContent() throws Exception {
        server.createContext("/v1/chat/completions", e -> {
            assertEquals("POST", e.getRequestMethod());
            String request = body(e);
            assertTrue(request.contains("\"model\":\"test-model\""));
            assertTrue(request.contains("\"function_call\":\"auto\""));
            assertTrue(request.contains("\"stream\":false"));
            assertTrue(request.contains("\"temperature\":0.7"));
            assertTrue(request.contains("\"max_tokens\":2000"));
            assertTrue(request.contains("Hello"));
            send(e, 200, "{\"choices\":[{\"message\":{\"content\":\"hello from lm studio\"}}]}");
        });

        assertEquals("hello from lm studio",
                new LMStudioFunctionCallingIntegration(baseUrl, "test-model").processRequest("Hello").get());
    }

    @Test
    void processRequestExecutesFunctionCallAndFormatsResult() throws Exception {
        server.createContext("/v1/chat/completions", e -> send(e, 200,
                "{\"choices\":[{\"message\":{\"content\":\"I'll add it\",\"function_call\":{\"name\":\"add_dependency\",\"arguments\":\"{\\\"groupId\\\":\\\"org.example\\\",\\\"artifactId\\\":\\\"demo\\\",\\\"version\\\":\\\"1.0.0\\\"}\"}}}] }"));

        String result = new LMStudioFunctionCallingIntegration(baseUrl, "test-model")
                .processRequest("add dependency").get();

        assertTrue(result.contains("Função NetBeans executada com sucesso"));
        assertTrue(result.contains("groupId: org.example"));
        assertTrue(result.contains("artifactId: demo"));
    }

    @Test
    void functionErrorIsReported() throws Exception {
        server.createContext("/v1/chat/completions", e -> send(e, 200,
                "{\"choices\":[{\"message\":{\"content\":\"oops\",\"function_call\":{\"name\":\"missing_function\",\"arguments\":\"{}\"}}}] }"));

        String result = new LMStudioFunctionCallingIntegration(baseUrl, "test-model")
                .processRequest("do it").get();

        assertTrue(result.contains("Erro ao executar função"));
        assertTrue(result.contains("Unknown function"));
    }

    @Test
    void malformedResponseDoesNotEscapeAsException() throws Exception {
        server.createContext("/v1/chat/completions", e -> send(e, 200, "{}"));
        String result = new LMStudioFunctionCallingIntegration(baseUrl, "test-model")
                .processRequest("hello").get();
        assertTrue(result.contains("Erro ao extrair resposta"));
    }

    @Test
    void malformedFunctionArgumentsAreReported() throws Exception {
        server.createContext("/v1/chat/completions", e -> send(e, 200,
                "{\"choices\":[{\"message\":{\"content\":\"oops\",\"function_call\":{\"name\":\"add_dependency\",\"arguments\":\"not-json\"}}}] }"));
        String result = new LMStudioFunctionCallingIntegration(baseUrl, "test-model")
                .processRequest("do it").get();
        assertTrue(result.startsWith("❌ Erro na execução: "));
        assertTrue(result.contains("Expected BEGIN_OBJECT but was STRING"));
    }

    @Test
    void missingFunctionCallFieldsAreReported() throws Exception {
        server.createContext("/v1/chat/completions", e -> send(e, 200,
                "{\"choices\":[{\"message\":{\"content\":\"oops\",\"function_call\":{}}}] }"));
        String result = new LMStudioFunctionCallingIntegration(baseUrl, "test-model")
                .processRequest("do it").get();
        assertTrue(result.startsWith("❌ Erro na execução: "));
        assertTrue(result.contains("Cannot invoke"));
        assertTrue(result.contains("getAsString()"));
    }

    @Test
    void httpErrorIsReturnedAsDiagnostic() throws Exception {
        server.createContext("/v1/chat/completions", e -> send(e, 500, "failure"));
        String result = new LMStudioFunctionCallingIntegration(baseUrl, "test-model")
                .processRequest("hello").get();
        assertTrue(result.startsWith("❌ Erro:"));
        assertTrue(result.contains("500"));
        assertTrue(result.contains("failure"));
    }

    @Test
    void testConnectionReportsAvailability() throws Exception {
        server.createContext("/v1/chat/completions", e -> {
            String request = body(e);
            assertTrue(request.contains("\"model\":\"test-model\""));
            assertTrue(request.contains("\"content\":\"Hello\""));
            assertTrue(request.contains("\"max_tokens\":10"));
            send(e, 200, "{\"choices\":[{\"message\":{\"content\":\"ok\"}}]}");
        });

        assertTrue(new LMStudioFunctionCallingIntegration(baseUrl + "/", "test-model")
                .testConnection().get());
    }

    @Test
    void testConnectionReturnsFalseOnFailure() throws Exception {
        server.createContext("/v1/chat/completions", e -> send(e, 503, "unavailable"));
        assertFalse(new LMStudioFunctionCallingIntegration(baseUrl, "test-model")
                .testConnection().get());
    }

    @Test
    void availableFunctionsAreExposedWithStableMetadata() {
        List<NetBeansFunctionDefinitions.FunctionDefinition> functions =
                new LMStudioFunctionCallingIntegration(baseUrl, "test-model").getAvailableFunctions();
        assertNotNull(functions);
        assertFalse(functions.isEmpty());
        var dependency = functions.stream()
                .filter(f -> "add_dependency".equals(f.getName()))
                .findFirst()
                .orElseThrow();
        assertNotNull(dependency.getDescription());
        assertFalse(dependency.getDescription().isBlank());
        assertNotNull(dependency.getParameters());
        assertFalse(dependency.getParameters().isEmpty());
    }

    @Test
    void executeFunctionDelegatesSuccessfulOperation() throws Exception {
        var result = new LMStudioFunctionCallingIntegration(baseUrl, "test-model")
                .executeFunction("add_dependency",
                        Map.of("groupId", "org.example", "artifactId", "demo", "version", "1.0.0"))
                .get();
        assertTrue(result.isSuccess());
        assertEquals("org.example", result.getData().get("groupId"));
        assertEquals("demo", result.getData().get("artifactId"));
    }

    @Test
    void availableModelsReturnsParsedIds() throws Exception {
        server.createContext("/v1/models", e -> {
            assertEquals("GET", e.getRequestMethod());
            send(e, 200, "{\"data\":[{\"id\":\"model-a\"},{\"id\":\"model-b\"}]}");
        });
        List<String> models = new LMStudioFunctionCallingIntegration(baseUrl + "/", "test-model")
                .getAvailableModels().get();
        assertEquals(List.of("model-a", "model-b"), models);
    }

    @Test
    void availableModelsReturnsDiagnosticOnHttpError() throws Exception {
        server.createContext("/v1/models", e -> send(e, 503, "unavailable"));
        List<String> models = new LMStudioFunctionCallingIntegration(baseUrl, "test-model")
                .getAvailableModels().get();
        assertEquals(1, models.size());
        assertTrue(models.get(0).startsWith("Error: Failed to get models: 503"));
    }

    @Test
    void availableModelsReturnsDiagnosticOnMalformedJson() throws Exception {
        server.createContext("/v1/models", e -> send(e, 200, "not-json"));
        List<String> models = new LMStudioFunctionCallingIntegration(baseUrl, "test-model")
                .getAvailableModels().get();
        assertEquals(1, models.size());
        assertTrue(models.get(0).startsWith("Error: "));
    }

    private static String body(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange exchange, int status, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
