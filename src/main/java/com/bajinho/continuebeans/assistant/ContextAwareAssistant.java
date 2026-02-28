package com.bajinho.continuebeans.assistant;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bajinho.continuebeans.filesystem.ProjectAnalyzer;
import com.bajinho.continuebeans.editor.IntelligentCodeEditor;
import com.bajinho.continuebeans.automation.TemplateEngine;
import com.bajinho.continuebeans.automation.WorkflowEngine;

/**
 * Context-aware assistant with deep project understanding, personalized suggestions,
 * intelligent code generation, and advanced AI integration.
 * 
 * @author Continue Beans Team
 */
public class ContextAwareAssistant {
    
    private static final Logger LOG = Logger.getLogger(ContextAwareAssistant.class.getName());
    
    private static ContextAwareAssistant instance;
    
    private final Map<String, ProjectContext> projectContexts;
    private final Map<String, UserContext> userContexts;
    private final List<AssistantListener> listeners;
    private final ProjectAnalyzer projectAnalyzer;
    private final IntelligentCodeEditor codeEditor;
    private final TemplateEngine templateEngine;
    private final WorkflowEngine workflowEngine;
    private AIAssistant aiAssistant;
    private final SuggestionEngine suggestionEngine;
    private final ContextCache contextCache;
    
    /**
     * Represents project context information.
     */
    public static class ProjectContext {
        private final String projectPath;
        private final String projectName;
        private final String projectType;
        private final List<String> sourceFiles;
        private final List<String> dependencies;
        private final Map<String, Object> metadata;
        private final CodeStructure codeStructure;
        private final ProjectMetrics metrics;
        private final long lastUpdated;
        
        public ProjectContext(String projectPath, String projectName, String projectType,
                            List<String> sourceFiles, List<String> dependencies,
                            Map<String, Object> metadata, CodeStructure codeStructure,
                            ProjectMetrics metrics) {
            this.projectPath = projectPath;
            this.projectName = projectName;
            this.projectType = projectType;
            this.sourceFiles = sourceFiles != null ? sourceFiles : new ArrayList<>();
            this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
            this.metadata = metadata != null ? metadata : new HashMap<>();
            this.codeStructure = codeStructure;
            this.metrics = metrics;
            this.lastUpdated = System.currentTimeMillis();
        }
        
        // Getters
        public String getProjectPath() { return projectPath; }
        public String getProjectName() { return projectName; }
        public String getProjectType() { return projectType; }
        public List<String> getSourceFiles() { return sourceFiles; }
        public List<String> getDependencies() { return dependencies; }
        public Map<String, Object> getMetadata() { return metadata; }
        public CodeStructure getCodeStructure() { return codeStructure; }
        public ProjectMetrics getMetrics() { return metrics; }
        public long getLastUpdated() { return lastUpdated; }
    }
    
    /**
     * Code structure information.
     */
    public static class CodeStructure {
        private final Map<String, ClassInfo> classes;
        private final Map<String, MethodInfo> methods;
        private final Map<String, VariableInfo> variables;
        private final List<String> packages;
        private final Map<String, List<String>> imports;
        
        public CodeStructure(Map<String, ClassInfo> classes, Map<String, MethodInfo> methods,
                           Map<String, VariableInfo> variables, List<String> packages,
                           Map<String, List<String>> imports) {
            this.classes = classes != null ? classes : new HashMap<>();
            this.methods = methods != null ? methods : new HashMap<>();
            this.variables = variables != null ? variables : new HashMap<>();
            this.packages = packages != null ? packages : new ArrayList<>();
            this.imports = imports != null ? imports : new HashMap<>();
        }
        
        // Getters
        public Map<String, ClassInfo> getClasses() { return classes; }
        public Map<String, MethodInfo> getMethods() { return methods; }
        public Map<String, VariableInfo> getVariables() { return variables; }
        public List<String> getPackages() { return packages; }
        public Map<String, List<String>> getImports() { return imports; }
    }
    
    /**
     * Class information.
     */
    public static class ClassInfo {
        private final String name;
        private final String packageName;
        private final String superClass;
        private final List<String> interfaces;
        private final List<String> methods;
        private final List<String> fields;
        private final String filePath;
        private final int lineNumber;
        
