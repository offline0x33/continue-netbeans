package com.bajinho.continuebeans.filesystem;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/**
 * Project analyzer for deep project analysis, metadata extraction,
 * dependency analysis, and comprehensive reporting.
 * 
 * @author Continue Beans Team
 */
public class ProjectAnalyzer {
    
    private static final Logger LOG = Logger.getLogger(ProjectAnalyzer.class.getName());
    
    private static ProjectAnalyzer instance;
    
    private final Map<String, ProjectAnalysis> analysisCache;
    private final List<ProjectAnalysisListener> listeners;
    private final Map<String, AnalysisTask> activeTasks;
    
    /**
     * Represents a complete project analysis.
     */
    public static class ProjectAnalysis {
        private final String projectPath;
        private final ProjectMetadata metadata;
        private final List<ProjectFile> files;
        private final DependencyGraph dependencies;
        private final CodeMetrics metrics;
        private final SecurityAnalysis security;
        private final PerformanceAnalysis performance;
        private final long analysisTime;
        private final String analysisId;
        
        public ProjectAnalysis(String projectPath, ProjectMetadata metadata, 
                            List<ProjectFile> files, DependencyGraph dependencies,
                            CodeMetrics metrics, SecurityAnalysis security,
                            PerformanceAnalysis performance, String analysisId) {
            this.projectPath = projectPath;
            this.metadata = metadata;
            this.files = files;
            this.dependencies = dependencies;
            this.metrics = metrics;
            this.security = security;
            this.performance = performance;
            this.analysisId = analysisId;
            this.analysisTime = System.currentTimeMillis();
        }
        
        // Getters
        public String getProjectPath() { return projectPath; }
        public ProjectMetadata getMetadata() { return metadata; }
        public List<ProjectFile> getFiles() { return files; }
        public DependencyGraph getDependencies() { return dependencies; }
        public CodeMetrics getMetrics() { return metrics; }
        public SecurityAnalysis getSecurity() { return security; }
        public PerformanceAnalysis getPerformance() { return performance; }
        public long getAnalysisTime() { return analysisTime; }
        public String getAnalysisId() { return analysisId; }
    }
    
    /**
     * Project metadata information.
     */
    public static class ProjectMetadata {
        private final String name;
        private final String displayName;
        private final String description;
        private final String type;
        private final List<String> sourceRoots;
        private final List<String> testRoots;
        private final Map<String, String> properties;
        private final String buildSystem;
        private final List<String> frameworks;
        
        public ProjectMetadata(String name, String displayName, String description, 
                            String type, List<String> sourceRoots, List<String> testRoots,
                            Map<String, String> properties, String buildSystem, 
                            List<String> frameworks) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
            this.type = type;
            this.sourceRoots = sourceRoots;
            this.testRoots = testRoots;
            this.properties = properties;
            this.buildSystem = buildSystem;
            this.frameworks = frameworks;
        }
        
