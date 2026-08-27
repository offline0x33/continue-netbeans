package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AIContextProviderApplicatorTest {
    @Test
    void providerIsSingletonAndExposesCompleteCapabilityCatalog() throws Exception {
        AIContextProvider first = AIContextProvider.getInstance();
        assertSame(first, AIContextProvider.getInstance());
        List<AIContextProvider.AICapability> capabilities = first.getAllAICapabilities();
        assertEquals(10, capabilities.size());
        assertTrue(capabilities.stream().allMatch(AIContextProvider.AICapability::isImplemented));
        AIContextProvider.AICapability file = capabilities.stream().filter(c -> "file_operations".equals(c.getCapabilityId())).findFirst().orElseThrow();
        assertFalse(file.getAvailableActions().isEmpty());
        assertFalse(file.getExamples().isEmpty());
        assertNotNull(file.getDescription());
    }

    @Test
    void providerBuildsRichAiContext() throws Exception {
        Map<String, Object> context = AIContextProvider.getInstance().getAIContext().get();
        assertEquals("COMPLETE - Enterprise-grade NetBeans integration", context.get("integration_level"));
        assertEquals(10, context.get("available_apis"));
        assertEquals("AI CONTEXT READY - Full NetBeans Platform Access Available", context.get("status"));
        assertEquals(10, ((Map<?, ?>) context.get("explicit_capabilities")).size());
        assertEquals(9, ((List<?>) context.get("ai_instructions")).size());
        assertEquals(8, ((List<?>) context.get("example_commands")).size());
    }

    @Test
    void providerCapabilitySummaryMatchesCatalog() {
        Map<String, Object> summary = AIContextProvider.getInstance().getCapabilitySummary();
        assertEquals(10, summary.get("total_capabilities"));
        assertEquals(10, summary.get("implemented_capabilities"));
        assertEquals("COMPLETE", summary.get("integration_level"));
        assertEquals(10, ((List<?>) summary.get("capability_names")).size());
    }

    @Test
    void capabilityValueObjectUsesEmptyCollectionsForNullInputs() {
        AIContextProvider.AICapability capability = new AIContextProvider.AICapability("id", "name", "description", null, null, false);
        assertTrue(capability.getAvailableActions().isEmpty());
        assertTrue(capability.getExamples().isEmpty());
        assertFalse(capability.isImplemented());
    }

    @Test
    void applicatorIsSingletonAndExposesCapabilitySummary() throws Exception {
        AIContextApplicator first = AIContextApplicator.getInstance();
        assertSame(first, AIContextApplicator.getInstance());
        AIContextApplicator.CapabilitiesSummary summary = first.getAICapabilities().get();
        assertNotNull(summary);
        assertEquals("AI Model Capabilities Summary", summary.getTitle());
        assertEquals(10, summary.getCapabilities().size());
        assertTrue(summary.getCapabilities().containsKey("File System Operations"));
        assertTrue(summary.getCapabilities().containsKey("Configuration"));
        assertTrue(summary.getCapabilities().values().stream().allMatch(d -> d.getName() != null && d.getDescription() != null));
    }

    @Test
    void capabilitySummaryStoresAndReplacesCapabilityDetails() {
        AIContextApplicator.CapabilitiesSummary summary = new AIContextApplicator.CapabilitiesSummary(true, false, "Test");
        summary.addCapability("Files", "first", false);
        summary.addCapability("Files", "second", true);
        assertTrue(summary.isConfigured());
        assertFalse(summary.isValid());
        assertEquals(1, summary.getCapabilities().size());
        assertEquals("second", summary.getCapabilities().get("Files").getDescription());
        assertTrue(summary.getCapabilities().get("Files").isEnabled());
    }

    @Test
    void applicationResultPreservesValues() {
        AIContextApplicator.ApplicationResult result = new AIContextApplicator.ApplicationResult(true, "operation", "message", "system", "complete", null);
        assertTrue(result.isSuccess());
        assertEquals("operation", result.getOperation());
        assertEquals("message", result.getMessage());
        assertEquals("system", result.getSystemPrompt());
        assertEquals("complete", result.getCompleteContext());
        assertNull(result.getValidation());
    }
}
