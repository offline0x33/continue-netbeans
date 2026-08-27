package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AIContextProviderApplicatorTest {

    @Test
    void providerIsSingletonAndExposesCompleteCapabilityCatalog() throws Exception {
        AIContextProvider first = AIContextProvider.getInstance();
        AIContextProvider second = AIContextProvider.getInstance();

        assertSame(first, second);

        List<AIContextProvider.AICapability> capabilities = first.getAllAICapabilities();
        assertEquals(10, capabilities.size());
        assertTrue(capabilities.stream().allMatch(AIContextProvider.AICapability::isImplemented));
        assertTrue(capabilities.stream().anyMatch(c -> "file_operations".equals(c.getCapabilityId())));
        assertTrue(capabilities.stream().anyMatch(c -> "configuration".equals(c.getCapabilityId())));

        AIContextProvider.AICapability file = capabilities.stream()
                .filter(c -> "file_operations".equals(c.getCapabilityId()))
                .findFirst().orElseThrow();
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

        Map<?, ?> explicit = (Map<?, ?>) context.get("explicit_capabilities");
        assertEquals(10, explicit.size());
        assertEquals("Can read/write/create/delete any file in the project", explicit.get("file_system"));

        List<?> instructions = (List<?>) context.get("ai_instructions");
        List<?> examples = (List<?>) context.get("example_commands");
        assertEquals(9, instructions.size());
        assertEquals(8, examples.size());
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
        AIContextProvider.AICapability capability = new AIContextProvider.AICapability(
                "id", "name", "description", null, null, false);

        assertTrue(capability.getAvailableActions().isEmpty());
        assertTrue(capability.getExamples().isEmpty());
        assertFalse(capability.isImplemented());
    }

    @Test
    void applicatorIsSingletonAndExposesCapabilitySummary() throws Exception {
        AIContextApplicator first = AIContextApplicator.getInstance();
        AIContextApplicator second = AIContextApplicator.getInstance();
        assertSame(first, second);

        AIContextApplicator.CapabilitiesSummary summary = first.getAICapabilities().get();
        assertNotNull(summary);
        assertEquals("AI Model Capabilities Summary", summary.getTitle());
        assertEquals(10, summary.getCapabilities().size());
        assertTrue(summary.getCapabilities().containsKey("File System Operations"));
        assertTrue(summary.getCapabilities().containsKey("Configuration"));

        for (AIContextApplicator.CapabilitiesSummary.CapabilityDetail detail : summary.getCapabilities().values()) {
            assertNotNull(detail.getName());
            assertNotNull(detail.getDescription());
        }
    }

    @Test
    void capabilitySummaryStoresCapabilityDetailsAndReplacesDuplicateNames() {
        AIContextApplicator.CapabilitiesSummary summary =
                new AIContextApplicator.CapabilitiesSummary(true, false, "Test");

        summary.addCapability("Files", "first", false);
        summary.addCapability("Files", "second", true);

        assertTrue(summary.isConfigured());
        assertFalse(summary.isValid());
        assertEquals("Test", summary.getTitle());
        assertEquals(1, summary.getCapabilities().size());
        assertEquals("second", summary.getCapabilities().get("Files").getDescription());
        assertTrue(summary.getCapabilities().get("Files").isEnabled());
    }

    @Test
    void applicationResultPreservesAllSuppliedValues() {
        AIContextApplicator.ApplicationResult result = new AIContextApplicator.ApplicationResult(
                true, "operation", "message", "system", "complete", null);

        assertTrue(result.isSuccess());
        assertEquals("operation", result.getOperation());
        assertEquals("message", result.getMessage());
        assertEquals("system", result.getSystemPrompt());
        assertEquals("complete", result.getCompleteContext());
        assertNull(result.getValidation());
    }
}
