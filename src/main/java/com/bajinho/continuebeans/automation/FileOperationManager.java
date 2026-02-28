package com.bajinho.continuebeans.automation;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.bajinho.continuebeans.filesystem.NetBeansFileSystem;
import com.bajinho.continuebeans.filesystem.FileWatcher;
import com.bajinho.continuebeans.filesystem.ProjectAnalyzer;

/**
 * Advanced file operations automation manager with templates,
 * workflows, batch processing, and intelligent file operations.
 * 
 * @author Continue Beans Team
 */
public class FileOperationManager {
    
    private static final Logger LOG = Logger.getLogger(FileOperationManager.class.getName());
    
    private static FileOperationManager instance;
    
    private final Map<String, FileTemplate> templates;
    private final Map<String, Workflow> workflows;
    private final Map<String, BatchOperation> batchOperations;
    private final List<OperationListener> listeners;
    private final ScheduledExecutorService scheduler;
    private final NetBeansFileSystem fileSystem;
    private final ProjectAnalyzer projectAnalyzer;
    private final Map<String, OperationQueue> operationQueues;
    
    /**
     * Represents a file template for automated file creation.
     */
    public static class FileTemplate {
        private final String templateId;
        private final String name;
        private final String description;
        private final String content;
        private final Map<String, String> variables;
        private final List<String> fileExtensions;
        private final boolean projectSpecific;
        private final String category;
        
        public FileTemplate(String templateId, String name, String description, 
                          String content, Map<String, String> variables,
                          List<String> fileExtensions, boolean projectSpecific, 
                          String category) {
            this.templateId = templateId;
            this.name = name;
            this.description = description;
            this.content = content;
            this.variables = variables != null ? variables : new HashMap<>();
            this.fileExtensions = fileExtensions != null ? fileExtensions : new ArrayList<>();
            this.projectSpecific = projectSpecific;
            this.category = category;
        }
        
        /**
         * Processes template with variable substitution.
         * @param context Variable context
         * @return Processed content
         */
        public String processTemplate(Map<String, String> context) {
            String result = content;
            for (Map.Entry<String, String> entry : variables.entrySet()) {
                String key = entry.getKey();
                String defaultValue = entry.getValue();
                String value = context.getOrDefault(key, defaultValue);
                result = result.replace("${" + key + "}", value);
            }
            return result;
        }
        
        // Getters
        public String getTemplateId() { return templateId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public String getContent() { return content; }
        public Map<String, String> getVariables() { return variables; }
        public List<String> getFileExtensions() { return fileExtensions; }
        public boolean isProjectSpecific() { return projectSpecific; }
        public String getCategory() { return category; }
    }
    
    /**
     * Represents an automated workflow.
     */
    public static class Workflow {
        private final String workflowId;
        private final String name;
        private final String description;
        private final List<WorkflowStep> steps;
        private final Map<String, Object> context;
        private final boolean parallel;
        private final int timeoutSeconds;
        
        public Workflow(String workflowId, String name, String description, 
                       List<WorkflowStep> steps, Map<String, Object> context,
                       boolean parallel, int timeoutSeconds) {
            this.workflowId = workflowId;
            this.name = name;
            this.description = description;
            this.steps = steps != null ? steps : new ArrayList<>();
            this.context = context != null ? context : new HashMap<>();
            this.parallel = parallel;
            this.timeoutSeconds = timeoutSeconds;
        }
        
        // Getters
        public String getWorkflowId() { return workflowId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<WorkflowStep> getSteps() { return steps; }
        public Map<String, Object> getContext() { return context; }
        public boolean isParallel() { return parallel; }
        public int getTimeoutSeconds() { return timeoutSeconds; }
    }
    
    /**
     * Represents a workflow step.
     */
    public static class WorkflowStep {
        private final String stepId;
        private final String name;
        private final String operationType;
        private final Map<String, Object> parameters;
        private final List<String> dependencies;
        private final boolean optional;
        private final int retryCount;
        
