package com.bajinho.continuebeans.ai;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * AI Context Applicator - Applies NetBeans context to AI model automatically.
 * This ensures the AI model permanently understands its NetBeans capabilities.
 * 
 * @author Continue Beans Team
 */
public class AIContextApplicator {
    
    private static final Logger LOG = Logger.getLogger(AIContextApplicator.class.getName());
    
    private static AIContextApplicator instance;
    
    /**
     * Applies NetBeans context to AI model and ensures permanent understanding.
     * This is the main method to call for setting up AI with NetBeans capabilities.
     * 
     * @return Application result
     */
    public CompletableFuture<ApplicationResult> applyNetBeansContext() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                LOG.info("Starting NetBeans context application to AI model...");
                
                // Step 1: Get the complete context
                String systemPrompt = AISystemContext.getSystemPromptContext();
                String completeContext = AISystemContext.getCompleteAIContext();
                
                // Step 2: Configure AI with context
                AIConfigurationManager configManager = AIConfigurationManager.getInstance();
                var configStatus = configManager.configureAIWithContext().get();
                
                // Step 3: Validate the configuration
                var validationResult = configManager.validateAIContext().get();
                
                // Step 4: Create application summary
                ApplicationResult result = new ApplicationResult(
                    configStatus.isSuccess() && validationResult.isValid(),
                    "NetBeans Context Application",
                    configStatus.isSuccess() ? "Successfully applied NetBeans context to AI model" : "Failed to apply context",
                    systemPrompt,
                    completeContext,
                    validationResult
                );
                
                // Step 5: Log the result
                if (result.isSuccess()) {
                    LOG.info("✅ AI Model now has FULL NETBEANS PLATFORM ACCESS!");
                    LOG.info("🚀 AI can read/write/create/delete any file");
                    LOG.info("🏗️ AI can manage NetBeans projects");
                    LOG.info("🪟 AI can control NetBeans windows");
                    LOG.info("✏️ AI can integrate with NetBeans editor");
                    LOG.info("🔧 AI can generate and refactor Java code");
                    LOG.info("🐛 AI can debug and analyze errors");
                    LOG.info("⚙️ AI can manage configuration");
                } else {
                    LOG.severe("❌ Failed to apply NetBeans context to AI model");
                }
                
                return result;
                
            } catch (Exception e) {
                LOG.severe("Error applying NetBeans context: " + e.getMessage());
                return new ApplicationResult(
                    false,
                    "NetBeans Context Application",
                    "Error during application: " + e.getMessage(),
                    null,
                    null,
                    null
                );
            }
        });
    }
    
    /**
     * Gets the current AI model capabilities summary.
     * 
     * @return Capabilities summary
     */
    public CompletableFuture<CapabilitiesSummary> getAICapabilities() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var configManager = AIConfigurationManager.getInstance();
                var status = configManager.getConfigurationStatus().get();
                var validation = configManager.validateAIContext().get();
                
                CapabilitiesSummary summary = new CapabilitiesSummary(
                    status.isSuccess(),
                    validation.isValid(),
                    "AI Model Capabilities Summary"
                );
                
                // Add capability details
                summary.addCapability("File System Operations", "Read/write/create/delete any file", validation.getChecks().getOrDefault("File System Access", false));
                summary.addCapability("Project Management", "Open/build/configure NetBeans projects", validation.getChecks().getOrDefault("Project Management", false));
                summary.addCapability("Window Management", "Control all NetBeans windows", validation.getChecks().getOrDefault("Window Management", false));
                summary.addCapability("Editor Integration", "Read/modify code in editor", validation.getChecks().getOrDefault("Editor Integration", false));
                summary.addCapability("Code Generation", "Generate complete Java classes", validation.getChecks().getOrDefault("Code Generation", false));
                summary.addCapability("Refactoring", "Refactor and optimize code", validation.getChecks().getOrDefault("Refactoring", false));
                summary.addCapability("Debugging", "Analyze errors and suggest fixes", validation.getChecks().getOrDefault("Debugging", false));
                summary.addCapability("Testing", "Create unit and integration tests", validation.getChecks().getOrDefault("Testing", false));
                summary.addCapability("Documentation", "Generate comprehensive documentation", validation.getChecks().getOrDefault("Documentation", false));
                summary.addCapability("Configuration", "Manage Maven/Gradle settings", validation.getChecks().getOrDefault("Configuration", false));
                
                return summary;
                
            } catch (Exception e) {
                return new CapabilitiesSummary(
                    false,
                    false,
                    "Failed to get capabilities: " + e.getMessage()
                );
            }
        });
    }
    
    /**
     * Application result class.
     */
    public static class ApplicationResult {
        private final boolean success;
        private final String operation;
        private final String message;
        private final String systemPrompt;
        private final String completeContext;
        private final AIConfigurationManager.ValidationResult validation;
        
        public ApplicationResult(boolean success, String operation, String message, 
                                 String systemPrompt, String completeContext, 
                                 AIConfigurationManager.ValidationResult validation) {
            this.success = success;
            this.operation = operation;
            this.message = message;
            this.systemPrompt = systemPrompt;
            this.completeContext = completeContext;
            this.validation = validation;
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getOperation() { return operation; }
        public String getMessage() { return message; }
        public String getSystemPrompt() { return systemPrompt; }
        public String getCompleteContext() { return completeContext; }
        public AIConfigurationManager.ValidationResult getValidation() { return validation; }
    }
    
    /**
     * Capabilities summary class.
     */
    public static class CapabilitiesSummary {
        private final boolean configured;
        private final boolean valid;
        private final String title;
        private final java.util.Map<String, CapabilityDetail> capabilities = new java.util.HashMap<>();
        
        public CapabilitiesSummary(boolean configured, boolean valid, String title) {
            this.configured = configured;
            this.valid = valid;
            this.title = title;
        }
        
        public void addCapability(String name, String description, boolean enabled) {
            capabilities.put(name, new CapabilityDetail(name, description, enabled));
        }
        
        // Getters
        public boolean isConfigured() { return configured; }
        public boolean isValid() { return valid; }
        public String getTitle() { return title; }
        public java.util.Map<String, CapabilityDetail> getCapabilities() { return capabilities; }
        
        public static class CapabilityDetail {
            private final String name;
            private final String description;
            private final boolean enabled;
            
            public CapabilityDetail(String name, String description, boolean enabled) {
                this.name = name;
                this.description = description;
                this.enabled = enabled;
            }
            
            // Getters
            public String getName() { return name; }
            public String getDescription() { return description; }
            public boolean isEnabled() { return enabled; }
        }
    }
    
    /**
     * Private constructor for singleton.
     */
    private AIContextApplicator() {
        LOG.info("AIContextApplicator initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The AIContextApplicator instance
     */
    public static synchronized AIContextApplicator getInstance() {
        if (instance == null) {
            instance = new AIContextApplicator();
        }
        return instance;
    }
}