        // Getters
        public String getName() { return name; }
        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public String getType() { return type; }
        public List<String> getSourceRoots() { return sourceRoots; }
        public List<String> getTestRoots() { return testRoots; }
        public Map<String, String> getProperties() { return properties; }
        public String getBuildSystem() { return buildSystem; }
        public List<String> getFrameworks() { return frameworks; }
    }
    
    /**
     * Represents a project file with metadata.
     */
    public static class ProjectFile {
        private final String path;
        private final String name;
        private final String extension;
        private final long size;
        private final long lastModified;
        private final String type;
        private final Map<String, Object> metadata;
        private final List<String> dependencies;
        private final List<String> imports;
        
        public ProjectFile(String path, String name, String extension, long size, 
                         long lastModified, String type, Map<String, Object> metadata,
                         List<String> dependencies, List<String> imports) {
            this.path = path;
            this.name = name;
            this.extension = extension;
            this.size = size;
            this.lastModified = lastModified;
            this.type = type;
            this.metadata = metadata;
            this.dependencies = dependencies;
            this.imports = imports;
        }
        
        // Getters
        public String getPath() { return path; }
        public String getName() { return name; }
        public String getExtension() { return extension; }
        public long getSize() { return size; }
        public long getLastModified() { return lastModified; }
        public String getType() { return type; }
        public Map<String, Object> getMetadata() { return metadata; }
        public List<String> getDependencies() { return dependencies; }
        public List<String> getImports() { return imports; }
    }
    
    /**
     * Dependency graph representation.
     */
    public static class DependencyGraph {
        private final Map<String, List<String>> dependencies;
        private final Map<String, DependencyInfo> dependencyInfo;
        private final List<CircularDependency> circularDependencies;
        
        public DependencyGraph(Map<String, List<String>> dependencies, 
                             Map<String, DependencyInfo> dependencyInfo,
                             List<CircularDependency> circularDependencies) {
            this.dependencies = dependencies;
            this.dependencyInfo = dependencyInfo;
            this.circularDependencies = circularDependencies;
        }
        
        // Getters
        public Map<String, List<String>> getDependencies() { return dependencies; }
        public Map<String, DependencyInfo> getDependencyInfo() { return dependencyInfo; }
        public List<CircularDependency> getCircularDependencies() { return circularDependencies; }
    }
    
    /**
     * Dependency information.
     */
    public static class DependencyInfo {
        private final String name;
        private final String version;
        private final String type; // internal, external, system
        private final boolean optional;
        private final List<String> usages;
        
        public DependencyInfo(String name, String version, String type, 
                            boolean optional, List<String> usages) {
            this.name = name;
            this.version = version;
            this.type = type;
            this.optional = optional;
            this.usages = usages;
        }
        
        // Getters
        public String getName() { return name; }
        public String getVersion() { return version; }
        public String getType() { return type; }
        public boolean isOptional() { return optional; }
        public List<String> getUsages() { return usages; }
    }
    
    /**
     * Circular dependency representation.
     */
    public static class CircularDependency {
        private final List<String> cycle;
        private final int length;
        private final String description;
        
        public CircularDependency(List<String> cycle, int length, String description) {
            this.cycle = cycle;
            this.length = length;
            this.description = description;
        }
        
        // Getters
        public List<String> getCycle() { return cycle; }
        public int getLength() { return length; }
        public String getDescription() { return description; }
    }
    
    /**
     * Code metrics analysis.
     */
    public static class CodeMetrics {
        private final int totalLines;
        private final int codeLines;
        private final int commentLines;
        private final int blankLines;
        private final int complexity;
        private final double maintainability;
        private final List<String> complexMethods;
        private final Map<String, Integer> fileMetrics;
        
        public CodeMetrics(int totalLines, int codeLines, int commentLines, int blankLines,
                         int complexity, double maintainability, List<String> complexMethods,
                         Map<String, Integer> fileMetrics) {
            this.totalLines = totalLines;
            this.codeLines = codeLines;
            this.commentLines = commentLines;
            this.blankLines = blankLines;
            this.complexity = complexity;
            this.maintainability = maintainability;
            this.complexMethods = complexMethods;
            this.fileMetrics = fileMetrics;
        }
        
        // Getters
        public int getTotalLines() { return totalLines; }
        public int getCodeLines() { return codeLines; }
        public int getCommentLines() { return commentLines; }
        public int getBlankLines() { return blankLines; }
        public int getComplexity() { return complexity; }
        public double getMaintainability() { return maintainability; }
        public List<String> getComplexMethods() { return complexMethods; }
        public Map<String, Integer> getFileMetrics() { return fileMetrics; }
    }
    
    /**
     * Security analysis results.
     */
    public static class SecurityAnalysis {
        private final List<SecurityVulnerability> vulnerabilities;
        private final List<SecurityIssue> issues;
        private final int securityScore;
        private final List<String> recommendations;
        
        public SecurityAnalysis(List<SecurityVulnerability> vulnerabilities, 
                             List<SecurityIssue> issues, int securityScore,
                             List<String> recommendations) {
            this.vulnerabilities = vulnerabilities;
            this.issues = issues;
            this.securityScore = securityScore;
            this.recommendations = recommendations;
        }
        
        // Getters
        public List<SecurityVulnerability> getVulnerabilities() { return vulnerabilities; }
        public List<SecurityIssue> getIssues() { return issues; }
        public int getSecurityScore() { return securityScore; }
        public List<String> getRecommendations() { return recommendations; }
    }
    
    /**
     * Security vulnerability.
     */
    public static class SecurityVulnerability {
        private final String type;
        private final String severity;
        private final String file;
        private final int line;
        private final String description;
        private final String recommendation;
        
        public SecurityVulnerability(String type, String severity, String file, 
                                    int line, String description, String recommendation) {
            this.type = type;
            this.severity = severity;
            this.file = file;
            this.line = line;
            this.description = description;
            this.recommendation = recommendation;
        }
        
        // Getters
        public String getType() { return type; }
        public String getSeverity() { return severity; }
        public String getFile() { return file; }
        public int getLine() { return line; }
        public String getDescription() { return description; }
        public String getRecommendation() { return recommendation; }
    }
    
    /**
     * Security issue.
     */
    public static class SecurityIssue {
        private final String category;
        private final String description;
        private final String file;
        private final String severity;
        
        public SecurityIssue(String category, String description, String file, String severity) {
            this.category = category;
            this.description = description;
            this.file = file;
            this.severity = severity;
        }
        
        // Getters
        public String getCategory() { return category; }
        public String getDescription() { return description; }
        public String getFile() { return file; }
        public String getSeverity() { return severity; }
    }
    
    /**
     * Performance analysis results.
     */
    public static class PerformanceAnalysis {
        private final List<PerformanceBottleneck> bottlenecks;
        private final List<PerformanceIssue> issues;
        private final int performanceScore;
        private final List<String> optimizations;
        
        public PerformanceAnalysis(List<PerformanceBottleneck> bottlenecks,
                                List<PerformanceIssue> issues, int performanceScore,
                                List<String> optimizations) {
            this.bottlenecks = bottlenecks;
            this.issues = issues;
            this.performanceScore = performanceScore;
            this.optimizations = optimizations;
        }
        
        // Getters
        public List<PerformanceBottleneck> getBottlenecks() { return bottlenecks; }
        public List<PerformanceIssue> getIssues() { return issues; }
        public int getPerformanceScore() { return performanceScore; }
        public List<String> getOptimizations() { return optimizations; }
    }
    
    /**
     * Performance bottleneck.
     */
    public static class PerformanceBottleneck {
        private final String type;
        private final String file;
        private final String method;
        private final String description;
        private final String impact;
        
        public PerformanceBottleneck(String type, String file, String method,
                                    String description, String impact) {
            this.type = type;
            this.file = file;
            this.method = method;
            this.description = description;
            this.impact = impact;
        }
        
        // Getters
        public String getType() { return type; }
        public String getFile() { return file; }
        public String getMethod() { return method; }
        public String getDescription() { return description; }
        public String getImpact() { return impact; }
    }
    
    /**
     * Performance issue.
     */
    public static class PerformanceIssue {
        private final String category;
        private final String description;
        private final String file;
        private final String severity;
        
        public PerformanceIssue(String category, String description, String file, String severity) {
            this.category = category;
            this.description = description;
            this.file = file;
            this.severity = severity;
        }
        
        // Getters
        public String getCategory() { return category; }
        public String getDescription() { return description; }
        public String getFile() { return file; }
        public String getSeverity() { return severity; }
    }
    
    /**
     * Analysis task representation.
     */
    public static class AnalysisTask {
        private final String taskId;
        private final String projectPath;
        private final long startTime;
        private long endTime;
        private boolean completed;
        private boolean success;
        private String errorMessage;
        private ProjectAnalysis result;
        
        public AnalysisTask(String taskId, String projectPath) {
            this.taskId = taskId;
            this.projectPath = projectPath;
            this.startTime = System.currentTimeMillis();
            this.completed = false;
            this.success = false;
        }
        
        // Getters and setters
        public String getTaskId() { return taskId; }
        public String getProjectPath() { return projectPath; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public boolean isCompleted() { return completed; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public ProjectAnalysis getResult() { return result; }
        
        public void setCompleted(boolean completed) {
            this.completed = completed;
            if (completed) {
                this.endTime = System.currentTimeMillis();
            }
        }
        
        public void setSuccess(boolean success) {
            this.success = success;
            this.completed = true;
            this.endTime = System.currentTimeMillis();
        }
        
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            this.success = false;
            this.completed = true;
            this.endTime = System.currentTimeMillis();
        }
        
        public void setResult(ProjectAnalysis result) {
            this.result = result;
        }
        
        public long getDuration() {
            return endTime - startTime;
        }
    }
    
    /**
     * Project analysis listener interface.
     */
    public interface ProjectAnalysisListener {
        void onAnalysisStarted(String taskId, String projectPath);
        void onAnalysisCompleted(String taskId, ProjectAnalysis analysis);
        void onAnalysisFailed(String taskId, String error);
        void onAnalysisProgress(String taskId, String progress);
    }
    
    /**
     * Private constructor for singleton.
     */
    private ProjectAnalyzer() {
        this.analysisCache = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.activeTasks = new ConcurrentHashMap<>();
        
        LOG.info("ProjectAnalyzer initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The ProjectAnalyzer instance
     */
    public static synchronized ProjectAnalyzer getInstance() {
        if (instance == null) {
            instance = new ProjectAnalyzer();
        }
        return instance;
    }
    
    /**
     * Analyzes a project asynchronously.
     * @param projectPath The project path
     * @return CompletableFuture with the analysis result
     */
    public CompletableFuture<ProjectAnalysis> analyzeProjectAsync(String projectPath) {
        return CompletableFuture.supplyAsync(() -> {
            String taskId = "analysis_" + System.currentTimeMillis();
            AnalysisTask task = new AnalysisTask(taskId, projectPath);
            activeTasks.put(taskId, task);
            
            try {
                notifyAnalysisStarted(taskId, projectPath);
                
                // Get NetBeans project
                Project project = getNetBeansProject(projectPath);
                if (project == null) {
                    task.setErrorMessage("NetBeans project not found: " + projectPath);
                    notifyAnalysisFailed(taskId, task.getErrorMessage());
                    return null;
                }
                
                // Extract metadata
                ProjectMetadata metadata = extractMetadata(project);
                notifyAnalysisProgress(taskId, "Metadata extracted");
                
                // Analyze files
                List<ProjectFile> files = analyzeFiles(project);
                notifyAnalysisProgress(taskId, "Files analyzed");
                
                // Analyze dependencies
                DependencyGraph dependencies = analyzeDependencies(files);
                notifyAnalysisProgress(taskId, "Dependencies analyzed");
                
                // Calculate metrics
                CodeMetrics metrics = calculateMetrics(files);
                notifyAnalysisProgress(taskId, "Metrics calculated");
                
                // Security analysis
                SecurityAnalysis security = analyzeSecurity(files);
                notifyAnalysisProgress(taskId, "Security analysis completed");
                
                // Performance analysis
                PerformanceAnalysis performance = analyzePerformance(files);
                notifyAnalysisProgress(taskId, "Performance analysis completed");
                
                // Create analysis result
                ProjectAnalysis analysis = new ProjectAnalysis(
                    projectPath, metadata, files, dependencies, 
                    metrics, security, performance, taskId
                );
                
                // Cache result
                analysisCache.put(projectPath, analysis);
                
                task.setResult(analysis);
                task.setSuccess(true);
                
                notifyAnalysisCompleted(taskId, analysis);
                
                LOG.info("Project analysis completed: " + projectPath);
                return analysis;
                
            } catch (Exception e) {
                task.setErrorMessage("Analysis failed: " + e.getMessage());
                notifyAnalysisFailed(taskId, task.getErrorMessage());
                LOG.log(Level.SEVERE, "Project analysis failed: " + projectPath, e);
                return null;
            } finally {
                activeTasks.remove(taskId);
            }
        });
    }
    
    /**
     * Gets a NetBeans project from path.
     * @param projectPath The project path
     * @return The Project or null if not found
     */
    private Project getNetBeansProject(String projectPath) {
        try {
            FileObject projectDir = FileUtil.toFileObject(new File(projectPath));
            if (projectDir != null) {
                return ProjectManager.getDefault().findProject(projectDir);
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to get NetBeans project: " + projectPath, e);
        }
        return null;
    }
    
    /**
     * Extracts project metadata.
     * @param project The NetBeans project
     * @return ProjectMetadata
     */
    private ProjectMetadata extractMetadata(Project project) {
        try {
            ProjectInformation info = project.getLookup().lookup(ProjectInformation.class);
            String name = info != null ? info.getName() : "Unknown";
            String displayName = info != null ? info.getDisplayName() : name;
            String description = ""; // getDescription() might not be available in all NetBeans versions
            
            // Get source roots
            List<String> sourceRoots = new ArrayList<>();
            List<String> testRoots = new ArrayList<>();
            
            Sources sources = project.getLookup().lookup(Sources.class);
            if (sources != null) {
                for (SourceGroup group : sources.getSourceGroups("java")) {
                    sourceRoots.add(group.getRootFolder().getPath());
                }
                for (SourceGroup group : sources.getSourceGroups("test")) {
                    testRoots.add(group.getRootFolder().getPath());
                }
            }
            
            // Detect build system and frameworks
            String buildSystem = detectBuildSystem(project);
            List<String> frameworks = detectFrameworks(project);
            
            Map<String, String> properties = new HashMap<>();
            properties.put("projectType", project.getClass().getSimpleName());
            
            return new ProjectMetadata(name, displayName, description, "Java Project",
                sourceRoots, testRoots, properties, buildSystem, frameworks);
            
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to extract project metadata", e);
            return new ProjectMetadata("Unknown", "Unknown", "", "Unknown",
                new ArrayList<>(), new ArrayList<>(), new HashMap<>(), "Unknown", new ArrayList<>());
        }
    }
    
    /**
     * Analyzes project files.
     * @param project The NetBeans project
     * @return List of ProjectFile
     */
    private List<ProjectFile> analyzeFiles(Project project) {
        List<ProjectFile> files = new ArrayList<>();
        
        try {
            Sources sources = project.getLookup().lookup(Sources.class);
            if (sources != null) {
                for (SourceGroup group : sources.getSourceGroups("java")) {
                    FileObject root = group.getRootFolder();
                    analyzeFilesRecursive(root, files, root.getPath());
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to analyze project files", e);
        }
        
        return files;
    }
    
    /**
     * Recursively analyzes files.
     * @param folder The folder to analyze
     * @param files List to add files to
     * @param basePath Base path for relative paths
     */
    private void analyzeFilesRecursive(FileObject folder, List<ProjectFile> files, String basePath) {
        try {
            for (FileObject child : folder.getChildren()) {
                if (child.isFolder()) {
                    analyzeFilesRecursive(child, files, basePath);
                } else {
                    String extension = child.getExt();
                    if (isSourceFile(extension)) {
                        ProjectFile file = createProjectFile(child, basePath);
                        files.add(file);
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to analyze files recursively", e);
        }
    }
    
    /**
     * Checks if a file is a source file.
     * @param extension The file extension
     * @return True if source file
     */
    private boolean isSourceFile(String extension) {
        return "java".equalsIgnoreCase(extension) || 
               "kt".equalsIgnoreCase(extension) ||
               "scala".equalsIgnoreCase(extension) ||
               "groovy".equalsIgnoreCase(extension) ||
               "xml".equalsIgnoreCase(extension) ||
               "properties".equalsIgnoreCase(extension) ||
               "yml".equalsIgnoreCase(extension) ||
               "yaml".equalsIgnoreCase(extension);
    }
    
    /**
     * Creates a ProjectFile from FileObject.
     * @param fileObject The FileObject
     * @param basePath Base path
     * @return ProjectFile
     */
    private ProjectFile createProjectFile(FileObject fileObject, String basePath) {
        try {
            String path = fileObject.getPath();
            String name = fileObject.getName();
            String extension = fileObject.getExt();
            long size = fileObject.getSize();
            long lastModified = fileObject.lastModified().getTime();
            String type = determineFileType(extension);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("path", path);
            metadata.put("size", size);
            metadata.put("lastModified", lastModified);
            
            // Extract dependencies and imports for Java files
            List<String> dependencies = new ArrayList<>();
            List<String> imports = new ArrayList<>();
            
            if ("java".equalsIgnoreCase(extension)) {
                // TODO: Parse Java file for imports and dependencies
                // This would require a Java parser implementation
            }
            
            return new ProjectFile(path, name, extension, size, lastModified, type,
                metadata, dependencies, imports);
                
        } catch (Exception e) {
            LOG.log(Level.WARNING, "Failed to create project file: " + fileObject.getPath(), e);
            return null;
        }
    }
    
    /**
     * Determines file type from extension.
     * @param extension The file extension
     * @return File type
     */
    private String determineFileType(String extension) {
        switch (extension.toLowerCase()) {
            case "java": return "source";
            case "kt": return "source";
            case "scala": return "source";
            case "groovy": return "source";
            case "xml": return "config";
            case "properties": return "config";
            case "yml":
            case "yaml": return "config";
            case "gradle": return "build";
            case "pom": return "build";
            default: return "other";
        }
    }
    
    /**
     * Analyzes dependencies.
     * @param files List of project files
     * @return DependencyGraph
     */
    private DependencyGraph analyzeDependencies(List<ProjectFile> files) {
        Map<String, List<String>> dependencies = new HashMap<>();
        Map<String, DependencyInfo> dependencyInfo = new HashMap<>();
        List<CircularDependency> circularDependencies = new ArrayList<>();
        
        // TODO: Implement dependency analysis
        // This would require parsing import statements and build files
        
        return new DependencyGraph(dependencies, dependencyInfo, circularDependencies);
    }
    
    /**
     * Calculates code metrics.
     * @param files List of project files
     * @return CodeMetrics
     */
    private CodeMetrics calculateMetrics(List<ProjectFile> files) {
        int totalLines = 0;
        int codeLines = 0;
        int commentLines = 0;
        int blankLines = 0;
        int complexity = 0;
        List<String> complexMethods = new ArrayList<>();
        Map<String, Integer> fileMetrics = new HashMap<>();
        
        // TODO: Implement metrics calculation
        // This would require parsing source files
        
        double maintainability = calculateMaintainability(complexity, totalLines, codeLines);
        
        return new CodeMetrics(totalLines, codeLines, commentLines, blankLines,
            complexity, maintainability, complexMethods, fileMetrics);
    }
    
    /**
     * Calculates maintainability index.
     * @param complexity Cyclomatic complexity
     * @param totalLines Total lines of code
     * @param codeLines Lines of actual code
     * @return Maintainability index (0-100)
     */
    private double calculateMaintainability(int complexity, int totalLines, int codeLines) {
        if (codeLines == 0) return 100.0;
        
        // Simplified maintainability calculation
        double complexityScore = Math.max(0, 100 - (complexity * 2));
        double sizeScore = Math.max(0, 100 - (totalLines / 1000.0));
        
        return (complexityScore + sizeScore) / 2.0;
    }
    
    /**
     * Analyzes security.
     * @param files List of project files
     * @return SecurityAnalysis
     */
    private SecurityAnalysis analyzeSecurity(List<ProjectFile> files) {
        List<SecurityVulnerability> vulnerabilities = new ArrayList<>();
        List<SecurityIssue> issues = new ArrayList<>();
        List<String> recommendations = new ArrayList<>();
        
        // TODO: Implement security analysis
        // This would require security pattern detection
        
        int securityScore = calculateSecurityScore(vulnerabilities, issues);
        
        return new SecurityAnalysis(vulnerabilities, issues, securityScore, recommendations);
    }
    
    /**
     * Calculates security score.
     * @param vulnerabilities List of vulnerabilities
     * @param issues List of security issues
     * @return Security score (0-100)
     */
    private int calculateSecurityScore(List<SecurityVulnerability> vulnerabilities, 
                                     List<SecurityIssue> issues) {
        int score = 100;
        
        // Deduct points for vulnerabilities
        for (SecurityVulnerability vuln : vulnerabilities) {
            switch (vuln.getSeverity().toLowerCase()) {
                case "critical": score -= 20; break;
                case "high": score -= 15; break;
                case "medium": score -= 10; break;
                case "low": score -= 5; break;
            }
        }
        
        // Deduct points for issues
        for (SecurityIssue issue : issues) {
            switch (issue.getSeverity().toLowerCase()) {
                case "high": score -= 8; break;
                case "medium": score -= 5; break;
                case "low": score -= 2; break;
            }
        }
        
        return Math.max(0, score);
    }
    
    /**
     * Analyzes performance.
     * @param files List of project files
     * @return PerformanceAnalysis
     */
    private PerformanceAnalysis analyzePerformance(List<ProjectFile> files) {
        List<PerformanceBottleneck> bottlenecks = new ArrayList<>();
        List<PerformanceIssue> issues = new ArrayList<>();
        List<String> optimizations = new ArrayList<>();
        
        // TODO: Implement performance analysis
        // This would require performance pattern detection
        
        int performanceScore = calculatePerformanceScore(bottlenecks, issues);
        
        return new PerformanceAnalysis(bottlenecks, issues, performanceScore, optimizations);
    }
    
    /**
     * Calculates performance score.
     * @param bottlenecks List of bottlenecks
     * @param issues List of performance issues
     * @return Performance score (0-100)
     */
    private int calculatePerformanceScore(List<PerformanceBottleneck> bottlenecks,
                                        List<PerformanceIssue> issues) {
        int score = 100;
        
        // Deduct points for bottlenecks
        for (PerformanceBottleneck bottleneck : bottlenecks) {
            score -= 10;
        }
        
        // Deduct points for issues
        for (PerformanceIssue issue : issues) {
            switch (issue.getSeverity().toLowerCase()) {
                case "high": score -= 8; break;
                case "medium": score -= 5; break;
                case "low": score -= 2; break;
            }
        }
        
        return Math.max(0, score);
    }
    
    /**
     * Detects build system.
     * @param project The NetBeans project
     * @return Build system name
     */
    private String detectBuildSystem(Project project) {
        // TODO: Implement build system detection
        // Check for pom.xml (Maven), build.gradle (Gradle), etc.
        return "Unknown";
    }
    
    /**
     * Detects frameworks.
     * @param project The NetBeans project
     * @return List of framework names
     */
    private List<String> detectFrameworks(Project project) {
        List<String> frameworks = new ArrayList<>();
        
        // TODO: Implement framework detection
        // Check for Spring, Hibernate, etc.
        
        return frameworks;
    }
    
    /**
     * Gets cached analysis.
     * @param projectPath The project path
     * @return Cached analysis or null
     */
    public ProjectAnalysis getCachedAnalysis(String projectPath) {
        return analysisCache.get(projectPath);
    }
    
    /**
     * Clears analysis cache.
     */
    public void clearCache() {
        analysisCache.clear();
    }
    
    /**
     * Adds a project analysis listener.
     * @param listener The listener to add
     */
    public void addAnalysisListener(ProjectAnalysisListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes a project analysis listener.
     * @param listener The listener to remove
     */
    public void removeAnalysisListener(ProjectAnalysisListener listener) {
        listeners.remove(listener);
    }
    
    /**
     * Notifies listeners about analysis start.
     * @param taskId The task ID
     * @param projectPath The project path
     */
    private void notifyAnalysisStarted(String taskId, String projectPath) {
        for (ProjectAnalysisListener listener : listeners) {
            try {
                listener.onAnalysisStarted(taskId, projectPath);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    /**
     * Notifies listeners about analysis completion.
     * @param taskId The task ID
     * @param analysis The analysis result
     */
    private void notifyAnalysisCompleted(String taskId, ProjectAnalysis analysis) {
        for (ProjectAnalysisListener listener : listeners) {
            try {
                listener.onAnalysisCompleted(taskId, analysis);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    /**
     * Notifies listeners about analysis failure.
     * @param taskId The task ID
     * @param error The error message
     */
    private void notifyAnalysisFailed(String taskId, String error) {
        for (ProjectAnalysisListener listener : listeners) {
            try {
                listener.onAnalysisFailed(taskId, error);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    /**
     * Notifies listeners about analysis progress.
     * @param taskId The task ID
     * @param progress The progress message
     */
    private void notifyAnalysisProgress(String taskId, String progress) {
        for (ProjectAnalysisListener listener : listeners) {
            try {
                listener.onAnalysisProgress(taskId, progress);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    /**
     * Gets all active tasks.
     * @return Copy of active tasks
     */
    public Map<String, AnalysisTask> getActiveTasks() {
        return new HashMap<>(activeTasks);
    }
    
    /**
     * Gets all cached analyses.
     * @return Copy of cached analyses
     */
    public Map<String, ProjectAnalysis> getCachedAnalyses() {
        return new HashMap<>(analysisCache);
    }
}
