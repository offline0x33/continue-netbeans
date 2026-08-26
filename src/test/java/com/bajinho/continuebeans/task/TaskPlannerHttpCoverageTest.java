package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

class TaskPlannerHttpCoverageTest {
    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void createPlanParsesJsonAndMarkdownFence() throws Exception {
        startServer(200, "{\"choices\":[{\"message\":{\"content\":\"```json\\n{\\\"tasks\\\":[{\\\"title\\\":\\\"Criar\\\",\\\"instruction\\\":\\\"criar\\\",\\\"completionCriteria\\\":\\\"feito\\\",\\\"dependsOn\\\":[]},{\\\"title\\\":\\\"Validar\\\",\\\"instruction\\\":\\\"validar\\\",\\\"completionCriteria\\\":\\\"ok\\\",\\\"dependsOn\\\":[0]}]}\\n```\"}}]}");
        TaskPlan plan = new TaskPlanner(HttpClient.newHttpClient(), serverUrl(), "model").createPlan("goal");
        assertEquals(2, plan.getTasks().size());
        assertEquals(plan.getTasks().get(0).getId(), plan.getTasks().get(1).getDependencies().get(0));
    }

    @Test
    void createPlanFallsBackOnHttpErrorAndInvalidResponse() throws Exception {
        startServer(500, "planner unavailable");
        TaskPlan fallback = new TaskPlanner(HttpClient.newHttpClient(), serverUrl(), "model").createPlan("goal");
        assertEquals(1, fallback.getTasks().size());
        assertEquals("Executar objetivo solicitado", fallback.getTasks().get(0).getTitle());

        server.stop(0);
        startServer(200, "not-json");
        TaskPlan invalidFallback = new TaskPlanner(HttpClient.newHttpClient(), serverUrl(), "model").createPlan("goal");
        assertEquals(1, invalidFallback.getTasks().size());
    }

    @Test
    void emptyEndpointOrModelFailsFast() {
        assertThrows(IllegalStateException.class,
                () -> new TaskPlanner(HttpClient.newHttpClient(), "", "model").createPlan("goal"));
        assertThrows(IllegalStateException.class,
                () -> new TaskPlanner(HttpClient.newHttpClient(), "http://127.0.0.1:1", "").createPlan("goal"));
    }

    @Test
    void normalizeChatEndpointCoversVariants() {
        TaskPlanner planner = new TaskPlanner(HttpClient.newHttpClient(), "http://unused", "model");
        assertEquals("http://127.0.0.1:1234/v1/chat/completions",
                planner.normalizeChatEndpoint("http://localhost:1234/v1/"));
        assertEquals("http://example.test/v1/chat/completions",
                planner.normalizeChatEndpoint("http://example.test"));
        assertEquals("http://example.test/v1/chat/completions",
                planner.normalizeChatEndpoint("http://example.test/v1"));
        assertEquals("http://example.test/chat/completions",
                planner.normalizeChatEndpoint("http://example.test/chat/completions/"));
    }

    private void startServer(int status, String body) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> send(exchange, status, body));
        server.start();
    }

    private String serverUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/chat/completions";
    }

    private static void send(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream out = exchange.getResponseBody()) {
            out.write(bytes);
        }
    }
}
