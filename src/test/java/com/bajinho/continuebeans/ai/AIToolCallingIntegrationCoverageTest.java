package com.bajinho.continuebeans.ai;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.bajinho.continuebeans.ContinueSettings;
import com.google.gson.JsonArray;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Coverage tests for {@link AIToolCallingIntegration} targeting the red lines
 * identified in issue #119. Uses a real HttpServer to simulate provider responses
 * and real file operations (read_file/create_file) that need no NetBeans runtime.
 */
class AIToolCallingIntegrationCoverageTest {

    private static final String WORKSPACE_PROPERTY = "continuebeans.workspace";

    @TempDir
    Path tempDir;

    private HttpServer server;
    private String previousApiUrl;
    private String previousModel;
    private String previousWorkspaceProperty;

    @BeforeEach
    void setUp() throws IOException {
        previousApiUrl = ContinueSettings.getApiUrl();
        previousModel = ContinueSettings.getModel();
        previousWorkspaceProperty = System.getProperty(WORKSPACE_PROPERTY);
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        ContinueSettings.setApiUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1/chat/completions");
        ContinueSettings.setModel("coverage-test-model");
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        ContinueSettings.setApiUrl(previousApiUrl);
        ContinueSettings.setModel(previousModel);
        restoreWorkspaceProperty();
    }

    private void setWorkspaceRoot(Path root) throws IOException {
        System.setProperty(WORKSPACE_PROPERTY, root.toAbsolutePath().toString());
    }

    private void restoreWorkspaceProperty() {
        if (previousWorkspaceProperty == null) {
            System.clearProperty(WORKSPACE_PROPERTY);
        } else {
            System.setProperty(WORKSPACE_PROPERTY, previousWorkspaceProperty);
        }
    }

    // ------------------------------------------------------------------
    // String overload: null / blank / happy path
    // ------------------------------------------------------------------

    @Test
    void stringOverload_nullMessage_returnsError() {
        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling((String) null, "lmstudio")
                .join();
        assertEquals("error", response.getType());
        assertEquals("Mensagem do usuário é obrigatória.", response.getContent());
    }

    @Test
    void stringOverload_blankMessage_returnsError() {
        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("   ", "lmstudio")
                .join();
        assertEquals("error", response.getType());
        assertEquals("Mensagem do usuário é obrigatória.", response.getContent());
    }

    @Test
    void stringOverload_happyPath_delegatesToJsonArray() {
        server.createContext("/v1/chat/completions", exchange -> send(exchange,
                "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Olá!\"}}]}"));

        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Olá", "lmstudio")
                .join();
        assertEquals("text", response.getType());
        assertEquals("Olá!", response.getContent());
    }

    // ------------------------------------------------------------------
    // JsonArray overload: null / empty / non-object elements
    // ------------------------------------------------------------------

    @Test
    void jsonArrayOverload_nullConversation_returnsError() {
        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling((JsonArray) null, "lmstudio")
                .join();
        assertEquals("error", response.getType());
        assertEquals("Histórico de conversa é obrigatório.", response.getContent());
    }

    @Test
    void jsonArrayOverload_emptyConversation_returnsError() {
        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling(new JsonArray(), "lmstudio")
                .join();
        assertEquals("error", response.getType());
        assertEquals("Histórico de conversa é obrigatório.", response.getContent());
    }

    @Test
    void jsonArrayOverload_nonObjectElements_returnsInvalidHistory() {
        JsonArray conversation = new JsonArray();
        conversation.add(42); // non-object element → deep copy skipped
        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling(conversation, "lmstudio")
                .join();
        assertEquals("error", response.getType());
        assertEquals("Histórico de conversa inválido.", response.getContent());
    }

    // ------------------------------------------------------------------
    // Tool round loop: tool_calls driving executeTool paths
    // ------------------------------------------------------------------

