package com.bajinho.continuebeans.ai;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AIContextProviderTest {

    @Test
    void singletonReturnsSameInstance() {
        AIContextProvider first = AIContextProvider.getInstance();
        AIContextProvider second = AIContextProvider.getInstance();

        assertSame(first, second);
    }

    @Test
    void capabilityConstructorNormalizesNullCollections() {
        AIContextProvider.AICapability capability = new AIContextProvider.AICapability(
                "id", "Name", "Description", null, null, true);

        assertNotNull(capability.getAvailableActions());
        assertNotNull(capability.getExamples());
        assertTrue(capability.getAvailableActions().isEmpty());
        assertTrue(capability.getExamples().isEmpty());
        assertTrue(capability.isImplemented());
    }

    @Test
    void allCapabilitiesExposeCompleteImplementedSet() {
        List<AIContextProvider.AICapability> capabilities = AIContextProvider.getInstance().getAllAICapabilities();

        assertEquals(10, capabilities.size());
        assertTrue(capabilities.stream().allMatch(AIContextProvider.AICapability::isImplemented));
        assertEquals(10, capabilities.stream().map(AIContextProvider.AICapability::getCapabilityId).distinct().count());
        assertTrue(capabilities.stream().allMatch(c -> !c.getAvailableActions().isEmpty()));
        assertTrue(capabilities.stream().allMatch(c -> !c.getExamples().isEmpty()));
    }

    @Test
    void capabilitySummaryMatchesAllImplementedCapabilities() {
        Map<String, Object> summary = AIContextProvider.getInstance().getCapabilitySummary();

        assertEquals(10, summary.get("total_capabilities"));
        assertEquals(10, summary.get("implemented_capabilities"));
        assertEquals("COMPLETE", summary.get("integration_level"));
        assertEquals(10, ((List<?>) summary.get("capability_names")).size());
    }

    @Test
    void aiContextContainsExpectedStructuredSections() throws Exception {
        Map<String, Object> context = AIContextProvider.getInstance().getAIContext().get(5, TimeUnit.SECONDS);

        assertEquals("COMPLETE - Enterprise-grade NetBeans integration", context.get("integration_level"));
        assertEquals(10, context.get("available_apis"));
        assertEquals("AI CONTEXT READY - Full NetBeans Platform Access Available", context.get("status"));
        assertNotNull(context.get("explicit_capabilities"));
        assertNotNull(context.get("ai_instructions"));
        assertNotNull(context.get("example_commands"));
    }

    @Test
    void aiContextListsConcreteCapabilitiesAndExamples() throws Exception {
        Map<String, Object> context = AIContextProvider.getInstance().getAIContext().get(5, TimeUnit.SECONDS);
        Map<?, ?> explicitCapabilities = (Map<?, ?>) context.get("explicit_capabilities");

        assertEquals(10, explicitCapabilities.size());
        assertTrue(explicitCapabilities.containsKey("file_system"));
        assertTrue(explicitCapabilities.containsKey("project_management"));
        assertTrue(((List<?>) context.get("ai_instructions")).size() >= 8);
        assertTrue(((List<?>) context.get("example_commands")).size() >= 8);
    }

    @Test
    void capabilityDetailsExposeStableIdentityAndContent() {
        AIContextProvider.AICapability fileOperations = AIContextProvider.getInstance().getAllAICapabilities().stream()
                .filter(c -> "file_operations".equals(c.getCapabilityId()))
                .findFirst()
                .orElseThrow();

        assertEquals("File System Operations", fileOperations.getName());
        assertTrue(fileOperations.getDescription().contains("read"));
        assertTrue(fileOperations.getAvailableActions().contains("read_file"));
        assertTrue(fileOperations.getExamples().stream().anyMatch(example -> example.contains("Main.java")));
        assertFalse(fileOperations.getAvailableActions().isEmpty());
    }
}
