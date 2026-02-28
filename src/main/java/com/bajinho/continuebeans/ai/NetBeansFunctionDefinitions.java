package com.bajinho.continuebeans.ai;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * NetBeans Function Definitions for OpenAI Function Calling.
 * Defines all functions that the AI model can call to interact with NetBeans.
 * 
 * @author Continue Beans Team
 */
public class NetBeansFunctionDefinitions {
    
    /**
     * Gets all available NetBeans functions for AI to call.
     * 
     * @return List of function definitions
     */
    public static List<FunctionDefinition> getAllFunctions() {
        return List.of(
            // File System Operations
            createReadFileFunction(),
            createCreateFileFunction(),
            createUpdateFileFunction(),
            createDeleteFileFunction(),
            createListDirectoryFunction(),
            
            // Project Management
            createBuildProjectFunction(),
            createOpenProjectFunction(),
            createGetProjectInfoFunction(),
            createCreateProjectFunction(),
            
            // Window Management
            createOpenEditorFunction(),
            createOpenProjectsWindowFunction(),
            createOpenOutputWindowFunction(),
            createCloseWindowFunction(),
            createGetActiveWindowsFunction(),
            
            // Code Generation
            createGenerateClassFunction(),
            createGenerateInterfaceFunction(),
            createGenerateEnumFunction(),
            createGenerateTestMethodFunction(),
            createGenerateDocumentationFunction(),
            
            // Code Analysis
            createAnalyzeCodeFunction(),
            createGetSyntaxErrorsFunction(),
            createGetCodeMetricsFunction(),
            createRefactorCodeFunction(),
            
            // Configuration Management
            createAddDependencyFunction(),
            createUpdatePropertiesFunction(),
            createGetProjectConfigFunction(),
            createSetProjectConfigFunction()
        );
    }
    
    /**
     * File System Functions
     */
    private static FunctionDefinition createReadFileFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filePath", "string - Path to the file to read (relative to project root)");
        parameters.put("encoding", "string - File encoding (optional, default: UTF-8)");
        
