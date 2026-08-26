package com.bajinho.continuebeans.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.bajinho.continuebeans.LlmClient;
import com.bajinho.continuebeans.ai.AIToolCallingIntegration;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.http.HttpClient;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class TaskOrchestratorTest {
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void plannerParsesTasksAndDependencies() {
        TaskPlanner planner = new TaskPlanner(HttpClient.newHttpClient(), "http://unused", "test");
        TaskPlan plan = planner.parsePlan("goal", "{\"tasks\":["
                + "{\"title\":\"Criar\",\"instruction\":\"criar\",\"completionCriteria\":\"existe\",\"dependsOn\":[]},"
                + "{\"title\":\"Validar\",\"instruction\":\"validar\",\"completionCriteria\":\"ok\",\"dependsOn\":[0]}]}");

        assertEquals(2, plan.getTasks().size());
        assertEquals(plan.getTasks().get(0).getId(), plan.getTasks().get(1).getDependencies().get(0));
        assertEquals(plan.getTasks().get(0), plan.nextRunnableTask());
    }

    @Test
    void plannerRejectsEmptyTaskList() {
        TaskPlanner planner = new TaskPlanner(HttpClient.newHttpClient(), "http://unused", "test");
        assertThrows(IllegalStateException.class,
                () -> planner.parsePlan("goal", "{\"tasks\":[]}"));
    }

    @Test
    void orchestratorAnswersConversationalMessageWithoutPlannerRetries() {
        LlmClient classifier = mock(LlmClient.class);
        when(classifier.shouldUseTaskOrchestrator(anyString())).thenReturn(false);
        RecordingAgent agent = new RecordingAgent(new String[] {"Olá! Como posso ajudar?"});
        TaskOrchestrator orchestrator = new TaskOrchestrator(
                new TaskPlanner(HttpClient.newHttpClient(), "http://unused", "test"), agent, classifier);
        RecordingListener listener = new RecordingListener();

        TaskPlan plan = orchestrator.executeGoal("Olá", "test", listener).join();

        assertTrue(plan.isComplete());
        assertEquals(1, plan.getTasks().size());
        assertEquals(TaskStatus.DONE, plan.getTasks().get(0).getStatus());
        assertEquals("Olá! Como posso ajudar?", plan.getTasks().get(0).getLastResult());
        assertEquals(1, agent.calls.get());
        assertEquals(1, listener.completed.size());
    }

    @Test
    void orchestratorCompletesAllTasksInDependencyOrder() throws Exception {
        String body = planJson(2);
        startPlannerServer(body);
        RecordingAgent agent = new RecordingAgent(new String[] {"DONE", "DONE", "DONE", "DONE"});
        TaskOrchestrator orchestrator = new TaskOrchestrator(
                new TaskPlanner(HttpClient.newHttpClient(), serverUrl(), "test"), agent);
        RecordingListener listener = new RecordingListener();

        TaskPlan plan = orchestrator.executeGoal("concluir objetivo", "test", listener).join();

        assertTrue(plan.isComplete());
        assertEquals(TaskStatus.DONE, plan.getTasks().get(0).getStatus());
        assertEquals(TaskStatus.DONE, plan.getTasks().get(1).getStatus());
        assertEquals(1, plan.getTasks().get(0).getAttempts());
        assertEquals(1, plan.getTasks().get(1).getAttempts());
        assertEquals(2, listener.completed.size());
        assertEquals(4, agent.calls.get());
    }

    @Test
    void orchestratorRetriesTaskWhenVerificationFails() throws Exception {
        startPlannerServer(planJson(1));
        RecordingAgent agent = new RecordingAgent(new String[] {"DONE", "NOT_DONE", "DONE", "DONE"});
        TaskOrchestrator orchestrator = new TaskOrchestrator(
                new TaskPlanner(HttpClient.newHttpClient(), serverUrl(), "test"), agent);

        TaskPlan plan = orchestrator.executeGoal("corrigir", "test", new RecordingListener()).join();

        assertTrue(plan.isComplete());
        assertEquals(TaskStatus.DONE, plan.getTasks().get(0).getStatus());
        assertEquals(2, plan.getTasks().get(0).getAttempts());
        assertEquals(4, agent.calls.get());
    }

    @Test
    void taskLifecycleGuardsInvalidTransitions() {
        AgentTask task = new AgentTask("t", "i", "c", List.of());
        task.start();
        task.verifying("resultado");
        assertEquals(TaskStatus.VERIFYING, task.getStatus());
        assertThrows(IllegalStateException.class, task::start);
        task.complete("ok");
        assertEquals(TaskStatus.DONE, task.getStatus());
        assertThrows(IllegalStateException.class, task::start);
    }

    private void startPlannerServer(String content) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> send(exchange, plannerResponse(content)));
        server.start();
    }

    private String serverUrl() {
        return "http://127.0.0.1:" + server.getAddress().getPort() + "/";
    }

    private static void send(HttpExchange exchange, String body) throws IOException {
        byte[] bytes = body.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream output = exchange.getResponseBody()) {
            output.write(bytes);
        }
    }

    private static String plannerResponse(String tasksJson) {
        JsonObject message = new JsonObject();
        message.addProperty("role", "assistant");
        message.addProperty("content", tasksJson);
        JsonArray choices = new JsonArray();
        JsonObject choice = new JsonObject();
        choice.add("message", message);
        choices.add(choice);
        JsonObject response = new JsonObject();
        response.add("choices", choices);
        return response.toString();
    }

    private static String planJson(int taskCount) {
        String first = "{\"title\":\"Executar\",\"instruction\":\"execute\",\"completionCriteria\":\"DONE\",\"dependsOn\":[]}";
        if (taskCount == 1) {
            return "{\"tasks\":[" + first + "]}";
        }
        String second = "{\"title\":\"Verificar\",\"instruction\":\"verify\",\"completionCriteria\":\"DONE\",\"dependsOn\":[0]}";
        return "{\"tasks\":[" + first + "," + second + "]}";
    }

    private static final class RecordingAgent extends AIToolCallingIntegration {
        private final AtomicInteger calls = new AtomicInteger();
        private final String[] results;

        private RecordingAgent(String[] results) {
            this.results = results;
        }

        @Override
        public CompletableFuture<AIResponse> processRequestWithToolCalling(String userMessage, String provider) {
            return nextResponse();
        }

        @Override
        public CompletableFuture<AIResponse> processRequestWithToolCalling(JsonArray messages, String provider) {
            return nextResponse();
        }

        private CompletableFuture<AIResponse> nextResponse() {
            int index = calls.getAndIncrement();
            String result = results[Math.min(index, results.length - 1)];
            return CompletableFuture.completedFuture(AIResponse.text(result));
        }
    }

    private static final class RecordingListener implements TaskOrchestrator.Listener {
        private final List<AgentTask> completed = new ArrayList<>();

        @Override public void onPlanCreated(TaskPlan plan) { }
        @Override public void onTaskStarted(AgentTask task) { }
        @Override public void onTaskVerifying(AgentTask task) { }
        @Override public void onTaskCompleted(AgentTask task) { completed.add(task); }
        @Override public void onTaskFailed(AgentTask task) { }
        @Override public void onReplanning(TaskPlan failedPlan) { }
        @Override public void onCompleted(TaskPlan plan) { }
        @Override public void onFailed(String message, TaskPlan plan) { }
    }
}
