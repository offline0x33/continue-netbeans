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

class AIContextApplicatorTest {

    @Test
    void singletonReturnsSameInstance() {
        AIContextApplicator first = AIContextApplicator.getInstance();
        AIContextApplicator second = AIContextApplicator.getInstance();

        assertSame(first, second);
    }

    @Test
    void applicationResultExposesAllFields() {
        AIConfigurationManager.ValidationResult validation =
                new AIConfigurationManager.ValidationResult(true, "validation", "ok");
        AIContextApplicator.ApplicationResult result = new AIContextApplicator.ApplicationResult(
                true, "operation", "message", "system prompt", "complete context", validation);

        assertTrue(result.isSuccess());
        assertEquals("operation", result.getOperation());
        assertEquals("message", result.getMessage());
        assertEquals("system prompt", result.getSystemPrompt());
        assertEquals("complete context", result.getCompleteContext());
        assertSame(validation, result.getValidation());
    }

    @Test
    void capabilitiesSummaryStoresCapabilityDetailsByName() {
        AIContextApplicator.CapabilitiesSummary summary =
                new AIContextApplicator.CapabilitiesSummary(true, true, "title");

        summary.addCapability("File System", "Read and write", true);
        summary.addCapability("Project", "Build projects", false);

        assertTrue(summary.isConfigured());
        assertTrue(summary.isValid());
        assertEquals("title", summary.getTitle());
        assertEquals(2, summary.getCapabilities().size());
        assertTrue(summary.getCapabilities().get("File System").isEnabled());
        assertFalse(summary.getCapabilities().get("Project").isEnabled());
    }

    @Test
    void capabilityDetailExposesFields() {
        AIContextApplicator.CapabilitiesSummary.CapabilityDetail detail =
                new AIContextApplicator.CapabilitiesSummary.CapabilityDetail("name", "description", true);

        assertEquals("name", detail.getName());
        assertEquals("description", detail.getDescription());
        assertTrue(detail.isEnabled());
    }

    @Test
    void getAICapabilitiesReturnsCompleteSummary() throws Exception {
        AIContextApplicator.CapabilitiesSummary summary =
                AIContextApplicator.getInstance().getAICapabilities().get(5, TimeUnit.SECONDS);

        assertTrue(summary.isConfigured());
        assertTrue(summary.isValid());
        assertEquals("AI Model Capabilities Summary", summary.getTitle());
        assertEquals(10, summary.getCapabilities().size());
        assertTrue(summary.getCapabilities().keySet().containsAll(List.of(
                "File System Operations", "Project Management", "Window Management",
                "Editor Integration", "Code Generation", "Refactoring", "Debugging",
                "Testing", "Documentation", "Configuration")));
    }

    @Test
    void applyNetBeansContextProducesSuccessfulApplicationResult() throws Exception {
        AIContextApplicator.ApplicationResult result =
                AIContextApplicator.getInstance().applyNetBeansContext().get(5, TimeUnit.SECONDS);

        assertTrue(result.isSuccess());
        assertEquals("NetBeans Context Application", result.getOperation());
        assertNotNull(result.getSystemPrompt());
        assertNotNull(result.getCompleteContext());
        assertNotNull(result.getValidation());
        assertTrue(result.getValidation().isValid());
    }

    @Test
    void failedApplicationResultCanRepresentMissingContext() {
        AIContextApplicator.ApplicationResult result = new AIContextApplicator.ApplicationResult(
                false, "NetBeans Context Application", "Error during application: test failure",
                null, null, null);

        assertFalse(result.isSuccess());
        assertEquals("Error during application: test failure", result.getMessage());
        assertEquals(null, result.getSystemPrompt());
        assertEquals(null, result.getCompleteContext());
        assertEquals(null, result.getValidation());
    }
}
