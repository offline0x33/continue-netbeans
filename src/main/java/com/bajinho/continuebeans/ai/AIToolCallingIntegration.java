package com.bajinho.continuebeans.ai;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * AI Tool Calling Integration - Connects OpenAI Function Calling with NetBeans.
 * This class integrates the AI model with NetBeans function definitions and executor.
 * 
 * @author Continue Beans Team
 */
public class AIToolCallingIntegration {
    
    private static final Logger LOG = Logger.getLogger(AIToolCallingIntegration.class.getName());
    
    private final NetBeansFunctionDefinitions functionDefinitions;
    private final NetBeansFunctionExecutor functionExecutor;
    
    public AIToolCallingIntegration() {
        this.functionDefinitions = new NetBeansFunctionDefinitions();
        this.functionExecutor = new NetBeansFunctionExecutor();
    }
    
    /**
     * Process AI request with tool calling support.
     * 
     * @param userMessage User message to AI
     * @param aiProvider AI provider (openai, claude, gemini)
     * @return AI response with function calls executed
     */
    public CompletableFuture<AIResponse> processRequestWithToolCalling(String userMessage, String aiProvider) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("Processing AI request with tool calling for provider: " + aiProvider);
                
                // 1. Get available functions
                List<NetBeansFunctionDefinitions.FunctionDefinition> functions = functionDefinitions.getAllFunctions();
                
                // 2. Format functions for AI provider
                String formattedFunctions = formatFunctionsForAI(functions, aiProvider);
                
                // 3. Create AI request with functions
                Map<String, Object> aiRequest = createAIRequest(userMessage, formattedFunctions, aiProvider);
                
                // 4. Simulate AI response (in real implementation would call actual AI)
                AIResponse aiResponse = simulateAIResponse(userMessage, functions);
                
                // 5. Execute function calls if present
                if (aiResponse.hasFunctionCalls()) {
                    AIResponse finalResponse = executeFunctionCalls(aiResponse);
                    return finalResponse;
                }
                
