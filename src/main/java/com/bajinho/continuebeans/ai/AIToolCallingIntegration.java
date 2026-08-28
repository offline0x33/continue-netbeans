package com.bajinho.continuebeans.ai;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.bajinho.continuebeans.ContinueSettings;
import com.bajinho.continuebeans.security.ToolExecutionPolicy;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/** Real OpenAI-compatible tool calling integration backed by the task executor. */
public class AIToolCallingIntegration {

    private static final Logger LOG = Logger.getLogger(AIToolCallingIntegration.class.getName());
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(120);
    private static final String API_KEY_ENV = "CONTINUE_BEANS_API_KEY";
    private static final int MAX_TOOL_ROUNDS = 8;

    private final NetBeansFunctionDefinitions functionDefinitions;
    private final NetBeansFunctionExecutor functionExecutor;
    private final HttpClient httpClient;
    private final Gson gson;
    private volatile String workspaceRoot;

    public AIToolCallingIntegration() {
        this(null);
    }

    public AIToolCallingIntegration(String workspaceRoot) {
        this.functionDefinitions = new NetBeansFunctionDefinitions();
        this.functionExecutor = new NetBeansFunctionExecutor();
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(15))
                .build();
        this.gson = new Gson();
        this.workspaceRoot = workspaceRoot;
    }

    public void setWorkspaceRoot(String workspaceRoot) {
        this.workspaceRoot = workspaceRoot;
    }

    public CompletableFuture<AIResponse> processRequestWithToolCalling(String userMessage, String aiProvider) {
        if (userMessage == null || userMessage.isBlank()) {
            return CompletableFuture.completedFuture(AIResponse.error("Mensagem do usuário é obrigatória."));
        }
        JsonArray messages = new JsonArray();
        messages.add(message("user", userMessage));
        return processRequestWithToolCalling(messages, aiProvider);
    }

    /**
     * Processes one user-facing request using the supplied prior conversation history.
     * Tool-call assistant/tool messages are kept local to this request and are not persisted
     * into the caller's history unless the caller explicitly chooses to store the final answer.
     */
    public CompletableFuture<AIResponse> processRequestWithToolCalling(JsonArray conversation, String aiProvider) {
        if (conversation == null || conversation.size() == 0) {
            return CompletableFuture.completedFuture(AIResponse.error("Histórico de conversa é obrigatório."));
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                List<JsonObject> messages = new ArrayList<>();
                for (JsonElement element : conversation) {
                    if (element != null && element.isJsonObject()) {
                        messages.add(element.getAsJsonObject().deepCopy());
                    }
                }
                if (messages.isEmpty()) {
                    return AIResponse.error("Histórico de conversa inválido.");
                }

                for (int round = 1; round <= MAX_TOOL_ROUNDS; round++) {
                    JsonObject response = callProvider(messages);
                    JsonObject choiceMessage = response.getAsJsonArray("choices")
                            .get(0).getAsJsonObject().getAsJsonObject("message");
                    JsonArray toolCalls = choiceMessage.has("tool_calls")
                            ? choiceMessage.getAsJsonArray("tool_calls") : new JsonArray();

                    if (toolCalls.isEmpty()) {
                        return AIResponse.text(readContent(choiceMessage));
                    }

                    messages.add(choiceMessage.deepCopy());
                    for (JsonElement toolCallElement : toolCalls) {
                        JsonObject toolCall = toolCallElement.getAsJsonObject();
                        JsonObject function = toolCall.getAsJsonObject("function");
                        String functionName = function.get("name").getAsString();
                        String rawArguments = function.has("arguments")
                                ? function.get("arguments").getAsString() : "{}";
                        Map<String, Object> arguments = parseArguments(rawArguments);
                        resolveRelativePathArguments(arguments);
                        JsonObject toolMessage = executeTool(functionName, arguments, toolCall, aiProvider);
                        messages.add(toolMessage);
                    }
                    final int currentRound = round;
                    LOG.fine(() -> "Completed tool round " + currentRound + " for provider " + aiProvider);
                }

                return AIResponse.error("Limite de " + MAX_TOOL_ROUNDS + " ciclos de tools atingido.");
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Erro no ciclo de tool calling", e);
                return AIResponse.error("Erro de integração AI: " + safeMessage(e));
            }
        });
    }

    private void resolveRelativePathArguments(Map<String, Object> arguments) {
        if (arguments == null || workspaceRoot == null || workspaceRoot.isBlank()) {
            return;
        }

        resolvePathArgument(arguments, "filePath");
        resolvePathArgument(arguments, "directoryPath");
        resolvePathArgument(arguments, "projectPath");
        resolvePathArgument(arguments, "path");
    }

    private void resolvePathArgument(Map<String, Object> arguments, String key) {
        Object value = arguments.get(key);
        if (!(value instanceof String)) {
            return;
        }

        String pathValue = (String) value;
        if (pathValue.isBlank() || Path.of(pathValue).isAbsolute()) {
            return;
        }

        String resolved = Path.of(workspaceRoot).resolve(pathValue).normalize().toString();
        arguments.put(key, resolved);
        LOG.fine(() -> "Resolved relative workspace path '" + pathValue + "' to '" + resolved + "'");
    }

    private JsonObject executeTool(String functionName, Map<String, Object> arguments,
            JsonObject toolCall, String aiProvider) {
        String toolCallId = toolCall.has("id") ? toolCall.get("id").getAsString() : "unknown";
        try {
            ToolExecutionPolicy.validate(functionName, arguments);
            LOG.info(() -> "Executing AI tool: " + functionName + " via " + aiProvider);
            NetBeansFunctionExecutor.FunctionResult result = functionExecutor
                    .executeFunction(functionName, arguments).join();
            JsonObject toolMessage = message("tool", resultToJson(result));
            toolMessage.addProperty("tool_call_id", toolCallId);
            return toolMessage;
        } catch (SecurityException denied) {
            LOG.warning(() -> "AI tool blocked by policy: " + functionName + " - " + denied.getMessage());
            JsonObject toolMessage = message("tool", "ERROR: " + denied.getMessage());
            toolMessage.addProperty("tool_call_id", toolCallId);
            return toolMessage;
        } catch (Exception e) {
            LOG.log(Level.WARNING, "AI tool execution failed: " + functionName, e);
            JsonObject toolMessage = message("tool", "ERROR: " + safeMessage(e));
            toolMessage.addProperty("tool_call_id", toolCallId);
            return toolMessage;
        }
    }

    private JsonObject callProvider(List<JsonObject> messages) throws Exception {
        String endpoint = ContinueSettings.getApiUrl();
        String model = ContinueSettings.getModel();
        if (endpoint == null || endpoint.isBlank()) {
            throw new IllegalStateException("URL da API AI não configurada.");
        }
        if (model == null || model.isBlank()) {
            throw new IllegalStateException("Modelo AI não configurado.");
        }

        JsonObject request = new JsonObject();
        request.addProperty("model", model);
        request.add("messages", gson.toJsonTree(messages));
        request.addProperty("temperature", ContinueSettings.getTemperature());
        request.add("tools", buildTools());
        request.addProperty("tool_choice", "auto");

        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(REQUEST_TIMEOUT)
                .header("Content-Type", "application/json");
        String apiKey = System.getenv(API_KEY_ENV);
        if (apiKey != null && !apiKey.isBlank()) {
            builder.header("Authorization", "Bearer " + apiKey);
        }

        HttpResponse<String> response = httpClient.send(
                builder.POST(HttpRequest.BodyPublishers.ofString(gson.toJson(request))).build(),
                HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalStateException("AI API retornou HTTP " + response.statusCode()
                    + ": " + truncate(response.body(), 500));
        }

        JsonObject payload = JsonParser.parseString(response.body()).getAsJsonObject();
        if (!payload.has("choices") || payload.getAsJsonArray("choices").isEmpty()) {
            throw new IllegalStateException("Resposta AI sem choices.");
        }
        return payload;
    }

    private JsonArray buildTools() {
        JsonArray tools = new JsonArray();
        for (NetBeansFunctionDefinitions.FunctionDefinition function : NetBeansFunctionDefinitions.getAllFunctions()) {
            JsonObject tool = new JsonObject();
            tool.addProperty("type", "function");
            JsonObject definition = new JsonObject();
            definition.addProperty("name", function.getName());
            definition.addProperty("description", function.getDescription());
            definition.add("parameters", buildParameters(function.getParameters()));
            tool.add("function", definition);
            tools.add(tool);
        }
        return tools;
    }

    private JsonObject buildParameters(Map<String, Object> parameters) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        JsonObject properties = new JsonObject();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            JsonObject property = new JsonObject();
            String descriptor = String.valueOf(entry.getValue());
            property.addProperty("type", parameterType(descriptor));
            property.addProperty("description", descriptor);
            properties.add(entry.getKey(), property);
        }
        schema.add("properties", properties);
        return schema;
    }

    private String parameterType(String descriptor) {
        String normalized = descriptor == null ? "" : descriptor.toLowerCase();
        if (normalized.startsWith("boolean")) return "boolean";
        if (normalized.startsWith("integer")) return "integer";
        if (normalized.startsWith("number")) return "number";
        if (normalized.startsWith("array")) return "array";
        return "string";
    }

    private Map<String, Object> parseArguments(String rawArguments) {
        if (rawArguments == null || rawArguments.isBlank()) return new HashMap<>();
        JsonElement element = JsonParser.parseString(rawArguments);
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("Argumentos da tool call não são um objeto JSON.");
        }
        @SuppressWarnings("unchecked")
        Map<String, Object> map = gson.fromJson(element, Map.class);
        return map == null ? new HashMap<>() : map;
    }

    private JsonObject message(String role, Object content) {
        JsonObject message = new JsonObject();
        message.addProperty("role", role);
        if (content instanceof JsonObject) message.add("content", (JsonObject) content);
        else message.addProperty("content", String.valueOf(content));
        return message;
    }

    private String resultToJson(NetBeansFunctionExecutor.FunctionResult result) {
        JsonObject payload = new JsonObject();
        payload.addProperty("success", result.isSuccess());
        payload.addProperty("message", result.getMessage());
        payload.add("data", gson.toJsonTree(result.getData()));
        return gson.toJson(payload);
    }

    private String readContent(JsonObject message) {
        return message.has("content") && !message.get("content").isJsonNull()
                ? message.get("content").getAsString() : "A operação foi concluída.";
    }

    private String safeMessage(Exception e) {
        return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
    }

    private String truncate(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) return value;
        return value.substring(0, maxLength) + "…";
    }

    public List<NetBeansFunctionDefinitions.FunctionDefinition> getAvailableFunctions() {
        return NetBeansFunctionDefinitions.getAllFunctions();
    }

    public CompletableFuture<NetBeansFunctionExecutor.FunctionResult> executeFunction(
            String functionName, Map<String, Object> arguments) {
        try {
            resolveRelativePathArguments(arguments);
            ToolExecutionPolicy.validate(functionName, arguments);
            return functionExecutor.executeFunction(functionName, arguments);
        } catch (SecurityException denied) {
            return CompletableFuture.completedFuture(
                    NetBeansFunctionExecutor.FunctionResult.error(denied.getMessage()));
        }
    }

    public static class AIResponse {
        private final String type;
        private final String content;

        private AIResponse(String type, String content) {
            this.type = type;
            this.content = content;
        }

        public static AIResponse text(String content) {
            return new AIResponse("text", content);
        }

        public static AIResponse error(String error) {
            return new AIResponse("error", error);
        }

        public String getType() { return type; }
        public String getContent() { return content; }
    }
}