        public ClassInfo(String name, String packageName, String superClass,
                        List<String> interfaces, List<String> methods, List<String> fields,
                        String filePath, int lineNumber) {
            this.name = name;
            this.packageName = packageName;
            this.superClass = superClass;
            this.interfaces = interfaces != null ? interfaces : new ArrayList<>();
            this.methods = methods != null ? methods : new ArrayList<>();
            this.fields = fields != null ? fields : new ArrayList<>();
            this.filePath = filePath;
            this.lineNumber = lineNumber;
        }
        
        // Getters
        public String getName() { return name; }
        public String getPackageName() { return packageName; }
        public String getSuperClass() { return superClass; }
        public List<String> getInterfaces() { return interfaces; }
        public List<String> getMethods() { return methods; }
        public List<String> getFields() { return fields; }
        public String getFilePath() { return filePath; }
        public int getLineNumber() { return lineNumber; }
    }
    
    /**
     * Method information.
     */
    public static class MethodInfo {
        private final String name;
        private final String className;
        private final String returnType;
        private final List<String> parameters;
        private final List<String> annotations;
        private final boolean isStatic;
        private final boolean isPublic;
        private final String filePath;
        private final int lineNumber;
        
        public MethodInfo(String name, String className, String returnType,
                        List<String> parameters, List<String> annotations,
                        boolean isStatic, boolean isPublic, String filePath, int lineNumber) {
            this.name = name;
            this.className = className;
            this.returnType = returnType;
            this.parameters = parameters != null ? parameters : new ArrayList<>();
            this.annotations = annotations != null ? annotations : new ArrayList<>();
            this.isStatic = isStatic;
            this.isPublic = isPublic;
            this.filePath = filePath;
            this.lineNumber = lineNumber;
        }
        
        // Getters
        public String getName() { return name; }
        public String getClassName() { return className; }
        public String getReturnType() { return returnType; }
        public List<String> getParameters() { return parameters; }
        public List<String> getAnnotations() { return annotations; }
        public boolean isStatic() { return isStatic; }
        public boolean isPublic() { return isPublic; }
        public String getFilePath() { return filePath; }
        public int getLineNumber() { return lineNumber; }
    }
    
    /**
     * Variable information.
     */
    public static class VariableInfo {
        private final String name;
        private final String type;
        private final String className;
        private final boolean isStatic;
        private final boolean isFinal;
        private final String filePath;
        private final int lineNumber;
        
        public VariableInfo(String name, String type, String className,
                          boolean isStatic, boolean isFinal, String filePath, int lineNumber) {
            this.name = name;
            this.type = type;
            this.className = className;
            this.isStatic = isStatic;
            this.isFinal = isFinal;
            this.filePath = filePath;
            this.lineNumber = lineNumber;
        }
        
        // Getters
        public String getName() { return name; }
        public String getType() { return type; }
        public String getClassName() { return className; }
        public boolean isStatic() { return isStatic; }
        public boolean isFinal() { return isFinal; }
        public String getFilePath() { return filePath; }
        public int getLineNumber() { return lineNumber; }
    }
    
    /**
     * Project metrics.
     */
    public static class ProjectMetrics {
        private final int totalFiles;
        private final int totalLines;
        private final int totalClasses;
        private final int totalMethods;
        private final double complexity;
        private final double maintainability;
        private final int testCoverage;
        
        public ProjectMetrics(int totalFiles, int totalLines, int totalClasses, int totalMethods,
                            double complexity, double maintainability, int testCoverage) {
            this.totalFiles = totalFiles;
            this.totalLines = totalLines;
            this.totalClasses = totalClasses;
            this.totalMethods = totalMethods;
            this.complexity = complexity;
            this.maintainability = maintainability;
            this.testCoverage = testCoverage;
        }
        
        // Getters
        public int getTotalFiles() { return totalFiles; }
        public int getTotalLines() { return totalLines; }
        public int getTotalClasses() { return totalClasses; }
        public int getTotalMethods() { return totalMethods; }
        public double getComplexity() { return complexity; }
        public double getMaintainability() { return maintainability; }
        public int getTestCoverage() { return testCoverage; }
    }
    
