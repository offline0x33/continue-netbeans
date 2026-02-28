package com.bajinho.continuebeans;

import org.junit.jupiter.api.Test;
import java.net.http.HttpClient;
import com.google.gson.Gson;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test to verify the model parsing fix
 */
class ModelParsingTest {

    @Test
    void testParseModelsOpenAIFormat() {
        LmStudioProvider provider = new LmStudioProvider(HttpClient.newHttpClient(), new Gson());
        
        // Test with OpenAI format response (without loaded_instances)
        String response = "{\n" +
            "  \"data\": [\n" +
            "    {\n" +
            "      \"id\": \"google/gemma-3-4b\",\n" +
            "      \"object\": \"model\",\n" +
            "      \"owned_by\": \"organization_owner\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"qwen3-coder-30b-a3b-instruct\",\n" +
            "      \"object\": \"model\",\n" +
            "      \"owned_by\": \"organization_owner\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"id\": \"text-embedding-nomic-embed-text-v1.5\",\n" +
            "      \"object\": \"model\",\n" +
            "      \"owned_by\": \"organization_owner\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"object\": \"list\"\n" +
            "}";
        
        // Use reflection to access the private parseModels method
        try {
            java.lang.reflect.Method method = LmStudioProvider.class.getDeclaredMethod("parseModels", String.class);
            method.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            java.util.List<String> models = (java.util.List<String>) method.invoke(provider, response);
            
            assertEquals(3, models.size(), "Should parse all 3 models");
            assertTrue(models.contains("google/gemma-3-4b"), "Should contain gemma model");
            assertTrue(models.contains("qwen3-coder-30b-a3b-instruct"), "Should contain qwen model");
            assertTrue(models.contains("text-embedding-nomic-embed-text-v1.5"), "Should contain embedding model");
            
        } catch (Exception e) {
            fail("Failed to test parseModels: " + e.getMessage());
        }
    }
}
