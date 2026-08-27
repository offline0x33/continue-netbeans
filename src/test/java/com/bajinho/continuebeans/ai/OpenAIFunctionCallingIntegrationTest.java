package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OpenAIFunctionCallingIntegrationTest {

    @Test
    void exposesAvailableFunctions() {
        OpenAIFunctionCallingIntegration integration = new OpenAIFunctionCallingIntegration("test-key", "test-model");
        List<NetBeansFunctionDefinitions.FunctionDefinition> functions = integration.getAvailableFunctions();

        assertNotNull(functions);
        assertFalse(functions.isEmpty());
        assertTrue(functions.stream().anyMatch(f -> "read_file".equals(f.getName())));
    }

    @Test
    void directFunctionExecutionUsesRealExecutor() throws Exception {
        OpenAIFunctionCallingIntegration integration = new OpenAIFunctionCallingIntegration("test-key", "test-model");
        NetBeansFunctionExecutor.FunctionResult result = integration.executeFunction(
                "add_dependency", Map.of("groupId", "org.example", "artifactId", "demo", "version", "1.0.0"))
                .get();

        assertTrue(result.isSuccess());
        assertEquals("org.example", result.getData().get("groupId"));
    }

    @Test
    void processRequestReturnsDefaultResponseForUnrecognizedIntent() throws Exception {
        OpenAIFunctionCallingIntegration integration = new OpenAIFunctionCallingIntegration("test-key", "test-model");
        String response = integration.processRequest("Tell me something unrelated").get();

        assertTrue(response.contains("assistente AI"));
        assertTrue(response.contains("NetBeans"));
    }

    @Test
    void processRequestHandlesCreateClassIntentAndExecutesFunction() throws Exception {
        OpenAIFunctionCallingIntegration integration = new OpenAIFunctionCallingIntegration("test-key", "test-model");
        String response = integration.processRequest("criar classe").get();

        assertTrue(response.contains("Função executada com sucesso"), response);
        assertTrue(response.contains("Class generated successfully"), response);
        assertTrue(response.contains("UserService"), response);
    }

    @Test
    void processRequestHandlesReadFileIntentAndReturnsErrorForMissingFile() throws Exception {
        OpenAIFunctionCallingIntegration integration = new OpenAIFunctionCallingIntegration("test-key", "test-model");
        String response = integration.processRequest("ler arquivo").get();

        assertTrue(response.contains("Erro ao executar função"), response);
        assertTrue(response.contains("File not found"), response);
    }

    @Test
    void processRequestHandlesProjectIntent() throws Exception {
        OpenAIFunctionCallingIntegration integration = new OpenAIFunctionCallingIntegration("test-key", "test-model");
        String response = integration.processRequest("projeto").get();

        assertTrue(response.contains("Erro ao executar função") || response.contains("Função executada"), response);
    }

    @Test
    void processRequestHandlesEnglishIntent() throws Exception {
        OpenAIFunctionCallingIntegration integration = new OpenAIFunctionCallingIntegration("test-key", "test-model");
        String response = integration.processRequest("generate class").get();

        assertTrue(response.contains("UserService"), response);
    }

    @Test
    void processRequestHandlesEnglishReadIntent() throws Exception {
        OpenAIFunctionCallingIntegration integration = new OpenAIFunctionCallingIntegration("test-key", "test-model");
        String response = integration.processRequest("read file").get();

        assertTrue(response.contains("File not found"), response);
    }
}
