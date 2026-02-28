package com.bajinho.continuebeans.ai;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * AI Configuration Manager - Configures AI model with NetBeans context.
 * This class ensures the AI model understands its NetBeans capabilities permanently.
 * 
 * @author Continue Beans Team
 */
public class AIConfigurationManager {
    
    private static final Logger LOG = Logger.getLogger(AIConfigurationManager.class.getName());
    
    private static AIConfigurationManager instance;
    
    /**
     * Configures the AI model with complete NetBeans context.
     * This method should be called once to set up the AI model permanently.
     * 
     * @return Configuration status
     */
    public CompletableFuture<ConfigurationStatus> configureAIWithContext() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get the complete system prompt context
                String systemPrompt = AISystemContext.getSystemPromptContext();
                
                // Get structured context for programmatic access
                var structuredContext = AISystemContext.getInstance().getStructuredContext().get();
                
                // Log configuration
                LOG.info("Configuring AI model with NetBeans context...");
                LOG.info("System prompt length: " + systemPrompt.length() + " characters");
                LOG.info("Available capabilities: " + structuredContext.get("capabilities"));
                LOG.info("Total example commands: " + structuredContext.get("example_commands"));
                
                // Configuration successful
                ConfigurationStatus status = new ConfigurationStatus(
                    true, 
                    "AI Model configured with NetBeans Platform context",
                    systemPrompt,
                    structuredContext
                );
                
                LOG.info("AI Model configuration completed successfully!");
                return status;
                
            } catch (Exception e) {
                LOG.severe("Failed to configure AI model: " + e.getMessage());
                return new ConfigurationStatus(
                    false, 
                    "Configuration failed: " + e.getMessage(),
                    null,
                    null
                );
            }
        });
    }
    
    /**
     * Gets the current AI configuration status.
     * 
     * @return Current configuration status
     */
    public CompletableFuture<ConfigurationStatus> getConfigurationStatus() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String systemPrompt = AISystemContext.getSystemPromptContext();
                var structuredContext = AISystemContext.getInstance().getStructuredContext().get();
                
                return new ConfigurationStatus(
                    true,
                    "AI Model is configured and ready",
                    systemPrompt,
                    structuredContext
                );
            } catch (Exception e) {
                return new ConfigurationStatus(
                    false,
                    "AI Model not configured: " + e.getMessage(),
                    null,
                    null
                );
            }
        });
    }
    
    /**
     * Validates that the AI model has proper NetBeans context.
     * 
     * @return Validation result
     */
    public CompletableFuture<ValidationResult> validateAIContext() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String systemPrompt = AISystemContext.getSystemPromptContext();
                
                // Check for key NetBeans capabilities
                boolean hasFileSystemAccess = systemPrompt.contains("read/write/create/delete any file");
                boolean hasProjectManagement = systemPrompt.contains("NetBeans projects");
                boolean hasWindowManagement = systemPrompt.contains("NetBeans windows");
                boolean hasEditorIntegration = systemPrompt.contains("NetBeans editor");
                boolean hasCodeGeneration = systemPrompt.contains("generate complete Java classes");
                boolean hasRefactoring = systemPrompt.contains("refactor existing code");
                boolean hasDebugging = systemPrompt.contains("analyze errors and suggest fixes");
                boolean hasTesting = systemPrompt.contains("create unit tests");
                boolean hasDocumentation = systemPrompt.contains("generate comprehensive documentation");
                boolean hasConfiguration = systemPrompt.contains("manage Maven/Gradle");
                
                boolean allCapabilitiesPresent = hasFileSystemAccess && hasProjectManagement && 
                    hasWindowManagement && hasEditorIntegration && hasCodeGeneration && 
                    hasRefactoring && hasDebugging && hasTesting && hasDocumentation && hasConfiguration;
                
                ValidationResult result = new ValidationResult(
                    allCapabilitiesPresent,
                    "AI Context Validation",
                    allCapabilitiesPresent ? "All NetBeans capabilities present" : "Missing capabilities detected"
                );
                
                // Add detailed checks
                result.addCheck("File System Access", hasFileSystemAccess);
                result.addCheck("Project Management", hasProjectManagement);
                result.addCheck("Window Management", hasWindowManagement);
                result.addCheck("Editor Integration", hasEditorIntegration);
                result.addCheck("Code Generation", hasCodeGeneration);
                result.addCheck("Refactoring", hasRefactoring);
                result.addCheck("Debugging", hasDebugging);
                result.addCheck("Testing", hasTesting);
                result.addCheck("Documentation", hasDocumentation);
                result.addCheck("Configuration", hasConfiguration);
                
                return result;
                
            } catch (Exception e) {
                return new ValidationResult(
                    false,
                    "Validation Error",
                    "Failed to validate AI context: " + e.getMessage()
                );
            }
        });
    }
    
    /**
     * Configuration status class.
     */
    public static class ConfigurationStatus {
        private final boolean success;
        private final String message;
        private final String systemPrompt;
        private final Object structuredContext;
        
        public ConfigurationStatus(boolean success, String message, String systemPrompt, Object structuredContext) {
            this.success = success;
            this.message = message;
            this.systemPrompt = systemPrompt;
            this.structuredContext = structuredContext;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getSystemPrompt() { return systemPrompt; }
        public Object getStructuredContext() { return structuredContext; }
    }
    
    /**
     * Validation result class.
     */
    public static class ValidationResult {
        private final boolean valid;
        private final String testName;
        private final String result;
        private final java.util.Map<String, Boolean> checks = new java.util.HashMap<>();
        
        public ValidationResult(boolean valid, String testName, String result) {
            this.valid = valid;
            this.testName = testName;
            this.result = result;
        }
        
        public void addCheck(String checkName, boolean passed) {
            checks.put(checkName, passed);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getTestName() { return testName; }
        public String getResult() { return result; }
        public java.util.Map<String, Boolean> getChecks() { return checks; }
    }
    
    /**
     * Private constructor for singleton.
     */
    private AIConfigurationManager() {
        LOG.info("AIConfigurationManager initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The AIConfigurationManager instance
     */
    public static synchronized AIConfigurationManager getInstance() {
        if (instance == null) {
            instance = new AIConfigurationManager();
        }
        return instance;
    }
}