    @Test
    void toolCall_readFile_success_returnsText() throws IOException {
        setWorkspaceRoot(tempDir);
        Path file = tempDir.resolve("hello.txt");
        Files.writeString(file, "content here");

        server.createContext("/v1/chat/completions", exchange -> {
            String body = read(exchange);
            if (body.contains("\"tool_call_id\"")) {
                // second call: final answer
                send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Arquivo lido com sucesso.\"}}]}");
            } else {
                // first call: tool call for read_file
                send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null," +
                        "\"tool_calls\":[{\"id\":\"call_1\",\"type\":\"function\"," +
                        "\"function\":{\"name\":\"read_file\",\"arguments\":\"{\\\"filePath\\\":\\\"hello.txt\\\"}\"}}]}}]}");
            }
        });

        AIToolCallingIntegration integration = new AIToolCallingIntegration(tempDir.toAbsolutePath().toString());
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Leia hello.txt", "lmstudio")
                .join();
        assertEquals("text", response.getType());
        assertEquals("Arquivo lido com sucesso.", response.getContent());
    }

    @Test
    void toolCall_buildProject_policyDenial_returnsError() {
        // build_project without -Dcontinuebeans.allowBuild=true → SecurityException
        server.createContext("/v1/chat/completions", exchange -> {
            String body = read(exchange);
            if (body.contains("\"tool_call_id\"")) {
                send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Build falhou.\"}}]}");
            } else {
                send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null," +
                        "\"tool_calls\":[{\"id\":\"call_2\",\"type\":\"function\"," +
                        "\"function\":{\"name\":\"build_project\",\"arguments\":\"{}\"}}]}}]}");
            }
        });

        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Build o projeto", "lmstudio")
                .join();
        assertEquals("text", response.getType());
    }

    @Test
    void toolCall_deleteFile_noConfirm_policyDenial() {
        // delete_file without confirm=true → SecurityException
        server.createContext("/v1/chat/completions", exchange -> {
            String body = read(exchange);
            if (body.contains("\"tool_call_id\"")) {
                send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Delete falhou.\"}}]}");
            } else {
                send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null," +
                        "\"tool_calls\":[{\"id\":\"call_3\",\"type\":\"function\"," +
                        "\"function\":{\"name\":\"delete_file\",\"arguments\":\"{\\\"filePath\\\":\\\"tmp.txt\\\",\\\"confirm\\\":false}\"}}]}}]}");
            }
        });

        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Delete tmp.txt", "lmstudio")
                .join();
        assertEquals("text", response.getType());
    }

    @Test
    void toolCall_unknownFunction_returnsError() {
        // unknown function → FunctionResult.error (not SecurityException)
        server.createContext("/v1/chat/completions", exchange -> {
            String body = read(exchange);
            if (body.contains("\"tool_call_id\"")) {
                send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Função desconhecida.\"}}]}");
            } else {
                send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null," +
                        "\"tool_calls\":[{\"id\":\"call_4\",\"type\":\"function\"," +
                        "\"function\":{\"name\":\"nonexistent_tool\",\"arguments\":\"{}\"}}]}}]}");
            }
        });

        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Chama tool desconhecida", "lmstudio")
                .join();
        assertEquals("text", response.getType());
    }

    // ------------------------------------------------------------------
    // callProvider: endpoint / model not configured, HTTP error, missing choices
    // ------------------------------------------------------------------

    @Test
    void callProvider_endpointNotConfigured_returnsError() {
        ContinueSettings.setApiUrl("");
        server.createContext("/v1/chat/completions", exchange -> send(exchange, "{}"));

        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Olá", "lmstudio")
                .join();
        assertEquals("error", response.getType());
        assertTrue(response.getContent().contains("Erro de integração AI"));
    }

    @Test
    void callProvider_modelNotConfigured_returnsError() {
        ContinueSettings.setModel("");
        server.createContext("/v1/chat/completions", exchange -> send(exchange, "{}"));

        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Olá", "lmstudio")
                .join();
        assertEquals("error", response.getType());
        assertTrue(response.getContent().contains("Erro de integração AI"));
    }

    @Test
    void callProvider_httpStatusError_returnsError() {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, 500, "Internal Server Error"));

        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Olá", "lmstudio")
                .join();
        assertEquals("error", response.getType());
        assertTrue(response.getContent().contains("Erro de integração AI"));
    }

    @Test
    void callProvider_missingChoices_returnsError() {
        server.createContext("/v1/chat/completions", exchange -> send(exchange, "{}"));

        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Olá", "lmstudio")
                .join();
        assertEquals("error", response.getType());
        assertTrue(response.getContent().contains("Erro de integração AI"));
    }

    // ------------------------------------------------------------------
    // workspaceRoot: relative path resolution
    // ------------------------------------------------------------------

    @Test
    void workspaceRoot_relativePathResolution() throws IOException {
        setWorkspaceRoot(tempDir);
        Path file = tempDir.resolve("relative.txt");
        Files.writeString(file, "relative content");

        server.createContext("/v1/chat/completions", exchange -> {
            String body = read(exchange);
            if (body.contains("\"tool_call_id\"")) {
                send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Caminho resolvido.\"}}]}");
            } else {
                send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":null," +
                        "\"tool_calls\":[{\"id\":\"call_5\",\"type\":\"function\"," +
                        "\"function\":{\"name\":\"read_file\",\"arguments\":\"{\\\"filePath\\\":\\\"relative.txt\\\"}\"}}]}}]}");
            }
        });

        AIToolCallingIntegration integration = new AIToolCallingIntegration(tempDir.toAbsolutePath().toString());
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Leia relative.txt", "lmstudio")
                .join();
        assertEquals("text", response.getType());
    }

    @Test
    void workspaceRoot_null_noResolution() {
        // No workspace root set → resolveRelativePathArguments is a no-op
        server.createContext("/v1/chat/completions", exchange -> send(exchange,
                "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Sem resolução.\"}}]}"));

        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling("Olá", "lmstudio")
                .join();
        assertEquals("text", response.getType());
    }

    // ------------------------------------------------------------------
    // public executeFunction: happy path + SecurityException catch
    // ------------------------------------------------------------------

    @Test
    void executeFunction_direct_readFile_success() throws IOException {
        setWorkspaceRoot(tempDir);
        Path file = tempDir.resolve("direct.txt");
        Files.writeString(file, "direct content");

        AIToolCallingIntegration integration = new AIToolCallingIntegration(tempDir.toAbsolutePath().toString());
        Map<String, Object> args = new HashMap<>();
        args.put("filePath", "direct.txt");

        NetBeansFunctionExecutor.FunctionResult result = integration.executeFunction("read_file", args).join();
        assertTrue(result.isSuccess());
    }

    @Test
    void executeFunction_policyDenial_returnsError() {
        // build_project without allowBuild → SecurityException caught in executeFunction
        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        Map<String, Object> args = new HashMap<>();

        NetBeansFunctionExecutor.FunctionResult result = integration.executeFunction("build_project", args).join();
        assertFalse(result.isSuccess());
    }

    // ------------------------------------------------------------------
    // getAvailableFunctions
    // ------------------------------------------------------------------

    @Test
    void getAvailableFunctions_returnsAll() {
        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        List<NetBeansFunctionDefinitions.FunctionDefinition> functions = integration.getAvailableFunctions();
        assertNotNull(functions);
        assertFalse(functions.isEmpty());
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    private static String read(HttpExchange exchange) throws IOException {
        try (java.io.InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static void send(HttpExchange exchange, String body) throws IOException {
        send(exchange, 200, body);
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
