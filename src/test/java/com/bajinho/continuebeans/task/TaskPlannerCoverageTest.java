package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TaskPlannerCoverageTest {

    private HttpServer server;

    @AfterEach
    void tearDown() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void normalizesBaseAndV1Endpoints() {
        TaskPlanner planner = new TaskPlanner(HttpClient.newHttpClient(), "unused", "model");
        assertEquals("http://127.0.0.1:1234/v1/chat/completions",
                planner.normalizeChatEndpoint("http://127.0.0.1:1234"));
        assertEquals("http://127.0.0.1:1234/v1/chat/completions",
                planner.normalizeChatEndpoint("http://localhost:1234/v1/"));
        assertEquals("http://127.0.0.1:1234/v1/chat/completions",
                planner.normalizeChatEndpoint("http://localhost:1234/v1/chat/completions/"));
    }

    @Test
    void parsesJsonEmbeddedInMarkdownAndLinksDependencies() {
        TaskPlanner planner = new TaskPlanner(HttpClient.newHttpClient(), "unused", "model");
        String json = "```json\n{\"tasks\":["
                + "{\"title\":\"Criar\",\"instruction\":\"criar\",\"completionCriteria\":\"ok\",\"dependsOn\":[]},"
                + "{\"title\":\"Validar\",\"instruction\":\"validar\",\"completionCriteria\":\"ok\",\"dependsOn\":[0]}]}\n```";

        TaskPlan plan = planner.parsePlan("goal", json);

        assertEquals(2, plan.getTasks().size());
        assertEquals(plan.getTasks().get(0).getId(), plan.getTasks().get(1).getDependsOn().get(0));
    }

    @Test
    void rejectsEmptyTaskList() {
        TaskPlanner planner = new TaskPlanner(HttpClient.newHttpClient(), "unused", "model");
        assertThrows(IllegalStateException.class,
                () -> planner.parsePlan("goal", "{\"tasks\":[]}"));
    }

    @Test
    void createsFallbackPlanWhenPlannerReturnsHttp500() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/v1/chat/completions", this::respond500);
        server.start();

        TaskPlanner planner = new TaskPlanner(HttpClient.newHttpClient(),
                "http://127.0.0.1:" + server.getAddress().getPort() + "/v1/chat/completions", "model");

        TaskPlan plan = planner.createPlan("analisar projeto");

        assertEquals(1, plan.getTasks().size());
        assertEquals("Executar objetivo solicitado", plan.getTasks().get(0).getTitle());
    }

    @Test
    void rejectsMissingModelOrEndpointBeforeHttpCall() {
        TaskPlanner missingEndpoint = new TaskPlanner(HttpClient.newHttpClient(), "", "model");
        TaskPlanner missingModel = new TaskPlanner(HttpClient.newHttpClient(), "http://127.0.0.1:1", "");

        assertThrows(IllegalStateException.class, () -> missingEndpoint.createPlan("goal"));
        assertThrows(IllegalStateException.class, () -> missingModel.createPlan("goal"));
    }

    private void respond500(HttpExchange exchange) throws java.io.IOException {
        byte[] body = "planner unavailable".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(500, body.length);
        try (java.io.OutputStream output = exchange.getResponseBody()) {
            output.write(body);
        }
    }
}