        public WorkflowStep(String stepId, String name, String operationType,
                          Map<String, Object> parameters, List<String> dependencies,
                          boolean optional, int retryCount) {
            this.stepId = stepId;
            this.name = name;
            this.operationType = operationType;
            this.parameters = parameters != null ? parameters : new HashMap<>();
            this.dependencies = dependencies != null ? dependencies : new ArrayList<>();
            this.optional = optional;
            this.retryCount = retryCount;
        }
        
        // Getters
        public String getStepId() { return stepId; }
        public String getName() { return name; }
        public String getOperationType() { return operationType; }
        public Map<String, Object> getParameters() { return parameters; }
        public List<String> getDependencies() { return dependencies; }
        public boolean isOptional() { return optional; }
        public int getRetryCount() { return retryCount; }
    }
    
    /**
     * Represents a batch operation.
     */
    public static class BatchOperation {
        private final String batchId;
        private final String name;
        private final String description;
        private final List<FileOperation> operations;
        private final BatchConfiguration config;
        private final long startTime;
        private long endTime;
        private boolean completed;
        private boolean success;
        private String errorMessage;
        
        public BatchOperation(String batchId, String name, String description,
                            List<FileOperation> operations, BatchConfiguration config) {
            this.batchId = batchId;
            this.name = name;
            this.description = description;
            this.operations = operations != null ? operations : new ArrayList<>();
            this.config = config;
            this.startTime = System.currentTimeMillis();
            this.completed = false;
            this.success = false;
        }
        
        // Getters and setters
        public String getBatchId() { return batchId; }
        public String getName() { return name; }
        public String getDescription() { return description; }
        public List<FileOperation> getOperations() { return operations; }
        public BatchConfiguration getConfig() { return config; }
        public long getStartTime() { return startTime; }
        public long getEndTime() { return endTime; }
        public boolean isCompleted() { return completed; }
        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        
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
        
        public long getDuration() {
            return endTime - startTime;
        }
    }
    
    /**
     * Batch operation configuration.
     */
    public static class BatchConfiguration {
        private final boolean stopOnError;
        private final int maxConcurrentOperations;
        private final long operationTimeout;
        private final boolean enableRetry;
        private final int maxRetries;
        
        public BatchConfiguration(boolean stopOnError, int maxConcurrentOperations,
                               long operationTimeout, boolean enableRetry, int maxRetries) {
            this.stopOnError = stopOnError;
            this.maxConcurrentOperations = maxConcurrentOperations;
            this.operationTimeout = operationTimeout;
            this.enableRetry = enableRetry;
            this.maxRetries = maxRetries;
        }
        
        // Getters
        public boolean isStopOnError() { return stopOnError; }
        public int getMaxConcurrentOperations() { return maxConcurrentOperations; }
        public long getOperationTimeout() { return operationTimeout; }
        public boolean isEnableRetry() { return enableRetry; }
        public int getMaxRetries() { return maxRetries; }
        
        /**
         * Creates default configuration.
         * @return Default BatchConfiguration
         */
        public static BatchConfiguration getDefault() {
            return new BatchConfiguration(
                false,  // continue on error
                5,      // max 5 concurrent operations
                30000,  // 30 second timeout
                true,   // enable retry
                3       // max 3 retries
            );
        }
    }
    
    /**
     * Simple file operation representation.
     */
    public static class FileOperation {
        private final String operationId;
        private final String type;
        private final String sourcePath;
        private final String targetPath;
        private final Map<String, Object> parameters;
        
        public FileOperation(String operationId, String type, String sourcePath,
                           String targetPath, Map<String, Object> parameters) {
            this.operationId = operationId;
            this.type = type;
            this.sourcePath = sourcePath;
            this.targetPath = targetPath;
            this.parameters = parameters != null ? parameters : new HashMap<>();
        }
        
        // Getters
        public String getOperationId() { return operationId; }
        public String getType() { return type; }
        public String getSourcePath() { return sourcePath; }
        public String getTargetPath() { return targetPath; }
        public Map<String, Object> getParameters() { return parameters; }
    }
    
    /**
     * Operation queue for managing pending operations.
     */
    public static class OperationQueue {
        private final String queueId;
        private final List<FileOperation> pendingOperations;
        private final List<FileOperation> runningOperations;
        private final List<FileOperation> completedOperations;
        private final QueueConfiguration config;
        