                return aiResponse;
                
            } catch (Exception e) {
                LOG.severe("Error processing AI request: " + e.getMessage());
                return AIResponse.error("Processing error: " + e.getMessage());
            }
        });
    }
    
    /**
     * Format function definitions for different AI providers.
     */
    private String formatFunctionsForAI(List<NetBeansFunctionDefinitions.FunctionDefinition> functions, String provider) {
        StringBuilder sb = new StringBuilder();
        sb.append("Available NetBeans functions:\n\n");
        
        for (NetBeansFunctionDefinitions.FunctionDefinition function : functions) {
            sb.append("Function: ").append(function.getName()).append("\n");
            sb.append("Description: ").append(function.getDescription()).append("\n");
            sb.append("Parameters: ").append(formatParameters(function.getParameters())).append("\n");
            sb.append("Returns: ").append(function.getReturns()).append("\n\n");
        }
        
        return sb.toString();
    }
    
    private String formatParameters(Map<String, Object> parameters) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            sb.append(entry.getKey()).append(" (").append(entry.getValue()).append("), ");
        }
        if (sb.length() > 2) {
            sb.setLength(sb.length() - 2); // Remove trailing comma
        }
        return sb.toString();
    }
    
    /**
     * Create AI request with functions.
     */
    private Map<String, Object> createAIRequest(String userMessage, String formattedFunctions, String provider) {
        Map<String, Object> request = new HashMap<>();
        request.put("message", userMessage);
        request.put("functions", formattedFunctions);
        request.put("provider", provider);
        request.put("enableFunctionCalling", true);
        return request;
    }
    
    /**
     * Simulate AI response with function calls.
     * In real implementation, this would call the actual AI provider.
     */
    private AIResponse simulateAIResponse(String userMessage, List<NetBeansFunctionDefinitions.FunctionDefinition> functions) {
        // Simple simulation - detect what user wants and suggest function calls
        String lowerMessage = userMessage.toLowerCase();
        
        if (lowerMessage.contains("criar classe") || lowerMessage.contains("generate class")) {
            return AIResponse.withFunctionCall(
                "Vou criar uma classe para você.",
                "generate_class",
                Map.of(
                    "className", "NovaClasse",
                    "packageName", "com.example"
                )
            );
        }
        
        if (lowerMessage.contains("ler arquivo") || lowerMessage.contains("read file")) {
            return AIResponse.withFunctionCall(
                "Vou ler o arquivo solicitado.",
                "read_file",
                Map.of(
                    "filePath", "src/main/java/com/example/Main.java"
                )
            );
        }
        
        if (lowerMessage.contains("projeto") || lowerMessage.contains("project")) {
            return AIResponse.withFunctionCall(
                "Vou obter informações do projeto.",
                "get_project_info",
                Map.of()
            );
        }
        
        if (lowerMessage.contains("janela") || lowerMessage.contains("window")) {
            return AIResponse.withFunctionCall(
                "Vou listar as janelas ativas.",
                "get_active_windows",
                Map.of()
            );
        }
        
        // Default response without function calls
        return AIResponse.text("Sou um assistente AI com acesso completo ao NetBeans Platform. " +
            "Posso ler/criar arquivos, gerar código, analisar projetos e muito mais. " +
            "O que você gostaria que eu faça?");
    }
    
    /**
     * Execute function calls from AI response.
     */
    private AIResponse executeFunctionCalls(AIResponse aiResponse) {
        try {
            String functionName = aiResponse.getFunctionName();
            Map<String, Object> arguments = aiResponse.getFunctionArguments();
            
            LOG.info("Executing function: " + functionName);
            
            // Execute the function
            var functionResult = functionExecutor.executeFunction(functionName, arguments).get();
            
            if (functionResult.isSuccess()) {
                String successMessage = formatSuccessMessage(aiResponse.getContent(), functionResult);
                return AIResponse.text(successMessage);
            } else {
                return AIResponse.error("Function execution failed: " + functionResult.getMessage());
            }
            
        } catch (Exception e) {
            LOG.severe("Error executing function calls: " + e.getMessage());
            return AIResponse.error("Function execution error: " + e.getMessage());
        }
    }
    
    /**
     * Format success message with function results.
     */
    private String formatSuccessMessage(String aiMessage, NetBeansFunctionExecutor.FunctionResult result) {
        StringBuilder sb = new StringBuilder();
        sb.append(aiMessage).append("\n\n");
        sb.append("✅ **Função executada com sucesso!**\n\n");
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
     * Get available functions for external use.
     */
    public List<NetBeansFunctionDefinitions.FunctionDefinition> getAvailableFunctions() {
        return functionDefinitions.getAllFunctions();
    }
    
    /**
     * Execute a single function directly.
     */
    public CompletableFuture<NetBeansFunctionExecutor.FunctionResult> executeFunction(String functionName, Map<String, Object> arguments) {
        return functionExecutor.executeFunction(functionName, arguments);
    }
    
    /**
     * AI Response class
     */
    public static class AIResponse {
        private final String type; // "text" or "function_call"
        private final String content;
        private final String functionName;
        private final Map<String, Object> functionArguments;
        
        private AIResponse(String type, String content, String functionName, Map<String, Object> functionArguments) {
            this.type = type;
            this.content = content;
            this.functionName = functionName;
            this.functionArguments = functionArguments != null ? functionArguments : new HashMap<>();
        }
        
        public static AIResponse text(String content) {
            return new AIResponse("text", content, null, null);
        }
        
        public static AIResponse withFunctionCall(String content, String functionName, Map<String, Object> arguments) {
            return new AIResponse("function_call", content, functionName, arguments);
        }
        
        public static AIResponse error(String error) {
            return new AIResponse("error", error, null, null);
        }
        
        // Getters
        public String getType() { return type; }
        public String getContent() { return content; }
        public boolean hasFunctionCalls() { return "function_call".equals(type); }
        public String getFunctionName() { return functionName; }
        public Map<String, Object> getFunctionArguments() { return functionArguments; }
    }
}
