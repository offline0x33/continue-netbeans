package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedAIIntegrationTest {

    @Test
    void providerManagerHandlesDefaultAndCustomProviders() {
        AdvancedAIIntegration.AIProviderManager manager = new AdvancedAIIntegration.AIProviderManager();
        assertEquals(2, manager.getAllProviders().size());
        assertFalse(manager.activateProvider("missing"));
        assertFalse(manager.deactivateProvider("missing"));
    }

    @Test
    void promptTemplateReplacesVariablesAndLeavesUnknownPlaceholders() {
        AdvancedAIIntegration.PromptTemplate template = new AdvancedAIIntegration.PromptTemplate(
                "id", "Name", "desc", Map.of("name", "person"), "Hello {name}, use {missing}");
        String result = template.process(Map.of("name", "World"));
        assertEquals("Hello World, use {missing}", result);
    }
}
