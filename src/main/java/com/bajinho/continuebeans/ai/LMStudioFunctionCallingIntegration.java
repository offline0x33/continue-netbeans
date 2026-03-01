package com.bajinho.continuebeans.ai;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * LM Studio Function Calling Integration - REAL connection.
 * Connects LM Studio with NetBeans function execution.
 * 
 * @author Continue Beans Team
 */
public class LMStudioFunctionCallingIntegration {
    
    private static final Logger LOG = Logger.getLogger(LMStudioFunctionCallingIntegration.class.getName());
    
    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;
    private final Gson gson;
    private final NetBeansFunctionExecutor functionExecutor;
    
    public LMStudioFunctionCallingIntegration(String baseUrl, String model) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.gson = new Gson();
        this.functionExecutor = new NetBeansFunctionExecutor();
    }
    
    /**
     * Process user request with REAL LM Studio function calling.
     * 
     * @param userMessage User message
     * @return AI response with executed functions
     */
    public CompletableFuture<String> processRequest(String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("Processing request with LM Studio: " + userMessage);
                
                // 1. Create LM Studio request with functions
                JsonObject request = createLMStudioRequest(userMessage);
                
                // 2. Send to LM Studio API (REAL HTTP call)
                JsonObject response = callLMStudioAPI(request);
                
                // 3. Parse response for function calls
                if (hasFunctionCall(response)) {
                    return handleFunctionCall(response);
                }
                
                // 4. Return text response
                return extractContent(response);
                
            } catch (Exception e) {
                LOG.severe("Error processing request: " + e.getMessage());
                return "❌ Erro: " + e.getMessage();
            }
        });
    }
    
    /**
     * Create LM Studio request with NetBeans function definitions.
     */
    private JsonObject createLMStudioRequest(String userMessage) {
        JsonObject request = new JsonObject();
        
        // Add model
        request.addProperty("model", model);
        
        // Add messages
        JsonArray messages = new JsonArray();
        
        // System message with context
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", AISystemContext.getSystemPromptContext());
        messages.add(systemMessage);
        
        // User message
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);
        
        request.add("messages", messages);
        
        // Add functions
        request.add("functions", getLMStudioFunctionDefinitions());
        request.addProperty("function_call", "auto");
        
        // Add parameters
        request.addProperty("temperature", 0.7);
        request.addProperty("max_tokens", 2000);
        request.addProperty("stream", false);
        
        return request;
    }
    
    /**
     * Convert NetBeans functions to LM Studio format.
     */
    private JsonArray getLMStudioFunctionDefinitions() {
        var netBeansFunctions = NetBeansFunctionDefinitions.getAllFunctions();
        JsonArray functions = new JsonArray();
        
        for (var function : netBeansFunctions) {
            JsonObject functionDef = new JsonObject();
            functionDef.addProperty("name", function.getName());
            functionDef.addProperty("description", function.getDescription());
            functionDef.add("parameters", convertParameters(function.getParameters()));
            functions.add(functionDef);
        }
        
        return functions;
    }
    
    /**
     * Convert parameters to LM Studio schema format.
     */
    private JsonObject convertParameters(Map<String, Object> parameters) {
        JsonObject schema = new JsonObject();
        schema.addProperty("type", "object");
        
        JsonObject properties = new JsonObject();
        JsonArray required = new JsonArray();
        
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            String paramDesc = entry.getValue().toString();
            
            JsonObject paramSchema = new JsonObject();
            paramSchema.addProperty("type", "string");
            paramSchema.addProperty("description", paramDesc);
            
            properties.add(paramName, paramSchema);
            required.add(paramName);
        }
        
        schema.add("properties", properties);
        schema.add("required", required);
        
        return schema;
    }
    
    /**
     * Call LM Studio API (REAL HTTP request).
     */
    private JsonObject callLMStudioAPI(JsonObject request) throws Exception {
        String url = baseUrl + "/v1/chat/completions";
        
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .header("Content-Type", "application/json")
            .timeout(Duration.ofSeconds(60))
            .POST(HttpRequest.BodyPublishers.ofString(request.toString()))
            .build();
        
        LOG.info("Sending request to LM Studio: " + url);
        
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        
        if (response.statusCode() != 200) {
            throw new RuntimeException("LM Studio API error: " + response.statusCode() + " - " + response.body());
        }
        
        LOG.info("LM Studio response received");
        return gson.fromJson(response.body(), JsonObject.class);
    }
    
    /**
     * Check if response contains function call.
     */
    private boolean hasFunctionCall(JsonObject response) {
        try {
            JsonObject choice = response.getAsJsonArray("choices").get(0).getAsJsonObject();
            JsonObject message = choice.getAsJsonObject("message");
            return message.has("function_call");
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Extract content from response.
     */
    private String extractContent(JsonObject response) {
        try {
            JsonObject choice = response.getAsJsonArray("choices").get(0).getAsJsonObject();
            JsonObject message = choice.getAsJsonObject("message");
            return message.get("content").getAsString();
        } catch (Exception e) {
            return "❌ Erro ao extrair resposta: " + e.getMessage();
        }
    }
    
    /**
     * Handle function call from LM Studio response.
     */
    private String handleFunctionCall(JsonObject response) {
        try {
            // Extract function call
            JsonObject choice = response.getAsJsonArray("choices").get(0).getAsJsonObject();
            JsonObject message = choice.getAsJsonObject("message");
            JsonObject functionCall = message.getAsJsonObject("function_call");
            
            String functionName = functionCall.get("name").getAsString();
            String argumentsStr = functionCall.get("arguments").getAsString();
            
            // Parse arguments
            Map<String, Object> arguments = gson.fromJson(argumentsStr, Map.class);
            
            LOG.info("Executing function: " + functionName + " with args: " + arguments);
            
            // Execute the function
            var result = functionExecutor.executeFunction(functionName, arguments).get();
            
            if (result.isSuccess()) {
                return formatSuccessResponse(message.get("content").getAsString(), result);
            } else {
                return "❌ Erro ao executar função: " + result.getMessage();
            }
            
        } catch (Exception e) {
            LOG.severe("Error handling function call: " + e.getMessage());
            return "❌ Erro na execução: " + e.getMessage();
        }
    }
    
    /**
     * Format success response with function results.
     */
    private String formatSuccessResponse(String aiMessage, NetBeansFunctionExecutor.FunctionResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(aiMessage).append("\n\n");
        sb.append("✅ **Função NetBeans executada com sucesso!**\n\n");
        sb.append("**Resultado:** ").append(result.getMessage()).append("\n\n");
        
        if (!result.getData().isEmpty()) {
            sb.append("**Detalhes:**\n");
            for (Map.Entry<String, Object> entry : result.getData().entrySet()) {
                sb.append("- ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }
        }
        
        return sb.toString();
    }
    
    /**
     * Test connection to LM Studio.
     */
    public CompletableFuture<Boolean> testConnection() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                JsonObject testRequest = new JsonObject();
                testRequest.addProperty("model", model);
                
                JsonArray messages = new JsonArray();
                JsonObject message = new JsonObject();
                message.addProperty("role", "user");
                message.addProperty("content", "Hello");
                messages.add(message);
                testRequest.add("messages", messages);
                testRequest.addProperty("max_tokens", 10);
                
                JsonObject response = callLMStudioAPI(testRequest);
                return response.has("choices");
                
            } catch (Exception e) {
                LOG.severe("Connection test failed: " + e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Get available models from LM Studio.
     */
    public CompletableFuture<List<String>> getAvailableModels() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = baseUrl + "/v1/models";
                
                HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(10))
                    .GET()
                    .build();
                
                HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
                
                if (response.statusCode() == 200) {
                    JsonObject modelsResponse = gson.fromJson(response.body(), JsonObject.class);
                    JsonArray models = modelsResponse.getAsJsonArray("data");
                    
                    List<String> modelNames = new java.util.ArrayList<>();
                    for (JsonElement model : models) {
                        String modelName = model.getAsJsonObject().get("id").getAsString();
                        modelNames.add(modelName);
                    }
                    
                    return modelNames;
                } else {
                    throw new RuntimeException("Failed to get models: " + response.statusCode());
                }
                
            } catch (Exception e) {
                LOG.severe("Error getting models: " + e.getMessage());
                return List.of("Error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Execute a single function directly.
     */
    public CompletableFuture<NetBeansFunctionExecutor.FunctionResult> executeFunction(String functionName, Map<String, Object> arguments) {
        return functionExecutor.executeFunction(functionName, arguments);
    }
    
    /**
     * Get available functions for external use.
     */
    public List<NetBeansFunctionDefinitions.FunctionDefinition> getAvailableFunctions() {
        return NetBeansFunctionDefinitions.getAllFunctions();
    }
}