    /**
     * User context information.
     */
    public static class UserContext {
        private final String userId;
        private final Map<String, Object> preferences;
        private final List<String> recentFiles;
        private final List<String> recentCommands;
        private final Map<String, Integer> commandFrequency;
        private final List<String> favoritePatterns;
        private final Map<String, Object> learningData;
        private final long lastActive;
        
        public UserContext(String userId) {
            this.userId = userId;
            this.preferences = new HashMap<>();
            this.recentFiles = new ArrayList<>();
            this.recentCommands = new ArrayList<>();
            this.commandFrequency = new HashMap<>();
            this.favoritePatterns = new ArrayList<>();
            this.learningData = new HashMap<>();
            this.lastActive = System.currentTimeMillis();
        }
        
        // Getters and setters
        public String getUserId() { return userId; }
        public Map<String, Object> getPreferences() { return preferences; }
        public List<String> getRecentFiles() { return recentFiles; }
        public List<String> getRecentCommands() { return recentCommands; }
        public Map<String, Integer> getCommandFrequency() { return commandFrequency; }
        public List<String> getFavoritePatterns() { return favoritePatterns; }
        public Map<String, Object> getLearningData() { return learningData; }
        public long getLastActive() { return lastActive; }
        
        public void addRecentFile(String filePath) {
            recentFiles.remove(filePath);
            recentFiles.add(0, filePath);
            if (recentFiles.size() > 20) {
                recentFiles.remove(recentFiles.size() - 1);
            }
        }
        
        public void addRecentCommand(String command) {
            recentCommands.remove(command);
            recentCommands.add(0, command);
            if (recentCommands.size() > 50) {
                recentCommands.remove(recentCommands.size() - 1);
            }
            commandFrequency.put(command, commandFrequency.getOrDefault(command, 0) + 1);
        }
    }
    
    /**
     * Assistant suggestion.
     */
    public static class AssistantSuggestion {
        private final String suggestionId;
        private final String type;
        private final String title;
        private final String description;
        private final String action;
        private final Map<String, Object> parameters;
        private final double confidence;
        private final String context;
        private final long timestamp;
        
        public AssistantSuggestion(String suggestionId, String type, String title, String description,
                                  String action, Map<String, Object> parameters, double confidence,
                                  String context) {
            this.suggestionId = suggestionId;
            this.type = type;
            this.title = title;
            this.description = description;
            this.action = action;
            this.parameters = parameters != null ? parameters : new HashMap<>();
            this.confidence = confidence;
            this.context = context;
            this.timestamp = System.currentTimeMillis();
        }
        