        public OperationQueue(String queueId, QueueConfiguration config) {
            this.queueId = queueId;
            this.pendingOperations = new ArrayList<>();
            this.runningOperations = new ArrayList<>();
            this.completedOperations = new ArrayList<>();
            this.config = config;
        }
        
        // Getters
        public String getQueueId() { return queueId; }
        public List<FileOperation> getPendingOperations() { return pendingOperations; }
        public List<FileOperation> getRunningOperations() { return runningOperations; }
        public List<FileOperation> getCompletedOperations() { return completedOperations; }
        public QueueConfiguration getConfig() { return config; }
    }
    
    /**
     * Queue configuration.
     */
    public static class QueueConfiguration {
        private final int maxConcurrentOperations;
        private final long operationTimeout;
        private final boolean enablePriority;
        
        public QueueConfiguration(int maxConcurrentOperations, long operationTimeout,
                                boolean enablePriority) {
            this.maxConcurrentOperations = maxConcurrentOperations;
            this.operationTimeout = operationTimeout;
            this.enablePriority = enablePriority;
        }
        
        // Getters
        public int getMaxConcurrentOperations() { return maxConcurrentOperations; }
        public long getOperationTimeout() { return operationTimeout; }
        public boolean isEnablePriority() { return enablePriority; }
    }
    
    /**
     * Operation listener interface.
     */
    public interface OperationListener {
        void onOperationStarted(String operationId, String operationType);
        void onOperationCompleted(String operationId, boolean success, String result);
        void onOperationFailed(String operationId, String error);
        void onBatchOperationStarted(String batchId, String batchName);
        void onBatchOperationCompleted(String batchId, boolean success);
        void onWorkflowStarted(String workflowId, String workflowName);
        void onWorkflowCompleted(String workflowId, boolean success);
        void onTemplateUsed(String templateId, String filePath);
    }
    
    /**
     * Private constructor for singleton.
     */
    private FileOperationManager() {
        this.templates = new ConcurrentHashMap<>();
        this.workflows = new ConcurrentHashMap<>();
        this.batchOperations = new ConcurrentHashMap<>();
        this.listeners = new ArrayList<>();
        this.scheduler = Executors.newScheduledThreadPool(3);
        this.fileSystem = NetBeansFileSystem.getInstance();
        this.projectAnalyzer = ProjectAnalyzer.getInstance();
        this.operationQueues = new ConcurrentHashMap<>();
        
        initializeDefaultTemplates();
        initializeDefaultWorkflows();
        
        LOG.info("FileOperationManager initialized");
    }
    
    /**
     * Gets the singleton instance.
     * @return The FileOperationManager instance
     */
    public static synchronized FileOperationManager getInstance() {
        if (instance == null) {
            instance = new FileOperationManager();
        }
        return instance;
    }
    
    /**
     * Initializes default file templates.
     */
    private void initializeDefaultTemplates() {
        // Java Class Template
        Map<String, String> javaClassVars = new HashMap<>();
        javaClassVars.put("className", "NewClass");
        javaClassVars.put("packageName", "com.example");
        javaClassVars.put("author", "Developer");
        
        FileTemplate javaClassTemplate = new FileTemplate(
            "java_class", "Java Class", "Standard Java class template",
            "package ${packageName};\n\n/**\n * ${className}\n * @author ${author}\n */\npublic class ${className} {\n    \n}",
            javaClassVars, List.of("java"), false, "Java"
        );
        templates.put("java_class", javaClassTemplate);
        
        // Java Interface Template
        FileTemplate javaInterfaceTemplate = new FileTemplate(
            "java_interface", "Java Interface", "Standard Java interface template",
            "package ${packageName};\n\n/**\n * ${className}\n * @author ${author}\n */\npublic interface ${className} {\n    \n}",
            javaClassVars, List.of("java"), false, "Java"
        );
        templates.put("java_interface", javaInterfaceTemplate);
        
        // Maven POM Template
        Map<String, String> pomVars = new HashMap<>();
        pomVars.put("groupId", "com.example");
        pomVars.put("artifactId", "my-project");
        pomVars.put("version", "1.0.0");
        pomVars.put("description", "My Project");
        
        FileTemplate pomTemplate = new FileTemplate(
            "maven_pom", "Maven POM", "Standard Maven POM template",
            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<project xmlns=\"http://maven.apache.org/POM/4.0.0\">\n    <modelVersion>4.0.0</modelVersion>\n    <groupId>${groupId}</groupId>\n    <artifactId>${artifactId}</artifactId>\n    <version>${version}</version>\n    <description>${description}</description>\n</project>",
            pomVars, List.of("xml"), false, "Build"
        );
        templates.put("maven_pom", pomTemplate);
        
        // README Template
        Map<String, String> readmeVars = new HashMap<>();
        readmeVars.put("projectName", "My Project");
        readmeVars.put("description", "Project description");
        readmeVars.put("author", "Developer");
        
        FileTemplate readmeTemplate = new FileTemplate(
            "readme", "README", "Standard README template",
            "# ${projectName}\n\n${description}\n\n## Author\n${author}\n\n## License\nMIT",
            readmeVars, List.of("md", "txt"), false, "Documentation"
        );
        templates.put("readme", readmeTemplate);
    }
    
