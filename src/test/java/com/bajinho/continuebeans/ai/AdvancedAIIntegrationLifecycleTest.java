package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AdvancedAIIntegrationLifecycleTest {
 @Test void providerObjectUsesSafeEmptyCollections(){var p=new AdvancedAIIntegration.AIProvider("p","P","1",AdvancedAIIntegration.ProviderType.CUSTOM,null,false,null,null,AdvancedAIIntegration.ProviderStatus.INACTIVE);assertEquals("p",p.getProviderId());assertEquals("P",p.getName());assertFalse(p.isActive());assertTrue(p.getConfiguration().isEmpty());assertTrue(p.getSupportedModels().isEmpty());assertTrue(p.getCapabilities().isEmpty());}
 @Test void modelObjectPreservesValues(){var perf=new AdvancedAIIntegration.PerformanceMetrics(1,.9,10,20,30,.8);var m=new AdvancedAIIntegration.AIModel("id","Name","provider",AdvancedAIIntegration.ModelType.CHAT,4096,.1,Map.of("streaming",true),AdvancedAIIntegration.ModelStatus.AVAILABLE,perf);assertEquals("id",m.getModelId());assertEquals("provider",m.getProviderId());assertEquals(4096,m.getContextWindow());assertEquals(.1,m.getCostPerToken());assertEquals(AdvancedAIIntegration.ModelStatus.AVAILABLE,m.getStatus());assertSame(perf,m.getPerformance());}
}
