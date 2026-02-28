package com.bajinho.continuebeans.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

/**
 * AI Context Provider - Explicitly shows the AI model what NetBeans capabilities are available.
 * This class provides clear documentation and examples of what the AI can do with NetBeans.
 * 
 * @author Continue Beans Team
 */
public class AIContextProvider {
    
    private static final Logger LOG = Logger.getLogger(AIContextProvider.class.getName());
    
    private static AIContextProvider instance;
    
    /**
     * AI capability definition with examples.
     */
    public static class AICapability {
        private final String capabilityId;
        private final String name;
        private final String description;
        private final List<String> availableActions;
        private final List<String> examples;
        private final boolean implemented;
        
        public AICapability(String capabilityId, String name, String description, 
                           List<String> availableActions, List<String> examples, boolean implemented) {
            this.capabilityId = capabilityId;
            this.name = name;
            this.description = description;
            this.availableActions = availableActions != null ? availableActions : new ArrayList<>();
            this.examples = examples != null ? examples : new ArrayList<>();
            this.implemented = implemented;
        }
        
        // Getters
        public String getCapabilityId() { return capabilityId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<String> getAvailableActions() { return availableActions; }
        public List<String> getExamples() { return examples; }
        public boolean isImplemented() { return implemented; }
    }
    
    /**
     * Private constructor for singleton.
     */
    private AIContextProvider() {
        LOG.info("AIContextProvider initialized - AI model can now understand NetBeans capabilities");
    }
    
    /**
     * Gets the singleton instance.
     * @return The AIContextProvider instance
     */
    public static synchronized AIContextProvider getInstance() {
        if (instance == null) {
            instance = new AIContextProvider();
        }
        return instance;
    }
    