    /**
     * Initializes default workflows.
     */
    private void initializeDefaultWorkflows() {
        // Create Java Package Workflow
        List<WorkflowStep> createPackageSteps = new ArrayList<>();
        
        WorkflowStep createPackageDir = new WorkflowStep(
            "create_package_dir", "Create Package Directory", "create_directory",
            Map.of("path", "${projectPath}/src/main/java/${packageName}"),
            new ArrayList<>(), false, 1
        );
        createPackageSteps.add(createPackageDir);
        
        WorkflowStep createTestClassDir = new WorkflowStep(
            "create_test_dir", "Create Test Directory", "create_directory",
            Map.of("path", "${projectPath}/src/test/java/${packageName}"),
            List.of("create_package_dir"), false, 1
        );
        createPackageSteps.add(createTestClassDir);
        
        Workflow createPackageWorkflow = new Workflow(
            "create_java_package", "Create Java Package", 
            "Creates a complete Java package structure",
            createPackageSteps, new HashMap<>(), false, 60
        );
        workflows.put("create_java_package", createPackageWorkflow);
        
        // Setup Maven Project Workflow
        List<WorkflowStep> mavenSetupSteps = new ArrayList<>();
        
        WorkflowStep createSrcDir = new WorkflowStep(
            "create_src_dir", "Create Source Directory", "create_directory",
            Map.of("path", "${projectPath}/src/main/java"),
            new ArrayList<>(), false, 1
        );
        mavenSetupSteps.add(createSrcDir);
        
        WorkflowStep createTestDir = new WorkflowStep(
            "create_test_dir", "Create Test Directory", "create_directory",
            Map.of("path", "${projectPath}/src/test/java"),
            new ArrayList<>(), false, 1
        );
        mavenSetupSteps.add(createTestDir);
        
        WorkflowStep createResourcesDir = new WorkflowStep(
            "create_resources_dir", "Create Resources Directory", "create_directory",
            Map.of("path", "${projectPath}/src/main/resources"),
            new ArrayList<>(), false, 1
        );
        mavenSetupSteps.add(createResourcesDir);
        
        WorkflowStep createPom = new WorkflowStep(
            "create_pom", "Create POM File", "create_file",
            Map.of("path", "${projectPath}/pom.xml", "template", "maven_pom"),
            new ArrayList<>(), false, 1
        );
        mavenSetupSteps.add(createPom);
        
        Workflow mavenSetupWorkflow = new Workflow(
            "setup_maven_project", "Setup Maven Project",
            "Sets up a complete Maven project structure",
            mavenSetupSteps, new HashMap<>(), false, 120
        );
        workflows.put("setup_maven_project", mavenSetupWorkflow);
    }
    