        // Getters
        public String getSuggestionId() { return suggestionId; }
        public String getType() { return type; }
        public String getTitle() { return title; }
        public String getDescription() { return description; }
        public String getAction() { return action; }
        public Map<String, Object> getParameters() { return parameters; }
        public double getConfidence() { return confidence; }
        public String getContext() { return context; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * AI assistant interface.
     */
    public static class AIAssistant {
        private final boolean enabled;
        private final String provider;
        private final Map<String, Object> configuration;
        
        public AIAssistant(boolean enabled, String provider, Map<String, Object> configuration) {
            this.enabled = enabled;
            this.provider = provider;
            this.configuration = configuration != null ? configuration : new HashMap<>();
        }
        
        /**
         * Generates AI-powered suggestions.
         * @param context The combined context
         * @param query The user query
         * @return CompletableFuture with suggestions
         */
        public CompletableFuture<List<AssistantSuggestion>> generateSuggestions(Context context, String query) {
            return CompletableFuture.supplyAsync(() -> {
                if (!enabled) {
                    return new ArrayList<>();
                }
                
                // TODO: Implement AI integration
                return new ArrayList<>();
            });
        }
        
        /**
         * Generates code with AI.
         * @param context The context
         * @param prompt The code generation prompt
         * @return CompletableFuture with generated code
         */
        public CompletableFuture<String> generateCode(Context context, String prompt) {
            return CompletableFuture.supplyAsync(() -> {
                if (!enabled) {
                    return "// AI is disabled";
                }
                
                // TODO: Implement AI code generation
                return "// AI generated code for: " + prompt;
            });
        }
        
        /**
         * Analyzes code with AI.
         * @param context The context
         * @param code The code to analyze
         * @return CompletableFuture with analysis
         */
        public CompletableFuture<String> analyzeCode(Context context, String code) {
            return CompletableFuture.supplyAsync(() -> {
                if (!enabled) {
                    return "AI analysis is disabled";
                }
                
                // TODO: Implement AI code analysis
                return "AI analysis for provided code";
            });
        }
        
        // Getters
        public boolean isEnabled() { return enabled; }
        public String getProvider() { return provider; }
        public Map<String, Object> getConfiguration() { return configuration; }
    }
    
    /**
     * Suggestion engine.
     */
    public static class SuggestionEngine {
        private final List<SuggestionProvider> providers;
        
        public SuggestionEngine() {
            this.providers = new ArrayList<>();
            initializeDefaultProviders();
        }
        
        /**
         * Initializes default suggestion providers.
         */
        private void initializeDefaultProviders() {
            // TODO: Initialize default suggestion providers
        }
        
        /**
         * Generates suggestions based on context.
         * @param context The context
         * @return List of suggestions
         */
        public List<AssistantSuggestion> generateSuggestions(Context context) {
            List<AssistantSuggestion> suggestions = new ArrayList<>();
            
            for (SuggestionProvider provider : providers) {
                try {
                    suggestions.addAll(provider.generateSuggestions(context));
                } catch (Exception e) {
                    LOG.log(Level.WARNING, "Error generating suggestions with provider: " + provider.getClass().getSimpleName(), e);
                }
            }
            
            return suggestions;
        }
        
        /**
         * Adds a suggestion provider.
         * @param provider The provider to add
         */
        public void addProvider(SuggestionProvider provider) {
            providers.add(provider);
        }
    }
    
    /**
     * Suggestion provider interface.
     */
    public interface SuggestionProvider {
        List<AssistantSuggestion> generateSuggestions(Context context);
        String getProviderName();
    }
    
    /**
     * Context cache.
     */
    public static class ContextCache {
        private final Map<String, CachedContext> cache;
        private final long maxAge;
        private final int maxSize;
        
        public ContextCache(long maxAge, int maxSize) {
            this.cache = new ConcurrentHashMap<>();
            this.maxAge = maxAge;
            this.maxSize = maxSize;
        }
        
        /**
         * Gets cached context.
         * @param key The cache key
         * @return Cached context or null
         */
        public CachedContext get(String key) {
            CachedContext cached = cache.get(key);
            if (cached != null && (System.currentTimeMillis() - cached.getTimestamp()) < maxAge) {
                return cached;
            }
            return null;
        }
        
        /**
         * Puts context in cache.
         * @param key The cache key
         * @param context The context to cache
         */
        public void put(String key, Context context) {
            if (cache.size() >= maxSize) {
                // Remove oldest entry
                String oldestKey = cache.keySet().iterator().next();
                cache.remove(oldestKey);
            }
            cache.put(key, new CachedContext(context, System.currentTimeMillis()));
        }
        
        /**
         * Clears the cache.
         */
        public void clear() {
            cache.clear();
        }
    }
    
    /**
     * Cached context wrapper.
     */
    public static class CachedContext {
        private final Context context;
        private final long timestamp;
        
        public CachedContext(Context context, long timestamp) {
            this.context = context;
            this.timestamp = timestamp;
        }
        
        public Context getContext() { return context; }
        public long getTimestamp() { return timestamp; }
    }
    
    /**
     * Combined context.
     */
    public static class Context {
        private final ProjectContext projectContext;
        private final UserContext userContext;
        private final Map<String, Object> additionalData;
        
        public Context(ProjectContext projectContext, UserContext userContext, Map<String, Object> additionalData) {
            this.projectContext = projectContext;
            this.userContext = userContext;
            this.additionalData = additionalData != null ? additionalData : new HashMap<>();
        }
        
        // Getters
        public ProjectContext getProjectContext() { return projectContext; }
        public UserContext getUserContext() { return userContext; }
        public Map<String, Object> getAdditionalData() { return additionalData; }
    }
    
    /**
     * Assistant listener interface.
     */
    public interface AssistantListener {
        void onContextUpdated(String projectPath, ProjectContext context);
        void onSuggestionGenerated(AssistantSuggestion suggestion);
        void onSuggestionApplied(String suggestionId, boolean success);
        void onCodeGenerated(String code, boolean success);
        void onAnalysisCompleted(String analysis, boolean success);
    }
    
    /**
     * Private constructor for singleton.
     */
    private ContextAwareAssistant() {
        this.projectContexts = new ConcurrentHashMap<>();
        this.userContexts = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.projectAnalyzer = ProjectAnalyzer.getInstance();
        this.codeEditor = IntelligentCodeEditor.getInstance();
        this.templateEngine = TemplateEngine.getInstance();
        this.workflowEngine = WorkflowEngine.getInstance();
        this.aiAssistant = new AIAssistant(false, "none", new HashMap<>());
        this.suggestionEngine = new SuggestionEngine();
        this.contextCache = new ContextCache(300000, 100); // 5 minutes, 100 entries
        
        LOG.info("ContextAwareAssistant initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The ContextAwareAssistant instance
     */
    public static synchronized ContextAwareAssistant getInstance() {
        if (instance == null) {
            instance = new ContextAwareAssistant();
        }
        return instance;
    }
    
    /**
     * Analyzes project and builds context.
     * @param projectPath The project path
     * @return CompletableFuture with project context
     */
    public CompletableFuture<ProjectContext> analyzeProject(String projectPath) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Check cache first
                String cacheKey = "project_" + projectPath.hashCode();
                CachedContext cached = contextCache.get(cacheKey);
                if (cached != null) {
                    return cached.getContext().getProjectContext();
                }
                
                // Analyze project
                var analysisFuture = projectAnalyzer.analyzeProjectAsync(projectPath);
                var analysis = analysisFuture.get();
                
                if (analysis == null) {
                    throw new RuntimeException("Failed to analyze project: " + projectPath);
                }
                
                // Extract project information
                String projectName = analysis.getMetadata().getName();
                String projectType = analysis.getMetadata().getType();
                
                // Build code structure
                CodeStructure codeStructure = buildCodeStructure(analysis);
                
                // Calculate metrics
                ProjectMetrics metrics = calculateProjectMetrics(analysis);
                
                // Create project context
                ProjectContext context = new ProjectContext(
                    projectPath, projectName, projectType,
                    extractSourceFiles(analysis), extractDependencies(analysis),
                    new HashMap<>(), codeStructure, metrics
                );
                
                // Cache context
                Context combinedContext = new Context(context, null, new HashMap<>());
                contextCache.put(cacheKey, combinedContext);
                
                // Store context
                projectContexts.put(projectPath, context);
                
                notifyContextUpdated(projectPath, context);
                
                LOG.info("Project context built: " + projectPath);
                return context;
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to analyze project: " + projectPath, e);
                return null;
            }
        });
    }
    
