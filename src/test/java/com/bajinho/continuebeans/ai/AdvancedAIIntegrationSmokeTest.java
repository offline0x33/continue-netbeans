package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.*;

class AdvancedAIIntegrationSmokeTest {

    @Test
    void nestedProviderAndModelTypesAreConstructible() {
        AdvancedAIIntegration.AIProvider provider = new AdvancedAIIntegration.AIProvider(
                "id", "name", "1", AdvancedAIIntegration.ProviderType.CUSTOM,
                null, false, null, null, AdvancedAIIntegration.ProviderStatus.INACTIVE);
        AdvancedAIIntegration.PerformanceMetrics metrics = new AdvancedAIIntegration.PerformanceMetrics(0, 1, 2, 3, 4, 5);
        AdvancedAIIntegration.AIModel model = new AdvancedAIIntegration.AIModel(
                "m", "M", "id", AdvancedAIIntegration.ModelType.CHAT,
                1024, 0.1, null, AdvancedAIIntegration.ModelStatus.AVAILABLE, metrics);
        assertEquals("id", provider.getProviderId());
        assertEquals("m", model.getModelId());
        assertEquals(3, metrics.getTotalRequests());
    }
}