    /**
     * Creates a file from template.
     * @param templateId The template ID
     * @param filePath The target file path
     * @param context Variable context
     * @return CompletableFuture with the operation result
     */
    public CompletableFuture<Boolean> createFileFromTemplate(String templateId, String filePath, 
                                                         Map<String, String> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                FileTemplate template = templates.get(templateId);
                if (template == null) {
                    LOG.warning("Template not found: " + templateId);
                    return false;
                }
                
                String content = template.processTemplate(context);
                
                // Create parent directories if needed
                File parentDir = new File(filePath).getParentFile();
                if (parentDir != null && !parentDir.exists()) {
                    parentDir.mkdirs();
                }
                
                // Write file
                return fileSystem.writeFileAsync(filePath, content, false)
                    .thenApply(operation -> operation.isSuccess())
                    .get();
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to create file from template: " + templateId, e);
                return false;
            }
        }).thenApply(success -> {
            notifyTemplateUsed(templateId, filePath);
            return success;
        });
    }
    
    /**
     * Executes a workflow.
     * @param workflowId The workflow ID
     * @param context Workflow context
     * @return CompletableFuture with the execution result
     */
    public CompletableFuture<Boolean> executeWorkflow(String workflowId, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Workflow workflow = workflows.get(workflowId);
                if (workflow == null) {
                    LOG.warning("Workflow not found: " + workflowId);
                    return false;
                }
                
                notifyWorkflowStarted(workflowId, workflow.getName());
                
                boolean success = true;
                
                if (workflow.isParallel()) {
                    // Execute steps in parallel
                    List<CompletableFuture<Boolean>> stepFutures = new ArrayList<>();
                    for (WorkflowStep step : workflow.getSteps()) {
                        stepFutures.add(executeWorkflowStep(step, context));
                    }
                    
                    // Wait for all steps to complete
                    for (CompletableFuture<Boolean> future : stepFutures) {
                        if (!future.get()) {
                            success = false;
                        }
                    }
                } else {
                    // Execute steps sequentially
                    for (WorkflowStep step : workflow.getSteps()) {
                        boolean stepSuccess = executeWorkflowStep(step, context).get();
                        if (!stepSuccess && !step.isOptional()) {
                            success = false;
                            break;
                        }
                    }
                }
                
                notifyWorkflowCompleted(workflowId, success);
                return success;
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to execute workflow: " + workflowId, e);
                notifyWorkflowCompleted(workflowId, false);
                return false;
            }
        });
    }
    
    /**
     * Executes a single workflow step.
     * @param step The workflow step
     * @param context The context
     * @return CompletableFuture with the step result
     */
    private CompletableFuture<Boolean> executeWorkflowStep(WorkflowStep step, Map<String, Object> context) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String operationType = step.getOperationType();
                Map<String, Object> parameters = step.getParameters();
                
                // Substitute variables in parameters
                Map<String, Object> resolvedParams = new HashMap<>();
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();
                    if (value instanceof String) {
                        String stringValue = (String) value;
                        for (Map.Entry<String, Object> contextEntry : context.entrySet()) {
                            stringValue = stringValue.replace("${" + contextEntry.getKey() + "}", 
                                                             String.valueOf(contextEntry.getValue()));
                        }
                        resolvedParams.put(key, stringValue);
                    } else {
                        resolvedParams.put(key, value);
                    }
                }
                
                // Execute operation based on type
                switch (operationType) {
                    case "create_directory":
                        return createDirectoryOperation(resolvedParams);
                    case "create_file":
                        return createFileOperation(resolvedParams);
                    case "copy_file":
                        return copyFileOperation(resolvedParams);
                    case "move_file":
                        return moveFileOperation(resolvedParams);
                    case "delete_file":
                        return deleteFileOperation(resolvedParams);
                    default:
                        LOG.warning("Unknown operation type: " + operationType);
                        return false;
                }
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to execute workflow step: " + step.getStepId(), e);
                return false;
            }
        });
    }
    
    /**
     * Creates a directory operation.
     */
    private boolean createDirectoryOperation(Map<String, Object> parameters) {
        try {
            String path = (String) parameters.get("path");
            return fileSystem.createDirectoryAsync(new File(path).getParent(), 
                                                 new File(path).getName())
                .thenApply(op -> op.isSuccess())
                .get();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create directory", e);
            return false;
        }
    }
    
    /**
     * Creates a file operation.
     */
    private boolean createFileOperation(Map<String, Object> parameters) {
        try {
            String path = (String) parameters.get("path");
            String templateId = (String) parameters.get("template");
            String content = (String) parameters.get("content");
            
            if (templateId != null) {
                Map<String, String> context = new HashMap<>();
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    if (entry.getValue() instanceof String) {
                        context.put(entry.getKey(), (String) entry.getValue());
                    }
                }
                return createFileFromTemplate(templateId, path, context).get();
            } else if (content != null) {
                return fileSystem.writeFileAsync(path, content, false)
                    .thenApply(op -> op.isSuccess())
                    .get();
            } else {
                return fileSystem.createFileAsync(new File(path).getParent(), 
                                                 new File(path).getName(), null)
                    .thenApply(op -> op.isSuccess())
                    .get();
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to create file", e);
            return false;
        }
    }
    
    /**
     * Copies a file operation.
     */
    private boolean copyFileOperation(Map<String, Object> parameters) {
        try {
            String source = (String) parameters.get("source");
            String target = (String) parameters.get("target");
            return fileSystem.copyAsync(source, target)
                .thenApply(op -> op.isSuccess())
                .get();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to copy file", e);
            return false;
        }
    }
    
    /**
     * Moves a file operation.
     */
    private boolean moveFileOperation(Map<String, Object> parameters) {
        try {
            String source = (String) parameters.get("source");
            String target = (String) parameters.get("target");
            return fileSystem.moveAsync(source, target)
                .thenApply(op -> op.isSuccess())
                .get();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to move file", e);
            return false;
        }
    }
    
    /**
     * Deletes a file operation.
     */
    private boolean deleteFileOperation(Map<String, Object> parameters) {
        try {
            String path = (String) parameters.get("path");
            return fileSystem.deleteAsync(path)
                .thenApply(op -> op.isSuccess())
                .get();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Failed to delete file", e);
            return false;
        }
    }
    
    /**
     * Executes a batch operation.
     * @param batchId The batch ID
     * @param operations List of operations
     * @param config Batch configuration
     * @return CompletableFuture with the batch result
     */
    public CompletableFuture<Boolean> executeBatchOperation(String batchId, List<FileOperation> operations,
                                                          BatchConfiguration config) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                BatchOperation batch = new BatchOperation(batchId, "Batch Operation", 
                                                         "Executing batch operations", 
                                                         operations, config);
                batchOperations.put(batchId, batch);
                
                notifyBatchOperationStarted(batchId, batch.getName());
                
                boolean success = true;
                
                if (config.getMaxConcurrentOperations() == 1) {
                    // Execute sequentially
                    for (FileOperation operation : operations) {
                        boolean opSuccess = executeFileOperation(operation).get();
                        if (!opSuccess && config.isStopOnError()) {
                            success = false;
                            break;
                        }
                    }
                } else {
                    // Execute in parallel with concurrency limit
                    // TODO: Implement parallel execution with semaphore
                    for (FileOperation operation : operations) {
                        boolean opSuccess = executeFileOperation(operation).get();
                        if (!opSuccess && config.isStopOnError()) {
                            success = false;
                            break;
                        }
                    }
                }
                
                batch.setSuccess(success);
                notifyBatchOperationCompleted(batchId, success);
                return success;
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to execute batch operation: " + batchId, e);
                return false;
            }
        });
    }
    
    /**
     * Executes a single file operation.
     * @param operation The file operation
     * @return CompletableFuture with the operation result
     */
    private CompletableFuture<Boolean> executeFileOperation(FileOperation operation) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                notifyOperationStarted(operation.getOperationId(), operation.getType());
                
                boolean success = false;
                
                switch (operation.getType()) {
                    case "create":
                        success = fileSystem.createFileAsync(
                            new File(operation.getTargetPath()).getParent(),
                            new File(operation.getTargetPath()).getName(),
                            null
                        ).thenApply(op -> op.isSuccess()).get();
                        break;
                    case "copy":
                        success = fileSystem.copyAsync(operation.getSourcePath(), operation.getTargetPath())
                            .thenApply(op -> op.isSuccess()).get();
                        break;
                    case "move":
                        success = fileSystem.moveAsync(operation.getSourcePath(), operation.getTargetPath())
                            .thenApply(op -> op.isSuccess()).get();
                        break;
                    case "delete":
                        success = fileSystem.deleteAsync(operation.getSourcePath())
                            .thenApply(op -> op.isSuccess()).get();
                        break;
                    default:
                        LOG.warning("Unknown operation type: " + operation.getType());
                }
                
                notifyOperationCompleted(operation.getOperationId(), success, "");
                return success;
                
            } catch (Exception e) {
                LOG.log(Level.SEVERE, "Failed to execute file operation: " + operation.getOperationId(), e);
                notifyOperationFailed(operation.getOperationId(), e.getMessage());
                return false;
            }
        });
    }
    
    /**
     * Adds a file template.
     * @param template The template to add
     */
    public void addTemplate(FileTemplate template) {
        templates.put(template.getTemplateId(), template);
        LOG.info("Template added: " + template.getTemplateId());
    }
    
    /**
     * Removes a file template.
     * @param templateId The template ID to remove
     */
    public void removeTemplate(String templateId) {
        templates.remove(templateId);
        LOG.info("Template removed: " + templateId);
    }
    
    /**
     * Adds a workflow.
     * @param workflow The workflow to add
     */
    public void addWorkflow(Workflow workflow) {
        workflows.put(workflow.getWorkflowId(), workflow);
        LOG.info("Workflow added: " + workflow.getWorkflowId());
    }
    
    /**
     * Removes a workflow.
     * @param workflowId The workflow ID to remove
     */
    public void removeWorkflow(String workflowId) {
        workflows.remove(workflowId);
        LOG.info("Workflow removed: " + workflowId);
    }
    
    /**
     * Gets all templates.
     * @return Copy of all templates
     */
    public Map<String, FileTemplate> getTemplates() {
        return new HashMap<>(templates);
    }
    
    /**
     * Gets all workflows.
     * @return Copy of all workflows
     */
    public Map<String, Workflow> getWorkflows() {
        return new HashMap<>(workflows);
    }
    
    /**
     * Gets all batch operations.
     * @return Copy of all batch operations
     */
    public Map<String, BatchOperation> getBatchOperations() {
        return new HashMap<>(batchOperations);
    }
    
    /**
     * Adds an operation listener.
     * @param listener The listener to add
     */
    public void addOperationListener(OperationListener listener) {
        listeners.add(listener);
    }
    
    /**
     * Removes an operation listener.
     * @param listener The listener to remove
     */
    public void removeOperationListener(OperationListener listener) {
        listeners.remove(listener);
    }
    
    // Notification methods
    
    private void notifyOperationStarted(String operationId, String operationType) {
        for (OperationListener listener : listeners) {
            try {
                listener.onOperationStarted(operationId, operationType);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyOperationCompleted(String operationId, boolean success, String result) {
        for (OperationListener listener : listeners) {
            try {
                listener.onOperationCompleted(operationId, success, result);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyOperationFailed(String operationId, String error) {
        for (OperationListener listener : listeners) {
            try {
                listener.onOperationFailed(operationId, error);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyBatchOperationStarted(String batchId, String batchName) {
        for (OperationListener listener : listeners) {
            try {
                listener.onBatchOperationStarted(batchId, batchName);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyBatchOperationCompleted(String batchId, boolean success) {
        for (OperationListener listener : listeners) {
            try {
                listener.onBatchOperationCompleted(batchId, success);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyWorkflowStarted(String workflowId, String workflowName) {
        for (OperationListener listener : listeners) {
            try {
                listener.onWorkflowStarted(workflowId, workflowName);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyWorkflowCompleted(String workflowId, boolean success) {
        for (OperationListener listener : listeners) {
            try {
                listener.onWorkflowCompleted(workflowId, success);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    private void notifyTemplateUsed(String templateId, String filePath) {
        for (OperationListener listener : listeners) {
            try {
                listener.onTemplateUsed(templateId, filePath);
            } catch (Exception e) {
                LOG.log(Level.WARNING, "Error notifying listener", e);
            }
        }
    }
    
    /**
     * Shuts down the operation manager.
     */
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        
        LOG.info("FileOperationManager shutdown completed");
    }
}
