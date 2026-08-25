package com.bajinho.continuebeans.task;

import com.bajinho.continuebeans.ContinueSettings;
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
import java.util.List;

/** Creates an explicit, ordered and verifiable task plan from the user's goal. */
public final class TaskPlanner {
    private static final Duration TIMEOUT = Duration.ofSeconds(90);
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
    private final Gson gson = new Gson();

    public TaskPlan createPlan(String goal) throws Exception {
        String endpoint = ContinueSettings.getApiUrl();
        String model = ContinueSettings.getModel();
        if (endpoint == null || endpoint.isBlank() || model == null || model.isBlank()) {
            throw new IllegalStateException("Provider AI não configurado para planejamento.");
        }

        JsonObject request = new JsonObject();
        request.addProperty("model", model);
        request.addProperty("temperature", 0.1);
        JsonArray messages = new JsonArray();
        JsonObject system = new JsonObject();
        system.addProperty("role", "system");
        system.addProperty("content", """
                Você é o planejador de tarefas de um agente de desenvolvimento dentro do NetBeans.
                Transforme o objetivo do usuário em tarefas concretas, ordenadas e verificáveis.
                Responda SOMENTE JSON válido neste formato:
                {"tasks":[{"title":"...","instruction":"...","completionCriteria":"...","dependsOn":[]}]}
                Regras:
                - pelo menos uma tarefa;
                - cada tarefa deve ser executável por um agente com ferramentas;
                - completionCriteria precisa ser observável no projeto/IDE;
                - crie tarefas de validação (testes/build/inspeção) antes da tarefa final quando necessário;
                - dependsOn referencia o índice zero-based das tarefas anteriores;
                - não declare o objetivo geral concluído sem uma tarefa explícita de verificação.
                """);
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
            throw new IllegalStateException("Falha no planejador HTTP " + response.statusCode());
        }

        JsonObject payload = JsonParser.parseString(response.body()).getAsJsonObject();
        String content = payload.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString();
        return parsePlan(goal, content);
    }

    private TaskPlan parsePlan(String goal, String content) {
        String json = content.trim();
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start >= 0 && end > start) {
            json = json.substring(start, end + 1);
        }
        JsonArray taskArray = JsonParser.parseString(json).getAsJsonObject().getAsJsonArray("tasks");
        if (taskArray == null || taskArray.isEmpty()) {
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
                for (var dependency : item.getAsJsonArray("dependsOn")) {
                    indexes.add(dependency.getAsInt());
                }
            }
            tasks.add(new AgentTask(title, instruction, criteria, List.of()));
            dependencyIndexes.add(indexes);
        }

        List<AgentTask> linked = new ArrayList<>();
        for (int i = 0; i < tasks.size(); i++) {
            List<String> ids = new ArrayList<>();
            for (Integer index : dependencyIndexes.get(i)) {
                if (index != null && index >= 0 && index < tasks.size()) {
                    ids.add(tasks.get(index).getId());
                }
            }
            AgentTask original = tasks.get(i);
            linked.add(new AgentTask(original.getId(), original.getTitle(), original.getInstruction(),
                    original.getCompletionCriteria(), ids));
        }
        return new TaskPlan(goal, linked);
    }
}
