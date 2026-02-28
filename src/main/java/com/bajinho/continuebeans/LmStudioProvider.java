package com.bajinho.continuebeans;

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
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class LmStudioProvider implements LlmProvider {

    private final HttpClient client;
    private final Gson gson;

    public LmStudioProvider(HttpClient client, Gson gson) {
        this.client = client;
        this.gson = gson;
    }

    private String resolveUrl(String url) {
        return UrlUtils.resolveUrl(url);
    }

    private static final List<JsonObject> conversationHistory = new ArrayList<>();

    @Override
    public void stream(String context, String prompt, String model, String mode,
            Consumer<String> onChunk, Consumer<Throwable> onError, Runnable onComplete) {
        streamWithRetry(context, prompt, model, mode, onChunk, onError, onComplete, true);
    }

    private void streamWithRetry(String context, String prompt, String model, String mode,
            Consumer<String> onChunk, Consumer<Throwable> onError, Runnable onComplete, boolean allowRetry) {

        String finalUrl = resolveUrl(ContinueSettings.getApiUrl());
        boolean isChatFormat = finalUrl.contains("/chat");

        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        payload.addProperty("temperature", ContinueSettings.getTemperature());
        payload.addProperty("stream", true);

        if (isChatFormat) {
            JsonArray messages = new JsonArray();

            // System Prompt
            JsonObject systemMessage = new JsonObject();
            systemMessage.addProperty("role", "system");
            String sysPrompt = "Você é um AI assistente avançado de programação profissional.";
            if ("Code".equalsIgnoreCase(mode))
                sysPrompt += " Foque em código limpo.";
            else if ("Planning".equalsIgnoreCase(mode))
                sysPrompt += " Planeje antes de codar.";
            systemMessage.addProperty("content", sysPrompt);
            messages.add(systemMessage);

            // Add History (Enterprise Rule)
            for (JsonObject msg : conversationHistory) {
                messages.add(msg);
            }

            // Current User Message
            JsonObject userMessage = new JsonObject();
            userMessage.addProperty("role", "user");
            String content = (context != null && !context.trim().isEmpty())
                    ? prompt + "\n\nContexto:\n```\n" + context + "\n```"
                    : prompt;
            userMessage.addProperty("content", content);
            messages.add(userMessage);

            payload.add("messages", messages);

            // Update History for next turn
            conversationHistory.add(userMessage);
            if (conversationHistory.size() > 10)
                conversationHistory.remove(0); // Basic truncation

        } else {
            StringBuilder promptBuilder = new StringBuilder();
            promptBuilder.append("### Instruction:\nVocê é um assistente de programação.\n\n");
            if (context != null && !context.trim().isEmpty()) {
                promptBuilder.append("Contexto:\n```\n").append(context).append("\n```\n\n");
            }
            promptBuilder.append("Questão: ").append(prompt).append("\n\n### Response:\n");
            payload.addProperty("prompt", promptBuilder.toString());
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofMinutes(5))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofLines())
                .thenAccept(response -> {
                    if (response.statusCode() == 429 && allowRetry) {
                        ContinueLogger.warn("Rate limit hit, retrying once...", null);
                        streamWithRetry(context, prompt, model, mode, onChunk, onError, onComplete, false);
                        return;
                    }

                    if (response.statusCode() != 200) {
                        onError.accept(new Exception("Erro HTTP " + response.statusCode() + " em " + finalUrl));
                        return;
                    }

                    StringBuilder fullContent = new StringBuilder();
                    response.body().forEach(line -> {
                        if (line.startsWith("data: ") && !line.contains("[DONE]")) {
                            try {
                                String json = line.substring(6).trim();
                                if (json.isEmpty())
                                    return;

                                JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                                if (obj.has("choices")) {
                                    JsonArray choices = obj.getAsJsonArray("choices");
                                    if (choices.size() > 0) {
                                        JsonObject choice = choices.get(0).getAsJsonObject();
                                        if (isChatFormat && choice.has("delta")) {
                                            JsonObject delta = choice.getAsJsonObject("delta");
                                            if (delta.has("content")) {
                                                String chunk = delta.get("content").getAsString();
                                                onChunk.accept(chunk);
                                                fullContent.append(chunk);
                                            }
                                        } else if (choice.has("text")) {
                                            String chunk = choice.get("text").getAsString();
                                            onChunk.accept(chunk);
                                            fullContent.append(chunk);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                ContinueLogger.warn("Failed to parse stream chunk: " + line, null);
                            }
                        }
                    });

                    // Add AI response to history
                    if (isChatFormat && fullContent.length() > 0) {
                        JsonObject aiMsg = new JsonObject();
                        aiMsg.addProperty("role", "assistant");
                        aiMsg.addProperty("content", fullContent.toString());
                        conversationHistory.add(aiMsg);
                    }

                    onComplete.run();
                })
                .exceptionally(ex -> {
                    onError.accept(ex);
                    return null;
                });
    }

    @Override
    public CompletableFuture<String> ask(String context, String prompt, String model, String mode) {
        JsonObject payload = new JsonObject();
        payload.addProperty("model", model);
        payload.addProperty("temperature", ContinueSettings.getTemperature());

        JsonArray messages = new JsonArray();
        String systemPrompt = "Você é um AI assistente avançado de programação.";
        if ("Code".equalsIgnoreCase(mode))
            systemPrompt += " Foque em código limpo.";
        else if ("Planning".equalsIgnoreCase(mode))
            systemPrompt += " Planeje antes de codar.";

        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", systemPrompt);
        messages.add(systemMessage);

        JsonObject userMessage = new JsonObject();
        userMessage.addProperty("role", "user");
        String content = (context != null && !context.trim().isEmpty())
                ? prompt + "\n\nContexto:\n```\n" + context + "\n```"
                : prompt;
        userMessage.addProperty("content", content);
        messages.add(userMessage);

        payload.add("messages", messages);
        payload.addProperty("stream", false);

        String finalUrl = resolveUrl(ContinueSettings.getApiUrl());

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(finalUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(60))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(payload)))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(response -> {
                    if (response.statusCode() != 200) {
                        return "Erro HTTP " + response.statusCode();
                    }
                    return extrairTexto(response.body());
                });
    }

    @Override
    public CompletableFuture<List<String>> listModels() {
        String baseUrl = ContinueSettings.getApiUrl();
        List<String> endpoints = new ArrayList<>();

        // Extract base host/port
        String rootUrl;
        if (baseUrl.contains("/v1/")) {
            rootUrl = baseUrl.substring(0, baseUrl.indexOf("/v1/"));
        } else if (baseUrl.contains(":1234")) {
            rootUrl = baseUrl.substring(0, baseUrl.indexOf(":1234") + 5);
        } else {
            // Fallback to trying to find the first slash after http://
            int thirdSlash = baseUrl.indexOf("/", 8);
            rootUrl = (thirdSlash != -1) ? baseUrl.substring(0, thirdSlash) : baseUrl;
        }

        endpoints.add(rootUrl + "/v1/models");
        endpoints.add(rootUrl + "/api/v1/models");

        ContinueLogger.info("Iniciando descoberta de modelos. Base: " + rootUrl);

        return tryEndpoints(endpoints, 0, new ArrayList<>());
    }

    private CompletableFuture<List<String>> tryEndpoints(List<String> endpoints, int index, List<String> lastModelos) {
        if (index >= endpoints.size()) {
            return CompletableFuture.completedFuture(lastModelos);
        }

        String url = resolveUrl(endpoints.get(index));
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenCompose(response -> {
                    if (response.statusCode() == 200) {
                        List<String> found = parseModels(response.body());
                        if (!found.isEmpty()) {
                            ContinueLogger.info("Modelos encontrados em " + url + ": " + found);
                            return CompletableFuture.completedFuture(found);
                        }
                    }
                    ContinueLogger.warn("Nenhum modelo em " + url + " (Status: " + response.statusCode() + ")", null);
                    return tryEndpoints(endpoints, index + 1, lastModelos);
                }).exceptionallyCompose(ex -> {
                    ContinueLogger.warn("Erro ao acessar " + url + ": " + ex.getMessage(), null);
                    return tryEndpoints(endpoints, index + 1, lastModelos);
                });
    }

    private List<String> parseModels(String body) {
        List<String> modelos = new ArrayList<>();
        try {
            JsonObject jsonObject = JsonParser.parseString(body).getAsJsonObject();
            // OpenAI Standard / LM Studio / local-llama
            if (jsonObject.has("data") && jsonObject.get("data").isJsonArray()) {
                JsonArray data = jsonObject.getAsJsonArray("data");
                for (int i = 0; i < data.size(); i++) {
                    JsonObject m = data.get(i).getAsJsonObject();
                    if (m.has("id"))
                        modelos.add(m.get("id").getAsString());
                }
            }
            // LM Studio specific older format
            else if (jsonObject.has("models") && jsonObject.get("models").isJsonArray()) {
                JsonArray models = jsonObject.getAsJsonArray("models");
                for (int i = 0; i < models.size(); i++) {
                    JsonObject m = models.get(i).getAsJsonObject();
                    if (m.has("id"))
                        modelos.add(m.get("id").getAsString());
                }
            }
        } catch (Exception e) {
            ContinueLogger.warn("Falha ao parsear JSON de modelos: " + e.getMessage(), null);
        }
        return modelos;
    }

    @Override
    public CompletableFuture<Boolean> loadModel(String modelId) {
        String baseUrl = ContinueSettings.getApiUrl();
        String rootUrl;
        if (baseUrl.contains("/v1/")) {
            rootUrl = baseUrl.substring(0, baseUrl.indexOf("/v1/"));
        } else if (baseUrl.contains(":1234")) {
            rootUrl = baseUrl.substring(0, baseUrl.indexOf(":1234") + 5);
        } else {
            int thirdSlash = baseUrl.indexOf("/", 8);
            rootUrl = (thirdSlash != -1) ? baseUrl.substring(0, thirdSlash) : baseUrl;
        }

        String loadUrl = resolveUrl(rootUrl + "/api/v1/models/load");
        JsonObject body = new JsonObject();
        body.addProperty("model", modelId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(loadUrl))
                .header("Content-Type", "application/json")
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(body)))
                .build();

        return client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(resp -> {
                    if (resp.statusCode() == 200) {
                        ContinueLogger.info("Modelo " + modelId + " carregado com sucesso.");
                        return true;
                    }
                    ContinueLogger.warn("Falha ao carregar modelo " + modelId + " (Status: " + resp.statusCode() + ")",
                            null);
                    return false;
                }).exceptionally(ex -> {
                    ContinueLogger.error("Erro ao carregar modelo " + modelId, ex);
                    return false;
                });
    }

    private String extrairTexto(String jsonResponse) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonResponse).getAsJsonObject();
            if (jsonObject.has("choices")) {
                return jsonObject.getAsJsonArray("choices")
                        .get(0).getAsJsonObject()
                        .getAsJsonObject("message")
                        .get("content").getAsString();
            }
            return "Resposta inesperada.";
        } catch (Exception e) {
            return "Erro ao processar JSON.";
        }
    }
}
