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
 * LM Studio Text-Based Integration - Works with any model.
 * Uses text instructions instead of function calling.
 * 
 * @author Continue Beans Team
 */
public class LMStudioTextIntegration {
    
    private static final Logger LOG = Logger.getLogger(LMStudioTextIntegration.class.getName());
    
    private final String baseUrl;
    private final String model;
    private final HttpClient httpClient;
    private final Gson gson;
    private final NetBeansFunctionExecutor functionExecutor;
    
    public LMStudioTextIntegration(String baseUrl, String model) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(30))
            .build();
        this.gson = new Gson();
        this.functionExecutor = new NetBeansFunctionExecutor();
    }
    
    /**
     * Process user request with text-based function execution.
     */
    public CompletableFuture<String> processRequest(String userMessage) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("Processing request with text-based integration: " + userMessage);
                
                // 1. Create enhanced system prompt
                JsonObject request = createTextRequest(userMessage);
                
                // 2. Send to LM Studio
                JsonObject response = callLMStudioAPI(request);
                
                // 3. Parse response for function instructions
                String aiResponse = extractContent(response);
                
                // 4. Check if AI wants to execute functions
                if (shouldExecuteFunction(aiResponse)) {
                    return handleTextBasedFunction(aiResponse);
                }
                
                return aiResponse;
                
            } catch (Exception e) {
                LOG.severe("Error processing request: " + e.getMessage());
                return "❌ Erro: " + e.getMessage();
            }
        });
    }
    
    /**
     * Create request with text-based function instructions.
     */
    private JsonObject createTextRequest(String userMessage) {
        JsonObject request = new JsonObject();
        
        request.addProperty("model", model);
        
        JsonArray messages = new JsonArray();
        
        // Enhanced system message
        JsonObject systemMessage = new JsonObject();
        systemMessage.addProperty("role", "system");
        systemMessage.addProperty("content", createTextSystemPrompt());
        messages.add(systemMessage);
        
        // User message
        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage);
        messages.add(userMsg);
        
        request.add("messages", messages);
        request.addProperty("temperature", 0.7);
        request.addProperty("max_tokens", 2000);
        request.addProperty("stream", false);
        
        return request;
    }
    
    /**
     * Create system prompt that teaches AI to use text-based function calls.
     */
    private String createTextSystemPrompt() {
        return "You are an AI assistant with FULL CONTROL of NetBeans Platform. " +
               "You can execute real operations on the project.\n\n" +
               "🚀 **HOW TO EXECUTE OPERATIONS:**\n" +
               "When you need to perform an operation, use this EXACT format:\n\n" +
               "**EXECUTE:** function_name(parameter1=value1, parameter2=value2)\n\n" +
               "📋 **AVAILABLE OPERATIONS:**\n" +
               "- create_file(filePath, content) - Create any file\n" +
               "- read_file(filePath) - Read file contents\n" +
               "- generate_class(className, packageName) - Generate Java class\n" +
               "- get_project_info() - Get project information\n" +
               "- get_active_windows() - List NetBeans windows\n\n" +
               "🎯 **EXAMPLES:**\n" +
               "User: 'Create hello world python'\n" +
               "AI: 'I'll create a Python hello world file for you.\n\n" +
               "**EXECUTE:** create_file(filePath=hello_world.py, content=print(\"Hello, World!\"))'\n\n" +
               "User: 'Create UserService class'\n" +
               "AI: 'I'll generate a UserService class for you.\n\n" +
               "**EXECUTE:** generate_class(className=UserService, packageName=com.example)'\n\n" +
               "🔧 **IMPORTANT:** Always use the EXECUTE format for operations. " +
               "After execution, I'll show you the real results.";
    }
    
    /**
     * Call LM Studio API.
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
     * Check if AI wants to execute a function.
     */
    private boolean shouldExecuteFunction(String response) {
        return response.contains("**EXECUTE:**");
    }
    
    /**
     * Handle text-based function call.
     */
    private String handleTextBasedFunction(String aiResponse) {
        try {
            // Extract function call
            String executeLine = aiResponse.lines()
                .filter(line -> line.contains("**EXECUTE:**"))
                .findFirst()
                .orElse("");
            
            if (executeLine.isEmpty()) {
                return aiResponse;
            }
            
            // Parse function call - extract only after **EXECUTE:**
            String functionCall = executeLine.replaceAll(".*\\*\\*EXECUTE:\\*\\*", "").trim();
            String functionName = functionCall.substring(0, functionCall.indexOf("(")).trim();
            
            // Find the closing parenthesis that matches the opening one
            int parenCount = 0;
            int endIndex = functionCall.indexOf("(");
            for (int i = endIndex; i < functionCall.length(); i++) {
                if (functionCall.charAt(i) == '(') {
                    parenCount++;
                } else if (functionCall.charAt(i) == ')') {
                    parenCount--;
                    if (parenCount == 0) {
                        endIndex = i;
                        break;
                    }
                }
            }
            
            String paramsStr = functionCall.substring(functionCall.indexOf("(") + 1, endIndex).trim();
            
            // Parse parameters
            Map<String, Object> parameters = parseParameters(paramsStr);
            
            LOG.info("Executing function: " + functionName + " with params: " + parameters);
            
            // Execute the function
            var result = functionExecutor.executeFunction(functionName, parameters).get();
            
            if (result.isSuccess()) {
                return formatSuccessResponse(aiResponse, result);
            } else {
                return "❌ Erro ao executar função: " + result.getMessage();
            }
            
        } catch (Exception e) {
            LOG.severe("Error handling function call: " + e.getMessage());
            return "❌ Erro na execução: " + e.getMessage();
        }
    }
    
    /**
     * Parse parameters from string.
     */
    private Map<String, Object> parseParameters(String paramsStr) {
        Map<String, Object> params = new HashMap<>();
        
        if (paramsStr.isEmpty()) {
            return params;
        }
        
        // Parse manually to handle quoted strings correctly
        int i = 0;
        while (i < paramsStr.length()) {
            // Skip whitespace
            while (i < paramsStr.length() && Character.isWhitespace(paramsStr.charAt(i))) {
                i++;
            }
            
            if (i >= paramsStr.length()) break;
            
            // Parse key
            int keyStart = i;
            while (i < paramsStr.length() && paramsStr.charAt(i) != '=') {
                i++;
            }
            String key = paramsStr.substring(keyStart, i).trim();
            
            // Skip '='
            i++;
            
            // Parse value
            if (i >= paramsStr.length()) break;
            
            String value;
            char quoteChar = paramsStr.charAt(i);
            if (quoteChar == '"' || quoteChar == '\'') {
                // Quoted string - handle both single and double quotes
                i++; // Skip opening quote
                StringBuilder valueBuilder = new StringBuilder();
                while (i < paramsStr.length()) {
                    char c = paramsStr.charAt(i);
                    if (c == '\\') {
                        // Handle escaped character
                        i++;
                        if (i < paramsStr.length()) {
                            valueBuilder.append(paramsStr.charAt(i));
                            i++;
                        }
                    } else if (c == quoteChar) {
                        // End of quoted string
                        i++;
                        break;
                    } else {
                        valueBuilder.append(c);
                        i++;
                    }
                }
                value = valueBuilder.toString();
            } else {
                // Unquoted value
                int valueStart = i;
                while (i < paramsStr.length() && paramsStr.charAt(i) != ',' && paramsStr.charAt(i) != ')') {
                    i++;
                }
                value = paramsStr.substring(valueStart, i).trim();
            }
            
            params.put(key, value);
            
            // Skip comma or whitespace
            while (i < paramsStr.length() && (paramsStr.charAt(i) == ',' || Character.isWhitespace(paramsStr.charAt(i)))) {
                i++;
            }
        }
        
        return params;
    }
    
    /**
     * Format success response.
     */
    private String formatSuccessResponse(String aiResponse, NetBeansFunctionExecutor.FunctionResult result) {
        StringBuilder sb = new StringBuilder();
        
        // Remove EXECUTE line from AI response
        String cleanedResponse = aiResponse.lines()
            .filter(line -> !line.contains("**EXECUTE:**"))
            .collect(java.util.stream.Collectors.joining("\n"));
        
        sb.append(cleanedResponse).append("\n\n");
        sb.append("✅ **Operação NetBeans executada com sucesso!**\n\n");
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
     * Test connection.
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
     * Execute a single function directly.
     */
    public CompletableFuture<NetBeansFunctionExecutor.FunctionResult> executeFunction(String functionName, Map<String, Object> arguments) {
        return functionExecutor.executeFunction(functionName, arguments);
    }
}