    /**
     * Builds code structure from analysis.
     * @param analysis The project analysis
     * @return Code structure
     */
    private CodeStructure buildCodeStructure(com.bajinho.continuebeans.filesystem.ProjectAnalyzer.ProjectAnalysis analysis) {
        Map<String, ClassInfo> classes = new HashMap<>();
        Map<String, MethodInfo> methods = new HashMap<>();
        Map<String, VariableInfo> variables = new HashMap<>();
        List<String> packages = new ArrayList<>();
        Map<String, List<String>> imports = new HashMap<>();
        
        // TODO: Extract code structure from analysis
        // This would require parsing the file analysis results
        
        return new CodeStructure(classes, methods, variables, packages, imports);
    }
    
    /**
     * Calculates project metrics.
     * @param analysis The project analysis
     * @return Project metrics
     */
    private ProjectMetrics calculateProjectMetrics(com.bajinho.continuebeans.filesystem.ProjectAnalyzer.ProjectAnalysis analysis) {
        int totalFiles = analysis.getFiles().size();
        int totalLines = analysis.getMetrics().getTotalLines();
        int totalClasses = 0; // TODO: Extract from analysis
        int totalMethods = 0; // TODO: Extract from analysis
        double complexity = analysis.getMetrics().getComplexity();
        double maintainability = analysis.getMetrics().getMaintainability();
        int testCoverage = 0; // TODO: Extract from security/performance analysis
        
        return new ProjectMetrics(totalFiles, totalLines, totalClasses, totalMethods,
                                complexity, maintainability, testCoverage);
    }
    