    /**
     * Gets all AI capabilities with explicit examples.
     * @return All AI capabilities
     */
    public List<AICapability> getAllAICapabilities() {
        List<AICapability> capabilities = new ArrayList<>();
        
        // File System Capabilities
        capabilities.add(new AICapability(
            "file_operations", "File System Operations",
            "The AI can read, write, create, delete, and monitor files in the NetBeans virtual file system",
            List.of("read_file", "write_file", "create_file", "delete_file", "list_directory", "monitor_changes"),
            List.of(
                "Read the content of src/main/java/com/example/Main.java",
                "Create a new file called src/main/resources/config.properties",
                "Write configuration data to the project's pom.xml",
                "List all files in the src/test/java directory",
                "Monitor changes to the .gitignore file",
                "Delete temporary build files from target/ directory"
            ),
            true
        ));
        
        // Project Management Capabilities
        capabilities.add(new AICapability(
            "project_management", "Project Management",
            "The AI can manage NetBeans projects, including opening, building, and configuring projects",
            List.of("open_project", "close_project", "build_project", "clean_project", "get_project_info", "configure_project"),
            List.of(
                "Open the continue-beans project in NetBeans",
                "Build the current Maven project",
                "Clean the project build artifacts",
                "Get project metadata and dependencies",
                "Configure project properties and settings",
                "Add new dependencies to pom.xml",
                "Create a new Java project with Maven"
            ),
            true
        ));
        
        // Window Management Capabilities
        capabilities.add(new AICapability(
            "window_management", "Window Management",
            "The AI can control NetBeans windows, including opening, closing, and arranging TopComponents",
            List.of("open_window", "close_window", "activate_window", "dock_window", "create_custom_window"),
            List.of(
                "Open a new editor window for the Main.java file",
                "Close the output window",
                "Activate the Projects window",
                "Dock the editor window to the left side",
                "Create a custom window for AI assistant",
                "Arrange windows in a specific layout",
                "Show the NetBeans console window"
            ),
            true
        ));
        
        // Editor Integration Capabilities
        capabilities.add(new AICapability(
            "editor_integration", "Editor Integration",
            "The AI can read and modify code in the NetBeans editor, including syntax highlighting and code completion",
            List.of("read_editor_content", "write_editor_content", "get_cursor_position", "insert_text", "replace_text"),
            List.of(
                "Read the current content of the active editor",
                "Insert a new method at the cursor position",
                "Replace the selected text with refactored code",
                "Get the current line and column of the cursor",
                "Add import statements to the top of the file",
                "Format the code in the editor",
                "Add comments to the current method"
            ),
            true
        ));
        
        // Code Generation Capabilities
        capabilities.add(new AICapability(
            "code_generation", "Code Generation",
            "The AI can generate Java code, classes, methods, and complete project structures",
            List.of("generate_class", "generate_method", "generate_interface", "generate_test", "generate_documentation"),
            List.of(
                "Generate a new Java class called UserService",
                "Create a method to validate user input",
                "Generate a REST controller class",
                "Create unit tests for the UserService class",
                "Generate JavaDoc documentation",
                "Create an interface for data access",
                "Generate a complete Spring Boot application structure"
            ),
            true
        ));
        
        // Refactoring Capabilities
        capabilities.add(new AICapability(
            "refactoring", "Code Refactoring",
            "The AI can refactor code, including renaming, extracting methods, and improving structure",
            List.of("rename_class", "extract_method", "inline_variable", "move_class", "optimize_imports"),
            List.of(
                "Rename the UserService class to UserManagementService",
                "Extract the validation logic into a separate method",
                "Inline the temporary variable in the calculate method",
                "Move the utility class to the utils package",
                "Optimize and organize import statements",
                "Convert anonymous class to lambda expression",
                "Extract interface from implementation class"
            ),
            true
        ));
        
        // Debugging Capabilities
        capabilities.add(new AICapability(
            "debugging", "Debugging Support",
            "The AI can help debug code by analyzing errors, suggesting fixes, and adding logging",
            List.of("analyze_error", "suggest_fix", "add_logging", "find_bug", "explain_exception"),
            List.of(
                "Analyze the NullPointerException in line 45",
                "Suggest a fix for the compilation error",
                "Add debug logging to the process method",
                "Find the bug causing the infinite loop",
                "Explain the meaning of the stack trace",
                "Add try-catch blocks for error handling",
                "Identify potential null pointer exceptions"
            ),
            true
        ));
        
        // Testing Capabilities
        capabilities.add(new AICapability(
            "testing", "Testing Support",
            "The AI can create unit tests, integration tests, and test data for Java applications",
            List.of("create_unit_test", "create_integration_test", "generate_test_data", "mock_dependencies"),
            List.of(
                "Create unit tests for the Calculator class",
                "Generate integration tests for the REST API",
                "Create test data for the user service",
                "Mock external dependencies in tests",
                "Generate parameterized tests",
                "Create test fixtures and setup methods",
                "Add assertions to existing tests"
            ),
            true
        ));
        
        // Documentation Capabilities
        capabilities.add(new AICapability(
            "documentation", "Documentation Generation",
            "The AI can generate comprehensive documentation including JavaDoc, README files, and API docs",
            List.of("generate_javadoc", "create_readme", "document_api", "generate_changelog"),
            List.of(
                "Generate JavaDoc for the UserService class",
                "Create a comprehensive README.md file",
                "Document the REST API endpoints",
                "Generate API documentation from code",
                "Create a changelog for the latest version",
                "Document the project architecture",
                "Generate user guide documentation"
            ),
            true
        ));
        
        // Configuration Capabilities
        capabilities.add(new AICapability(
            "configuration", "Configuration Management",
            "The AI can manage project configuration files including Maven, Gradle, and application properties",
            List.of("update_pom", "configure_properties", "setup_logging", "configure_database"),
            List.of(
                "Add Spring Boot dependency to pom.xml",
                "Configure application.properties for database connection",
                "Setup logging configuration in logback.xml",
                "Configure Maven plugins for code quality",
                "Set up Gradle build configuration",
                "Configure development and production profiles",
                "Add environment-specific configuration"
            ),
            true
        ));
        
        return capabilities;
    }
    
