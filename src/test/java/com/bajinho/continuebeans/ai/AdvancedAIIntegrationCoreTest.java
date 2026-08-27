package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedAIIntegrationCoreTest {

    @Test
    void providerManagerStartsWithDefaultsAndSupportsRegistrationLookupAndSelection() {
        AdvancedAIIntegration.AIProviderManager manager = new AdvancedAIIntegration.AIProviderManager();
        assertNotNull(manager.getProvider("openai"));
        assertNotNull(manager.getProvider("local"));
        assertNull(manager.getProvider("missing"));
        assertTrue(manager.getAllProviders().size() >= 2);
        assertTrue(manager.getActiveProviders().isEmpty());

        AdvancedAIIntegration.AIProvider provider = new AdvancedAIIntegration.AIProvider(
                "custom", "Custom", "1.0", AdvancedAIIntegration.ProviderType.CUSTOM,
                null, true, null, null, AdvancedAIIntegration.ProviderStatus.ACTIVE);
        manager.registerProvider(provider);
        assertSame(provider, manager.getProvider("custom"));
        assertTrue(manager.getActiveProviders().contains(provider));
        assertTrue(manager.activateProvider("custom"));
        assertTrue(manager.deactivateProvider("custom"));
        assertFalse(manager.activateProvider("missing"));
        assertFalse(manager.deactivateProvider("missing"));
    }

    @Test
    void modelManagerCoversDefaultsRegistrationAvailabilityAndProviderFilter() {
        AdvancedAIIntegration.ModelManager manager = new AdvancedAIIntegration.ModelManager();
        assertNotNull(manager.getModel("gpt-4"));
        assertNotNull(manager.getModel("gpt-3.5-turbo"));
        assertNull(manager.getModel("missing"));
        assertTrue(manager.getAvailableModels().size() >= 2);
        assertEquals(2, manager.getModelsByProvider("openai").size());
        assertTrue(manager.getModelsByProvider("missing").isEmpty());

        AdvancedAIIntegration.AIModel model = new AdvancedAIIntegration.AIModel(
                "local-test", "Local Test", "local", AdvancedAIIntegration.ModelType.CODE_GENERATION,
                4096, 0.0, null, AdvancedAIIntegration.ModelStatus.UNAVAILABLE,
                new AdvancedAIIntegration.PerformanceMetrics(1.0, 0.9, 10, 1, 2, 3));
        manager.registerModel(model);
        assertSame(model, manager.getModel("local-test"));
        assertEquals(6, new AdvancedAIIntegration.PerformanceMetrics(1, .5, 2, 3, 4, 5).getTotalRequests() + 3);
    }

    @Test
    void promptManagerProcessesDefaultsCustomTemplateAndUnknownTemplate() {
        AdvancedAIIntegration.PromptManager manager = new AdvancedAIIntegration.PromptManager();
        assertNotNull(manager.getTemplate("code_generation"));
        assertNull(manager.getTemplate("missing"));

        String generated = manager.processTemplate("code_generation", Map.of(
                "language", "Java", "description", "a service"));
        assertTrue(generated.contains("Java"));
        assertTrue(generated.contains("a service"));
        assertEquals("", manager.processTemplate("missing", Map.of()));

        AdvancedAIIntegration.PromptTemplate custom = new AdvancedAIIntegration.PromptTemplate(
                "custom", "Custom", "Description", Map.of("name", "default"), "Hello {name}");
        manager.registerTemplate(custom);
        assertEquals("Hello World", manager.processTemplate("custom", Map.of("name", "World")));
    }

    @Test
    void valueObjectsExposeDefaultsAndMetrics() {
        AdvancedAIIntegration.AIProvider provider = new AdvancedAIIntegration.AIProvider(
                "id", "name", "version", AdvancedAIIntegration.ProviderType.LOCAL,
                null, false, null, null, AdvancedAIIntegration.ProviderStatus.INACTIVE);
        assertTrue(provider.getConfiguration().isEmpty());
        assertTrue(provider.getSupportedModels().isEmpty());
        assertTrue(provider.getCapabilities().isEmpty());
        assertFalse(provider.isActive());

        AdvancedAIIntegration.AIModel model = new AdvancedAIIntegration.AIModel(
                "m", "Model", "p", AdvancedAIIntegration.ModelType.CHAT, 100, .2,
                null, AdvancedAIIntegration.ModelStatus.AVAILABLE,
                new AdvancedAIIntegration.PerformanceMetrics(4.5, .99, 20, 30, 40, .8));
        assertTrue(model.getCapabilities().isEmpty());
        assertEquals(100, model.getContextWindow());
        assertEquals(.2, model.getCostPerToken());
        assertEquals(4.5, model.getPerformance().getAverageResponseTime());
        assertEquals(.99, model.getPerformance().getSuccessRate());
        assertEquals(20, model.getPerformance().getThroughput());
        assertEquals(30, model.getPerformance().getTotalRequests());
        assertEquals(40, model.getPerformance().getTotalTokens());
        assertEquals(.8, model.getPerformance().getCostEfficiency());
    }

    @Test
    void promptTemplateHandlesMissingAndExtraVariables() {
        AdvancedAIIntegration.PromptTemplate template = new AdvancedAIIntegration.PromptTemplate(
                "id", "name", "desc", Map.of("name", "fallback"),
                "Hello {name}, role={role}");
        String rendered = template.process(Map.of("name", "Alice", "unused", "value"));
        assertEquals("Hello Alice, role={role}", rendered);
    }
}