    /**
     * Extracts source files from analysis.
     * @param analysis The project analysis
     * @return List of source files
     */
    private List<String> extractSourceFiles(com.bajinho.continuebeans.filesystem.ProjectAnalyzer.ProjectAnalysis analysis) {
        List<String> sourceFiles = new ArrayList<>();
        for (var file : analysis.getFiles()) {
            if ("source".equals(file.getType())) {
                sourceFiles.add(file.getPath());
            }
        }
        return sourceFiles;
    }
    
    /**
     * Extracts dependencies from analysis.
     * @param analysis The project analysis
     * @return List of dependencies
     */
    private List<String> extractDependencies(com.bajinho.continuebeans.filesystem.ProjectAnalyzer.ProjectAnalysis analysis) {
        List<String> dependencies = new ArrayList<>();
        var dependencyGraph = analysis.getDependencies();
        for (var entry : dependencyGraph.getDependencies().entrySet()) {
            dependencies.add(entry.getKey());
        }
        return dependencies;
    }
    
    /**
     * Gets or creates user context.
     * @param userId The user ID
     * @return User context
     */
    public UserContext getUserContext(String userId) {
        return userContexts.computeIfAbsent(userId, UserContext::new);
    }
    
    /**
     * Generates suggestions based on context.
     * @param projectPath The project path
     * @param userId The user ID
     * @return CompletableFuture with suggestions
     */
    public CompletableFuture<List<AssistantSuggestion>> generateSuggestions(String projectPath, String userId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get contexts
                ProjectContext projectContext = projectContexts.get(projectPath);
                UserContext userContext = getUserContext(userId);
                
                if (projectContext == null) {
                    // Analyze project if not available
                    projectContext = analyzeProject(projectPath).get();
                }
                
                Context combinedContext = new Context(projectContext, userContext, null);
                
                // Generate regular suggestions
                List<AssistantSuggestion> suggestions = suggestionEngine.generateSuggestions(combinedContext);
                
                // Generate AI suggestions if enabled
                if (aiAssistant.isEnabled()) {
                    try {
                        List<AssistantSuggestion> aiSuggestions = aiAssistant.generateSuggestions(combinedContext, "").get();
                        suggestions.addAll(aiSuggestions);
                    } catch (Exception e) {
                        LOG.log(Level.WARNING, "Failed to generate AI suggestions", e);
                    }
                }
                
                // Sort by confidence
                suggestions.sort((a, b) -> Double.compare(b.getConfidence(), a.getConfidence()));
                
                // Notify listeners
                for (AssistantSuggestion suggestion : suggestions) {
                    notifySuggestionGenerated(suggestion);
                }
                
                return suggestions;
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to generate suggestions", e);
                return new ArrayList<>();
            }
        });
    }
    
    /**
     * Generates code based on context and prompt.
     * @param projectPath The project path
     * @param userId The user ID
     * @param prompt The code generation prompt
     * @return CompletableFuture with generated code
     */
    public CompletableFuture<String> generateCode(String projectPath, String userId, String prompt) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get contexts
                ProjectContext projectContext = projectContexts.get(projectPath);
                UserContext userContext = getUserContext(userId);
                
                if (projectContext == null) {
                    projectContext = analyzeProject(projectPath).get();
                }
                
                Context combinedContext = new Context(projectContext, userContext, null);
                
                // Generate code with AI
                String generatedCode = aiAssistant.generateCode(combinedContext, prompt).get();
                
                notifyCodeGenerated(generatedCode, true);
                
                return generatedCode;
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to generate code", e);
                notifyCodeGenerated("", false);
                return "// Code generation failed: " + e.getMessage();
            }
        });
    }
    
    /**
     * Analyzes code with AI.
     * @param projectPath The project path
     * @param userId The user ID
     * @param code The code to analyze
     * @return CompletableFuture with analysis
     */
    public CompletableFuture<String> analyzeCode(String projectPath, String userId, String code) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Get contexts
                ProjectContext projectContext = projectContexts.get(projectPath);
                UserContext userContext = getUserContext(userId);
                
                if (projectContext == null) {
                    projectContext = analyzeProject(projectPath).get();
                }
                
                Context combinedContext = new Context(projectContext, userContext, null);
                
                // Analyze code with AI
                String analysis = aiAssistant.analyzeCode(combinedContext, code).get();
                
                notifyAnalysisCompleted(analysis, true);
                
                return analysis;
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to analyze code", e);
                notifyAnalysisCompleted("", false);
                return "Code analysis failed: " + e.getMessage();
            }
        });
    }
    
    /**
     * Applies a suggestion.
     * @param suggestionId The suggestion ID
     * @return CompletableFuture with success status
     */
    public CompletableFuture<Boolean> applySuggestion(String suggestionId) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // TODO: Implement suggestion application logic
                // This would depend on the suggestion type and action
                
                notifySuggestionApplied(suggestionId, true);
                return true;
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to apply suggestion: " + suggestionId, e);
                notifySuggestionApplied(suggestionId, false);
                return false;
            }
        });
    }
    
    /**
     * Updates user activity.
     * @param userId The user ID
     * @param filePath The file being edited
     * @param command The command executed
     */
    public void updateUserActivity(String userId, String filePath, String command) {
        UserContext userContext = getUserContext(userId);
        
        if (filePath != null) {
            userContext.addRecentFile(filePath);
        }
        
        if (command != null) {
            userContext.addRecentCommand(command);
        }
    }
    
    /**
     * Gets project context.
     * @param projectPath The project path
     * @return Project context or null if not found
     */
    public ProjectContext getProjectContext(String projectPath) {
        return projectContexts.get(projectPath);
    }
    
    /**
     * Gets all project contexts.
     * @return Copy of all project contexts
     */
    public Map<String, ProjectContext> getProjectContexts() {
        return new HashMap<>(projectContexts);
    }
    
    /**
     * Gets all user contexts.
     * @return Copy of all user contexts
     */
    public Map<String, UserContext> getUserContexts() {
        return new HashMap<>(userContexts);
    }
    
    /**
     * Adds an assistant listener.
     * @param listener The listener to add
     */
    public void addAssistantListener(AssistantListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes an assistant listener.
     * @param listener The listener to remove
     */
    public void removeAssistantListener(AssistantListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Configures AI assistant.
     * @param enabled Whether AI is enabled
     * @param provider The AI provider
     * @param configuration The configuration
     */
    public void configureAI(boolean enabled, String provider, Map<String, Object> configuration) {
        this.aiAssistant = new AIAssistant(enabled, provider, configuration);
        LOG.info("AI assistant configured: enabled=" + enabled + ", provider=" + provider);
    }
    
    /**
     * Adds a suggestion provider.
     * @param provider The provider to add
     */
    public void addSuggestionProvider(SuggestionProvider provider) {
        suggestionEngine.addProvider(provider);
        LOG.info("Suggestion provider added: " + provider.getProviderName());
    }
    
    /**
     * Clears context cache.
     */
    public void clearCache() {
        contextCache.clear();
        LOG.info("Context cache cleared");
    }
    
    // Notification methods
    
    private void notifyContextUpdated(String projectPath, ProjectContext context) {
        for (AssistantListener listener : listeners) {
            try {
                listener.onContextUpdated(projectPath, context);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifySuggestionGenerated(AssistantSuggestion suggestion) {
        for (AssistantListener listener : listeners) {
            try {
                listener.onSuggestionGenerated(suggestion);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifySuggestionApplied(String suggestionId, boolean success) {
        for (AssistantListener listener : listeners) {
            try {
                listener.onSuggestionApplied(suggestionId, success);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyCodeGenerated(String code, boolean success) {
        for (AssistantListener listener : listeners) {
            try {
                listener.onCodeGenerated(code, success);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyAnalysisCompleted(String analysis, boolean success) {
        for (AssistantListener listener : listeners) {
            try {
                listener.onAnalysisCompleted(analysis, success);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    /**
     * Gets assistant statistics.
     * @return Statistics map
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("projectContexts", projectContexts.size());
        stats.put("userContexts", userContexts.size());
        stats.put("aiEnabled", aiAssistant.isEnabled());
        stats.put("aiProvider", aiAssistant.getProvider());
        stats.put("cacheSize", contextCache.cache.size());
        return stats;
    }
}
