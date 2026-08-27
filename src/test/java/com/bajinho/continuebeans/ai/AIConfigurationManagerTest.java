package com.bajinho.continuebeans.ai;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AIConfigurationManagerTest {

    @Test
    void singletonReturnsSameManager() {
        assertSame(AIConfigurationManager.getInstance(), AIConfigurationManager.getInstance());
    }

    @Test
    void configureContextProducesSuccessfulConfiguration() throws Exception {
        AIConfigurationManager manager = AIConfigurationManager.getInstance();
        AIConfigurationManager.ConfigurationStatus status = manager.configureAIWithContext().get();

        assertTrue(status.isSuccess());
        assertEquals("AI Model configured with NetBeans Platform context", status.getMessage());
        assertNotNull(status.getSystemPrompt());
        assertFalse(status.getSystemPrompt().isBlank());
        assertNotNull(status.getStructuredContext());
    }

    @Test
    void currentConfigurationStatusIsReady() throws Exception {
        AIConfigurationManager.ConfigurationStatus status =
                AIConfigurationManager.getInstance().getConfigurationStatus().get();

        assertTrue(status.isSuccess());
        assertEquals("AI Model is configured and ready", status.getMessage());
        assertNotNull(status.getSystemPrompt());
        assertNotNull(status.getStructuredContext());
    }

    @Test
    void contextValidationReportsAllCapabilities() throws Exception {
        AIConfigurationManager.ValidationResult result =
                AIConfigurationManager.getInstance().validateAIContext().get();

        assertTrue(result.isValid());
        assertEquals("AI Context Validation", result.getTestName());
        assertEquals("All NetBeans capabilities present", result.getResult());
        assertEquals(10, result.getChecks().size());
        assertTrue(result.getChecks().values().stream().allMatch(Boolean.TRUE::equals));
        assertTrue(result.getChecks().containsKey("File System Access"));
        assertTrue(result.getChecks().containsKey("Configuration"));
    }

    @Test
    void resultValueObjectsExposeSuppliedValuesAndAllowChecks() {
        Map<String, Object> context = Map.of("capabilities", 10);
        AIConfigurationManager.ConfigurationStatus status =
                new AIConfigurationManager.ConfigurationStatus(true, "ok", "prompt", context);

        assertTrue(status.isSuccess());
        assertEquals("ok", status.getMessage());
        assertEquals("prompt", status.getSystemPrompt());
        assertSame(context, status.getStructuredContext());

        AIConfigurationManager.ValidationResult result =
                new AIConfigurationManager.ValidationResult(false, "test", "failed");
        result.addCheck("one", true);
        result.addCheck("two", false);
        assertFalse(result.isValid());
        assertEquals("test", result.getTestName());
        assertEquals("failed", result.getResult());
        assertEquals(Map.of("one", true, "two", false), result.getChecks());
    }
}