        return new FunctionDefinition(
            "read_file",
            "Read the complete content of any file in the NetBeans project",
            parameters,
            "Returns the file content as text"
        );
    }
    
    private static FunctionDefinition createCreateFileFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filePath", "string - Path where to create the file");
        parameters.put("content", "string - Content to write to the file");
        parameters.put("overwrite", "boolean - Whether to overwrite if file exists (optional, default: false)");
        
        return new FunctionDefinition(
            "create_file",
            "Create a new file with the specified content in the NetBeans project",
            parameters,
            "Returns success status and file path"
        );
    }
    
    private static FunctionDefinition createUpdateFileFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filePath", "string - Path to the file to update");
        parameters.put("content", "string - New content for the file");
        parameters.put("append", "boolean - Whether to append to existing content (optional, default: false)");
        
        return new FunctionDefinition(
            "update_file",
            "Update the content of an existing file in the NetBeans project",
            parameters,
            "Returns success status and updated file info"
        );
    }
    
    private static FunctionDefinition createDeleteFileFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filePath", "string - Path to the file to delete");
        parameters.put("confirm", "boolean - Confirmation required for safety (optional, default: false)");
        
        return new FunctionDefinition(
            "delete_file",
            "Delete a file from the NetBeans project",
            parameters,
            "Returns success status and deleted file info"
        );
    }
    
    private static FunctionDefinition createListDirectoryFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("directoryPath", "string - Path to the directory to list (optional, default: project root)");
        parameters.put("recursive", "boolean - Whether to list recursively (optional, default: false)");
        parameters.put("includeHidden", "boolean - Whether to include hidden files (optional, default: false)");
        
        return new FunctionDefinition(
            "list_directory",
            "List files and directories in a NetBeans project directory",
            parameters,
            "Returns list of files and directories with metadata"
        );
    }
    
    /**
     * Project Management Functions
     */
    private static FunctionDefinition createBuildProjectFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("projectPath", "string - Path to the project to build (optional, default: current project)");
        parameters.put("goals", "array - Maven goals to run (optional, default: ['clean', 'install'])");
        parameters.put("profiles", "array - Maven profiles to activate (optional)");
        
        return new FunctionDefinition(
            "build_project",
            "Build a NetBeans Maven project with specified goals",
            parameters,
            "Returns build results, logs, and status"
        );
    }
    
    private static FunctionDefinition createOpenProjectFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("projectPath", "string - Path to the NetBeans project to open");
        
        return new FunctionDefinition(
            "open_project",
            "Open a NetBeans project in the IDE",
            parameters,
            "Returns project opening status and project info"
        );
    }
    
    private static FunctionDefinition createGetProjectInfoFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("projectPath", "string - Path to the project (optional, default: current project)");
        
        return new FunctionDefinition(
            "get_project_info",
            "Get detailed information about a NetBeans project",
            parameters,
            "Returns project metadata, dependencies, structure, and configuration"
        );
    }
    
    private static FunctionDefinition createCreateProjectFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("projectName", "string - Name of the new project");
        parameters.put("projectType", "string - Project type (maven, gradle, ant)");
        parameters.put("packageName", "string - Base package name");
        parameters.put("location", "string - Where to create the project");
        
        return new FunctionDefinition(
            "create_project",
            "Create a new NetBeans project with specified configuration",
            parameters,
            "Returns created project information and status"
        );
    }
    
    /**
     * Window Management Functions
     */
    private static FunctionDefinition createOpenEditorFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filePath", "string - Path to the file to open in editor");
        parameters.put("lineNumber", "integer - Line number to position cursor (optional)");
        parameters.put("focus", "boolean - Whether to focus the editor (optional, default: true)");
        
        return new FunctionDefinition(
            "open_editor",
            "Open a file in the NetBeans editor and optionally position cursor",
            parameters,
            "Returns editor opening status and file info"
        );
    }
    
    private static FunctionDefinition createOpenProjectsWindowFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("focus", "boolean - Whether to focus the window (optional, default: true)");
        
        return new FunctionDefinition(
            "open_projects_window",
            "Open the NetBeans Projects window",
            parameters,
            "Returns window opening status"
        );
    }
    
    private static FunctionDefinition createOpenOutputWindowFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("tab", "string - Output tab to open (build, debug, find, etc.)");
        
        return new FunctionDefinition(
            "open_output_window",
            "Open the NetBeans Output window with specified tab",
            parameters,
            "Returns window opening status"
        );
    }
    
    private static FunctionDefinition createCloseWindowFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("windowTitle", "string - Title of the window to close");
        parameters.put("force", "boolean - Whether to force close without saving (optional, default: false)");
        
        return new FunctionDefinition(
            "close_window",
            "Close a specific NetBeans window",
            parameters,
            "Returns window closing status"
        );
    }
    
    private static FunctionDefinition createGetActiveWindowsFunction() {
        Map<String, Object> parameters = new HashMap<>();
        
        return new FunctionDefinition(
            "get_active_windows",
            "Get list of all currently open NetBeans windows",
            parameters,
            "Returns list of active windows with their types and states"
        );
    }
    
    /**
     * Code Generation Functions
     */
    private static FunctionDefinition createGenerateClassFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("className", "string - Name of the class to generate");
        parameters.put("packageName", "string - Package name for the class");
        parameters.put("fields", "array - List of field definitions (name, type, visibility)");
        parameters.put("methods", "array - List of method definitions (name, parameters, return type)");
        parameters.put("extendsClass", "string - Class to extend (optional)");
        parameters.put("implements", "array - Interfaces to implement (optional)");
        parameters.put("annotations", "array - Class annotations (optional)");
        
        return new FunctionDefinition(
            "generate_class",
            "Generate a complete Java class with specified fields and methods",
            parameters,
            "Returns generated class content and file path"
        );
    }
    
    private static FunctionDefinition createGenerateInterfaceFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("interfaceName", "string - Name of the interface to generate");
        parameters.put("packageName", "string - Package name for the interface");
        parameters.put("methods", "array - List of method signatures");
        parameters.put("annotations", "array - Interface annotations (optional)");
        
        return new FunctionDefinition(
            "generate_interface",
            "Generate a Java interface with specified method signatures",
            parameters,
            "Returns generated interface content and file path"
        );
    }
    
    private static FunctionDefinition createGenerateEnumFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("enumName", "string - Name of the enum to generate");
        parameters.put("packageName", "string - Package name for the enum");
        parameters.put("values", "array - List of enum values");
        parameters.put("fields", "array - Enum fields (optional)");
        parameters.put("methods", "array - Enum methods (optional)");
        
        return new FunctionDefinition(
            "generate_enum",
            "Generate a Java enum with specified values and methods",
            parameters,
            "Returns generated enum content and file path"
        );
    }
    
    private static FunctionDefinition createGenerateTestMethodFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("className", "string - Name of the class to test");
        parameters.put("testMethods", "array - List of test methods to generate");
        parameters.put("testFramework", "string - Test framework (junit, testng) (optional, default: junit)");
        parameters.put("mockClasses", "array - Classes to mock (optional)");
        
        return new FunctionDefinition(
            "generate_test_method",
            "Generate unit test methods for a specified class",
            parameters,
            "Returns generated test content and file path"
        );
    }
    
    private static FunctionDefinition createGenerateDocumentationFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("target", "string - What to document (class, method, project)");
        parameters.put("format", "string - Documentation format (javadoc, markdown) (optional, default: javadoc)");
        parameters.put("includeExamples", "boolean - Whether to include usage examples (optional, default: true)");
        
        return new FunctionDefinition(
            "generate_documentation",
            "Generate documentation for classes, methods, or entire project",
            parameters,
            "Returns generated documentation content"
        );
    }
    
    /**
     * Code Analysis Functions
     */
    private static FunctionDefinition createAnalyzeCodeFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filePath", "string - Path to the file to analyze");
        parameters.put("analysisType", "string - Type of analysis (complexity, security, performance, style)");
        parameters.put("includeSuggestions", "boolean - Whether to include improvement suggestions (optional, default: true)");
        
        return new FunctionDefinition(
            "analyze_code",
            "Analyze code for complexity, security, performance, or style issues",
            parameters,
            "Returns analysis results and suggestions"
        );
    }
    
    private static FunctionDefinition createGetSyntaxErrorsFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filePath", "string - Path to the file to check (optional, default: all files)");
        parameters.put("severity", "string - Minimum severity level (error, warning, info) (optional, default: error)");
        
        return new FunctionDefinition(
            "get_syntax_errors",
            "Get syntax errors and warnings from NetBeans compiler",
            parameters,
            "Returns list of syntax errors with locations and descriptions"
        );
    }
    
    private static FunctionDefinition createGetCodeMetricsFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filePath", "string - Path to the file or directory to analyze");
        parameters.put("metrics", "array - List of metrics to calculate (loc, complexity, coverage, etc.)");
        
        return new FunctionDefinition(
            "get_code_metrics",
            "Calculate code metrics for files or directories",
            parameters,
            "Returns calculated metrics with detailed breakdown"
        );
    }
    
    private static FunctionDefinition createRefactorCodeFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("filePath", "string - Path to the file to refactor");
        parameters.put("refactoringType", "string - Type of refactoring (extract_method, rename_class, optimize_imports)");
        parameters.put("targetElement", "string - Specific element to refactor (method name, class name, etc.)");
        parameters.put("newName", "string - New name for rename refactoring (if applicable)");
        
        return new FunctionDefinition(
            "refactor_code",
            "Apply automated refactoring to code",
            parameters,
            "Returns refactoring results and modified file content"
        );
    }
    
    /**
     * Configuration Management Functions
     */
    private static FunctionDefinition createAddDependencyFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("groupId", "string - Maven groupId of the dependency");
        parameters.put("artifactId", "string - Maven artifactId of the dependency");
        parameters.put("version", "string - Version of the dependency (optional)");
        parameters.put("scope", "string - Dependency scope (compile, test, provided) (optional, default: compile)");
        
        return new FunctionDefinition(
            "add_dependency",
            "Add a Maven dependency to the project",
            parameters,
            "Returns dependency addition status and updated pom.xml"
        );
    }
    
    private static FunctionDefinition createUpdatePropertiesFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("propertiesFile", "string - Path to properties file");
        parameters.put("properties", "object - Key-value pairs of properties to update");
        parameters.put("createFile", "boolean - Whether to create file if it doesn't exist (optional, default: true)");
        
        return new FunctionDefinition(
            "update_properties",
            "Update or add properties in a properties file",
            parameters,
            "Returns properties update status and file content"
        );
    }
    
    private static FunctionDefinition createGetProjectConfigFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "string - Type of configuration (pom, gradle, properties)");
        parameters.put("projectPath", "string - Path to the project (optional, default: current project)");
        
        return new FunctionDefinition(
            "get_project_config",
            "Get project configuration (pom.xml, build.gradle, etc.)",
            parameters,
            "Returns configuration file content and metadata"
        );
    }
    
    private static FunctionDefinition createSetProjectConfigFunction() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("configType", "string - Type of configuration (pom, gradle, properties)");
        parameters.put("content", "string - New configuration content");
        parameters.put("projectPath", "string - Path to the project (optional, default: current project)");
        
        return new FunctionDefinition(
            "set_project_config",
            "Update project configuration file",
            parameters,
            "Returns configuration update status and file info"
        );
    }
    
    /**
     * Function Definition class
     */
    public static class FunctionDefinition {
        private final String name;
        private final String description;
        private final Map<String, Object> parameters;
        private final String returns;
        
        public FunctionDefinition(String name, String description, Map<String, Object> parameters, String returns) {
            this.name = name;
            this.description = description;
            this.parameters = parameters;
            this.returns = returns;
        }
        
        // Getters
        public String getName() { return name; }
        public String getDescription() { return description; }
        public Map<String, Object> getParameters() { return parameters; }
        public String getReturns() { return returns; }
    }
}
