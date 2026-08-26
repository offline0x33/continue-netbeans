package com.bajinho.continuebeans.ai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.bajinho.continuebeans.ContinueSettings;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class AIToolCallingConversationHistoryTest {
    private HttpServer server;
    private String previousApiUrl;
    private String previousModel;

    @BeforeEach
    void setUp() throws IOException {
        previousApiUrl = ContinueSettings.getApiUrl();
        previousModel = ContinueSettings.getModel();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();
        ContinueSettings.setApiUrl("http://127.0.0.1:" + server.getAddress().getPort() + "/v1/chat/completions");
        ContinueSettings.setModel("history-test-model");
    }

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
        ContinueSettings.setApiUrl(previousApiUrl);
        ContinueSettings.setModel(previousModel);
    }

    @Test
    void sendsPreviousTurnsAlongsideCurrentUserMessage() {
        AtomicReference<String> requestBody = new AtomicReference<>();
        server.createContext("/v1/chat/completions", exchange -> {
            requestBody.set(read(exchange));
            send(exchange, "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Seu nome é José.\"}}]}");
        });

        JsonArray conversation = new JsonArray();
        conversation.add(message("user", "Meu nome é José."));
        conversation.add(message("assistant", "Prazer, José."));
        conversation.add(message("user", "Qual é o meu nome?"));

        AIToolCallingIntegration integration = new AIToolCallingIntegration();
        AIToolCallingIntegration.AIResponse response = integration
                .processRequestWithToolCalling(conversation, "lmstudio")
                .join();

        assertEquals("text", response.getType());
        assertEquals("Seu nome é José.", response.getContent());
        JsonObject payload = JsonParser.parseString(requestBody.get()).getAsJsonObject();
        JsonArray messages = payload.getAsJsonArray("messages");
        assertEquals(3, messages.size());
        assertEquals("Meu nome é José.", messages.get(0).getAsJsonObject().get("content").getAsString());
        assertEquals("Prazer, José.", messages.get(1).getAsJsonObject().get("content").getAsString());
        assertEquals("Qual é o meu nome?", messages.get(2).getAsJsonObject().get("content").getAsString());
    }

    @Test
    void requestHistoryDoesNotGetMutatedByToolCalling() {
        server.createContext("/v1/chat/completions", exchange -> send(exchange,
                "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Resposta.\"}}]}"));

        JsonArray conversation = new JsonArray();
        conversation.add(message("user", "Olá"));
        String before = conversation.toString();

        new AIToolCallingIntegration()
                .processRequestWithToolCalling(conversation, "lmstudio")
                .join();

        assertTrue(conversation.toString().equals(before));
    }

    private static JsonObject message(String role, String content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        message.addProperty("content", content);
        return message;
    }

    private static String read(HttpExchange exchange) throws IOException {
        try (java.io.InputStream input = exchange.getRequestBody()) {
            return new String(input.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }

    private static void send(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }
}
