package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AdvancedAIIntegrationManagerTest {
 @Test void providerManagerDefaultsAndRegistration(){var m=new AdvancedAIIntegration.AIProviderManager();assertNotNull(m.getProvider("openai"));assertNotNull(m.getProvider("local"));assertNull(m.getProvider("missing"));var p=new AdvancedAIIntegration.AIProvider("custom","Custom","1",AdvancedAIIntegration.ProviderType.CUSTOM,Map.of(),true,List.of("m"),Map.of(),AdvancedAIIntegration.ProviderStatus.ACTIVE);m.registerProvider(p);assertSame(p,m.getProvider("custom"));assertEquals(1,m.getActiveProviders().size());assertTrue(m.activateProvider("custom"));assertTrue(m.deactivateProvider("custom"));assertFalse(m.activateProvider("missing"));}
 @Test void modelManagerLookupAndFilters(){var m=new AdvancedAIIntegration.ModelManager();assertNotNull(m.getModel("gpt-4"));assertNull(m.getModel("missing"));assertEquals(2,m.getAvailableModels().size());assertEquals(2,m.getModelsByProvider("openai").size());assertTrue(m.getModelsByProvider("missing").isEmpty());}
 @Test void promptManagerProcessesTemplates(){var m=new AdvancedAIIntegration.PromptManager();assertNotNull(m.getTemplate("code_generation"));assertNull(m.getTemplate("missing"));String s=m.processTemplate("code_generation",Map.of("language","Java","description","service"));assertTrue(s.contains("Java"));assertTrue(s.contains("service"));assertEquals("",m.processTemplate("missing",Map.of()));}
 @Test void valueObjectsPreserveState(){var metrics=new AdvancedAIIntegration.PerformanceMetrics(1,2,3,4,5,6);assertEquals(1,metrics.getAverageResponseTime());assertEquals(4,metrics.getTotalRequests());var p=new AdvancedAIIntegration.AIProvider("p","P","v",AdvancedAIIntegration.ProviderType.LOCAL,null,false,null,null,AdvancedAIIntegration.ProviderStatus.INACTIVE);assertTrue(p.getConfiguration().isEmpty());assertTrue(p.getSupportedModels().isEmpty());assertTrue(p.getCapabilities().isEmpty());}
}
