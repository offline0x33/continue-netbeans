package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedAIIntegrationManagerTest {

    @Test
    void providerManagerInitializesDefaultsAndSupportsLookupRegistration() {
        AdvancedAIIntegration.AIProviderManager manager = new AdvancedAIIntegration.AIProviderManager();
        assertNotNull(manager.getProvider("openai"));
        assertNotNull(manager.getProvider("local"));
        assertNull(manager.getProvider("missing"));
        assertTrue(manager.getActiveProviders().isEmpty());

        AdvancedAIIntegration.AIProvider provider = new AdvancedAIIntegration.AIProvider(
                "custom", "Custom", "1.0", AdvancedAIIntegration.ProviderType.CUSTOM,
                Map.of("url", "test"), true, List.of("model"), Map.of("tools", true),
                AdvancedAIIntegration.ProviderStatus.ACTIVE);
        manager.registerProvider(provider);
        assertSame(provider, manager.getProvider("custom"));
        assertEquals(1, manager.getActiveProviders().size());
        assertTrue(manager.activateProvider("custom"));
        assertTrue(manager.deactivateProvider("custom"));
        assertFalse(manager.activateProvider("missing"));
        assertFalse(manager.deactivateProvider("missing"));
        assertEquals(3, manager.getAllProviders().size());
    }

    @Test
    void modelManagerFiltersAndLooksUpModels() {
        AdvancedAIIntegration.ModelManager manager = new AdvancedAIIntegration.ModelManager();
        assertNotNull(manager.getModel("gpt-4"));
        assertNull(manager.getModel("missing"));
        assertEquals(2, manager.getAvailableModels().size());
        assertEquals(2, manager.getModelsByProvider("openai").size());
        assertTrue(manager.getModelsByProvider("missing").isEmpty());

        AdvancedAIIntegration.AIModel model = new AdvancedAIIntegration.AIModel(
                "local", "Local", "local", AdvancedAIIntegration.ModelType.CODE_GENERATION,
                4096, 0.0, Map.of("code", true), AdvancedAIIntegration.ModelStatus.AVAILABLE,
                new AdvancedAIIntegration.PerformanceMetrics(1, 1, 1, 1, 1, 1));
        manager.registerModel(model);
        assertSame(model, manager.getModel("local"));
        assertEquals(3, manager.getAllModels().size());
    }

    @Test
    void promptManagerProcessesKnownTemplatesAndUnknownTemplate() {
        AdvancedAIIntegration.PromptManager manager = new AdvancedAIIntegration.PromptManager();
        assertNotNull(manager.getTemplate("code_generation"));
        assertNotNull(manager.getTemplate("code_review"));
        assertNull(manager.getTemplate("missing"));

        String prompt = manager.processTemplate("code_generation", Map.of(
                "language", "Java", "description", "a service"));
        assertTrue(prompt.contains("Java"));
        assertTrue(prompt.contains("a service"));
        assertEquals("", manager.processTemplate("missing", Map.of()));

        AdvancedAIIntegration.PromptTemplate template = new AdvancedAIIntegration.PromptTemplate(
                "custom", "Custom", "desc", Map.of("name", "name"), "Hello {name}");
        manager.registerTemplate(template);
        assertEquals("Hello World", manager.processTemplate("custom", Map.of("name", "World")));
    }

    @Test
    void valueObjectsExposeConstructorState() {
        AdvancedAIIntegration.PerformanceMetrics metrics = new AdvancedAIIntegration.PerformanceMetrics(2.0, .9, 5, 10, 20, .8);
        assertEquals(2.0, metrics.getAverageResponseTime());
        assertEquals(.9, metrics.getSuccessRate());
        assertEquals(5, metrics.getThroughput());
        assertEquals(10, metrics.getTotalRequests());
        assertEquals(20, metrics.getTotalTokens());
        assertEquals(.8, metrics.getCostEfficiency());

        AdvancedAIIntegration.AIProvider provider = new AdvancedAIIntegration.AIProvider(
                "p", "P", "v", AdvancedAIIntegration.ProviderType.LOCAL,
                null, false, null, null, AdvancedAIIntegration.ProviderStatus.INACTIVE);
        assertEquals("p", provider.getProviderId());
        assertEquals("P", provider.getName());
        assertEquals("v", provider.getVersion());
        assertEquals(AdvancedAIIntegration.ProviderType.LOCAL, provider.getType());
        assertFalse(provider.isActive());
        assertTrue(provider.getConfiguration().isEmpty());
        assertTrue(provider.getSupportedModels().isEmpty());
        assertTrue(provider.getCapabilities().isEmpty());
        assertEquals(AdvancedAIIntegration.ProviderStatus.INACTIVE, provider.getStatus());
    }
}
