package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AdvancedAIIntegrationTest {
 @Test void promptTemplateReplacesKnownVariables(){var t=new AdvancedAIIntegration.PromptTemplate("id","Name","desc",Map.of("name","person"),"Hello {name}");assertEquals("Hello World",t.process(Map.of("name","World")));}
 @Test void promptTemplateLeavesMissingVariable(){var t=new AdvancedAIIntegration.PromptTemplate("id","Name","desc",Map.of(),"Hello {missing}");assertEquals("Hello {missing}",t.process(Map.of()));}
 @Test void performanceMetricsExposeAllValues(){var m=new AdvancedAIIntegration.PerformanceMetrics(1,2,3,4,5,6);assertEquals(1,m.getAverageResponseTime());assertEquals(2,m.getSuccessRate());assertEquals(3,m.getThroughput());assertEquals(4,m.getTotalRequests());assertEquals(5,m.getTotalTokens());assertEquals(6,m.getCostEfficiency());}
}
