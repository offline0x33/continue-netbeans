package com.bajinho.continuebeans.task;

import com.bajinho.continuebeans.ContinueLogger;
import com.bajinho.continuebeans.ContinueSettings;
import com.bajinho.continuebeans.UrlUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Creates an explicit, ordered and verifiable task plan from the user's goal. */
public final class TaskPlanner {
    private static final Duration TIMEOUT = Duration.ofMinutes(5);
    private static final int ERROR_BODY_LIMIT = 600;
    private final HttpClient client;
    private final Gson gson = new Gson();
    private final String configuredEndpoint;
    private final String configuredModel;

    public TaskPlanner() {
        this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build(), null, null);
    }

    TaskPlanner(HttpClient client, String endpoint, String model) {
        this.client = client;
        this.configuredEndpoint = endpoint;
        this.configuredModel = model;
    }

    public TaskPlan createPlan(String goal) throws Exception {
        String rawEndpoint = configuredEndpoint == null ? ContinueSettings.getApiUrl() : configuredEndpoint;
        String model = configuredModel == null ? ContinueSettings.getModel() : configuredModel;
        if (rawEndpoint == null || rawEndpoint.isBlank() || model == null || model.isBlank()) {
            throw new IllegalStateException("Provider AI não configurado para planejamento.");
        }

        String endpoint = normalizeChatEndpoint(rawEndpoint);
        try {
            JsonObject request = new JsonObject();
            request.addProperty("model", model);
            request.addProperty("temperature", 0.1);
            request.addProperty("stream", false);

            JsonArray messages = new JsonArray();
            JsonObject system = new JsonObject();
            system.addProperty("role", "system");
            system.addProperty("content", "Você é o planejador de tarefas de um agente de desenvolvimento dentro do NetBeans. "
                    + "Transforme o objetivo do usuário em tarefas concretas, ordenadas e verificáveis. "
                    + "Responda SOMENTE JSON válido no formato de tasks com title, instruction, completionCriteria e dependsOn. "
                    + "Cada tarefa deve ser executável e ter critério observável. "
                    + "dependsOn referencia índice zero-based e o plano precisa terminar com uma tarefa de verificação. "
                    + "Nunca use dependência para uma tarefa que não exista.");
            messages.add(system);

            JsonObject user = new JsonObject();
            user.addProperty("role", "user");
            user.addProperty("content", goal);
            messages.add(user);
            request.add("messages", messages);

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .timeout(TIMEOUT)
                    .header("Content-Type", "application/json");
            String key = System.getenv("CONTINUE_BEANS_API_KEY");
            if (key != null && !key.isBlank()) {
                builder.header("Authorization", "Bearer " + key);
            }

            HttpResponse<String> response = client.send(
                    builder.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request))).build(),
                    HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("Falha no planejador HTTP " + response.statusCode()
                        + ": " + truncate(response.body(), ERROR_BODY_LIMIT));
            }

            JsonObject payload = JsonParser.parseString(response.body()).getAsJsonObject();
            String content = payload.getAsJsonArray("choices").get(0).getAsJsonObject()
                    .getAsJsonObject("message").get("content").getAsString();
            return parsePlan(goal, content);
        } catch (Exception e) {
            ContinueLogger.warn("Planejador estruturado indisponível; usando plano direto. Motivo: "
                    + safeMessage(e), e);
            return createFallbackPlan(goal);
        }
    }

    String normalizeChatEndpoint(String endpoint) {
        String normalized = UrlUtils.resolveUrl(endpoint).trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        if (normalized.endsWith("/chat/completions")) {
            return normalized;
        }
        if (normalized.endsWith("/v1") || normalized.endsWith("/api/v1")) {
            return normalized + "/chat/completions";
        }
        return normalized + "/v1/chat/completions";
    }

    private TaskPlan createFallbackPlan(String goal) {
        AgentTask task = new AgentTask(
                "Executar objetivo solicitado",
                goal,
                "O objetivo solicitado foi executado e verificado no projeto.",
                Collections.<String>emptyList());
        return new TaskPlan(goal, Collections.singletonList(task));
    }

    private String safeMessage(Exception e) {
        String message = e.getMessage();
        return message == null || message.isBlank() ? e.getClass().getSimpleName() : truncate(message, ERROR_BODY_LIMIT);
    }

    private String truncate(String value, int limit) {
        if (value == null || value.length() <= limit) {
            return value;
        }
        return value.substring(0, limit) + "...";
    }

    TaskPlan parsePlan(String goal, String content) {
        String json = content.trim();
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start >= 0 && end > start) {
            json = json.substring(start, end + 1);
        }
        JsonArray taskArray = JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("tasks");
        if (taskArray == null || taskArray.size() == 0) {
            throw new IllegalStateException("Planejador não retornou tarefas.");
        }

        List<AgentTask> tasks = new ArrayList<>();
        List<List<Integer>> dependencyIndexes = new ArrayList<>();
        for (int i = 0; i < taskArray.size(); i++) {
            JsonObject item = taskArray.get(i).getAsJsonObject();
            String title = item.get("title").getAsString();
            String instruction = item.get("instruction").getAsString();
            String criteria = item.get("completionCriteria").getAsString();
            List<Integer> indexes = new ArrayList<>();
            if (item.has("dependsOn")) {
                for (com.google.gson.JsonElement dependency : item.getAsJsonArray("dependsOn")) {
                    indexes.add(dependency.getAsInt());
                }
            }
            tasks.add(new AgentTask(title, instruction, criteria, Collections.<String>emptyList()));
            dependencyIndexes.add(indexes);
        }

        List<AgentTask> linked = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            List<String> ids = new ArrayList<>();
            for (Integer index : dependencyIndexes.get(i)) {
                if (index == null || index < 0 || index >= tasks.size()) {
                    throw new IllegalStateException("Dependência inválida na tarefa " + (i + 1)
                            + ": índice " + index + " não existe no plano.");
                }
                if (index >= i) {
                    throw new IllegalStateException("Dependência inválida na tarefa " + (i + 1)
                            + ": depende de uma tarefa futura (" + index + ").");
                }
                ids.add(tasks.get(index).getId());
            }
            AgentTask original = tasks.get(i);
            linked.add(new AgentTask(original.getId(), original.getTitle(), original.getInstruction(),
                    original.getCompletionCriteria(), ids));
        }
        return new TaskPlan(goal, linked);
    }
}