    /**
     * Gets AI context for NetBeans integration.
     * @return Complete AI context
     */
    public CompletableFuture<Map<String, Object>> getAIContext() {
        return CompletableFuture.supplyAsync(() -> {
            Map<String, Object> context = new HashMap<>();
            
            context.put("ai_model_capabilities", "The AI model has full access to NetBeans Platform APIs");
            context.put("integration_level", "COMPLETE - Enterprise-grade NetBeans integration");
            context.put("available_apis", getAllAICapabilities().size());
            
            // Explicit capabilities for the AI to understand
            Map<String, Object> explicitCapabilities = new HashMap<>();
            explicitCapabilities.put("file_system", "Can read/write/create/delete any file in the project");
            explicitCapabilities.put("project_management", "Can open/build/configure NetBeans projects");
            explicitCapabilities.put("window_management", "Can control all NetBeans windows and TopComponents");
            explicitCapabilities.put("editor_integration", "Can read/modify code in the NetBeans editor");
            explicitCapabilities.put("code_generation", "Can generate complete Java classes and projects");
            explicitCapabilities.put("refactoring", "Can refactor and improve existing code");
            explicitCapabilities.put("debugging", "Can analyze errors and suggest fixes");
            explicitCapabilities.put("testing", "Can create unit tests and integration tests");
            explicitCapabilities.put("documentation", "Can generate comprehensive documentation");
            explicitCapabilities.put("configuration", "Can manage Maven/Gradle and application configuration");
            
            context.put("explicit_capabilities", explicitCapabilities);
            
            // AI instructions
            List<String> aiInstructions = new ArrayList<>();
            aiInstructions.add("You have FULL access to NetBeans Platform APIs");
            aiInstructions.add("You can read, write, create, and delete any file in the project");
            aiInstructions.add("You can control NetBeans windows, editors, and UI components");
            aiInstructions.add("You can generate, modify, and refactor Java code");
            aiInstructions.add("You can manage project configuration and dependencies");
            aiInstructions.add("You can create tests, documentation, and build configurations");
            aiInstructions.add("You should proactively suggest improvements and optimizations");
            aiInstructions.add("You can analyze errors and provide specific fixes");
            aiInstructions.add("You can interact with the user through NetBeans UI components");
            
            context.put("ai_instructions", aiInstructions);
            
            // Example commands the AI can execute
            List<String> exampleCommands = new ArrayList<>();
            exampleCommands.add("Read the content of src/main/java/com/example/Main.java");
            exampleCommands.add("Create a new UserService class with CRUD operations");
            exampleCommands.add("Add Spring Boot dependency to pom.xml");
            exampleCommands.add("Generate unit tests for the Calculator class");
            exampleCommands.add("Refactor the long method into smaller methods");
            exampleCommands.add("Create a README.md file for the project");
            exampleCommands.add("Configure application.properties for database connection");
            exampleCommands.add("Open the Projects window and show the continue-beans project");
            
            context.put("example_commands", exampleCommands);
            
            context.put("status", "AI CONTEXT READY - Full NetBeans Platform Access Available");
            context.put("message", "The AI model now understands it has complete access to NetBeans Platform and can interact with all IDE features");
            
            return context;
        });
    }
    
    /**
     * Gets AI capability summary.
     * @return Capability summary
     */
    public Map<String, Object> getCapabilitySummary() {
        Map<String, Object> summary = new HashMap<>();
        
        List<AICapability> capabilities = getAllAICapabilities();
        summary.put("total_capabilities", capabilities.size());
        summary.put("implemented_capabilities", capabilities.stream().mapToInt(c -> c.isImplemented() ? 1 : 0).sum());
        
        List<String> capabilityNames = new ArrayList<>();
        for (AICapability cap : capabilities) {
            if (cap.isImplemented()) {
                capabilityNames.add(cap.getName());
            }
        }
        summary.put("capability_names", capabilityNames);
        
        summary.put("integration_level", "COMPLETE");
        summary.put("ai_understanding", "The AI model now understands it has full NetBeans Platform access");
        
        return summary;
    }
}
